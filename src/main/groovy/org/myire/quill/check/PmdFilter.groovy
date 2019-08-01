/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import groovy.xml.QName

import org.gradle.api.plugins.quality.Pmd

import org.myire.quill.common.ProjectAware
import org.myire.quill.filter.RuleViolationFilterLoader
import org.myire.quill.filter.RuleViolationMatcher


/**
 * A PMD filter applies a rule violation filter loaded from a file on the result of a PMD analysis.
 * Any violations matched by the filter are removed from the analysis result.
 */
class PmdFilter extends ProjectAware
{
    private final Pmd fTask;

    /**
     * The file to load the rule violation filter from.
     */
    File file

    /**
     * Is the filter enabled?
     */
    boolean enabled = true


    /**
     * Create a new {@code PmdFilter}.
     *
     * @param pTask     The task to filter the result of.
     */
    PmdFilter(Pmd pTask)
    {
        super(pTask.project);
        fTask = pTask;
    }


    void setFile(Object pFile)
    {
        file = pFile ? project.file(pFile) : null;
    }


    /**
     * Apply the filter specified in the file property to the result of the task specified in the
     * constructor.
     */
    void apply()
    {
        if (!enabled || !fTask.reports.getXml().destination?.canRead())
            // The filter isn't enabled, or the task's XML report doesn't exist, shouldn't/cannot
            // filter.
            return;

        if (file != null)
        {
            Collection<RuleViolationMatcher> aMatchers = loadFilterFile(file);
            fTask.logger.debug('Loaded {} PMD filters from {}',
                               String.valueOf(aMatchers.size()),
                               file.absolutePath);

            // Parse the PMD XML report file and filter it.
            File aPmdReportFile = fTask.reports.getXml().destination;
            Node aPmdReport = new XmlParser().parse(aPmdReportFile);
            int aNumFilteredViolations = doFilter(aPmdReport, aMatchers);
            if (aNumFilteredViolations > 0)
            {
                fTask.logger.warn('Filtered out {} rule violations from {}',
                                  aNumFilteredViolations,
                                  aPmdReportFile);

                // Replace the PMD XML report file with the filtered version.
                XmlNodePrinter aPrinter = new XmlNodePrinter(new PrintWriter(aPmdReportFile), '');
                aPrinter.preserveWhitespace = true;
                aPrinter.print(aPmdReport);
            }
        }
        else
            fTask.logger.debug('No PMD filter file specified');
    }


    /**
     * Load the contents of a rule violation filter file.
     *
     * @param pFile The file to load.
     *
     * @return  A collection containing the loaded rule violation matchers.
     */
    private Collection<RuleViolationMatcher> loadFilterFile(File pFile)
    {
        if (pFile.canRead())
        {
            return RuleViolationFilterLoader.loadXmlFile(pFile);
        }
        else
        {
            fTask.logger.warn('Cannot read PMD filter file {}, ignoring', pFile.absolutePath);
            return Collections.emptyList();
        }
    }


    /**
     * Filter a {@code Node} containing a PMD report. The node has the following structure:
     *<pre>
     * <pmd>
     *   <file>
     *     <violation>
     *     </violation>
     *     ...
     *     <violation>
     *     </violation>
     *   </file>
     *   ...
     *   <file>
     *   </file>
     * </pmd>
     *</pre>
     *
     * All violation children that are matched by one of the specified matchers will be removed.
     *
     * @param pPmdReport    The PMD report to filter.
     * @param pMatchers     The violation matchers to filter through.
     *
     * @return  The number of violations that were filtered.
     */
    private int doFilter(Node pPmdReport, Collection<RuleViolationMatcher> pMatchers)
    {
        int aNumFilteredViolations = 0;
        def aEmptyFileNodes = [];
        pPmdReport.children().each
        {
            Node aNode ->
                if (getLocalName(aNode.name()).equals('file'))
                {
                    aNumFilteredViolations += filterFileNode(aNode, pMatchers);
                    if (aNode.children().isEmpty())
                        // All violations were filtered out, mark this file node for removal from
                        // the report.
                        aEmptyFileNodes += aNode;
                }
        }

        // Remove all file nodes that had all violations filtered out.
        aEmptyFileNodes.each { Node aNode -> pPmdReport.remove(aNode) };

        return aNumFilteredViolations;
    }


    /**
     * Filter a {@code Node} specifying a file with one or more PMD rule violations. The node has
     * the following structure:
     *<pre>
     * <file name="...">
     *     <violation>
     *     </violation>
     *     ...
     *     <violation>
     *     </violation>
     * </file>
     *</pre>
     *
     * @param pFileNode The file node to filter.
     * @param pMatchers The violation matchers to filter through.
     *
     * @return  The number of violations that were filtered.
     */
    private int filterFileNode(Node pFileNode, Collection<RuleViolationMatcher> pMatchers)
    {
        String aFileName = pFileNode.attribute('name')?.toString();
        if (aFileName == null)
            return 0;

        def aMatchingNodes = [];
        pFileNode.children().each
        {
            Node aNode ->
                if (getLocalName(aNode.name()).equals('violation') && matches(aFileName, aNode, pMatchers))
                    // This violation node was matched by the filter, mark it for removal.
                    aMatchingNodes += aNode;
        }

        // Remove all violation nodes that were matched by the filter.
        aMatchingNodes.each { Node aNode -> pFileNode.remove(aNode) };
        return aMatchingNodes.size();
    }


    /**
     * Check if a violation node is matched by a filter. The node has the following structure:
     *<pre>
     * <violation beginline="20"
     *            endline="20"
     *            rule="SystemPrintln"
     *            ...
     *            >
     * ...
     * </violation>
     *</pre>
     *
     * @param pFileName         The name of the file the violation refers to.
     * @param pViolationNode    The violation node to check.
     * @param pMatchers         The matchers defining the filter to apply.
     *
     *  @return True if the file name and the violation's rule name and line number(s) are matched
     *          by the filter, false if not.
     */
    private boolean matches(String pFileName, Node pViolationNode, Collection<RuleViolationMatcher> pMatchers)
    {
        String aRuleName = pViolationNode.attribute('rule')?.toString();
        if (aRuleName == null)
            return false;

        int aBeginLine = parseLineNumber(pViolationNode.attribute('beginline')?.toString(), 1);
        int aEndLine = parseLineNumber(pViolationNode.attribute('endline')?.toString(), 0);
        for (i in aBeginLine..aEndLine)
        {
            if (pMatchers.any( { it.matches(aRuleName, pFileName, i)}))
            {
                fTask.logger.debug('Removing violation of rule {} in file {} at line {}',
                                   aRuleName,
                                   pFileName,
                                   String.valueOf(i));
                return true;
            }
        }

        return false;
    }


    static private String getLocalName(Object pNodeName)
    {
        if (pNodeName instanceof QName)
            return ((QName) pNodeName).localPart;
        else if (pNodeName != null)
            return pNodeName.toString();
        else
            return '';
    }


    static private int parseLineNumber(String pValue, int pDefault)
    {
        try
        {
            if (pValue != null)
                return Integer.parseInt(pValue);
            else
                return pDefault;
        }
        catch (NumberFormatException ignore)
        {
            return pDefault;
        }
    }
}
