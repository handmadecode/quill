/*
 * Copyright 2015, 2018-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd;

import java.io.File;
import java.io.IOException;

import groovy.lang.Closure;

import org.gradle.api.Action;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.plugins.quality.PmdExtension;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

import org.gradle.util.VersionNumber;
import org.myire.quill.common.ExternalToolLoader;
import org.myire.quill.common.Projects;
import org.myire.quill.common.Tasks;
import org.myire.quill.report.FormatChoiceReport;
import org.myire.quill.report.TransformingReport;


/**
 * Task for performing copy-paste detection using the CPD tool.
 */
public class CpdTask extends SourceTask implements Reporting<CpdReports>
{
    static private final VersionNumber MINIMUM_TOOL_VERSION = VersionNumber.parse("6.1.0");
    static private final String DEFAULT_TOOL_VERSION = "6.16.0";
    static private final String PMD_EXTENSION_NAME = "pmd";

    // Fully qualified name of the CpdRunner implementation class to use.
    static private final String IMPLEMENTATION_PACKAGE = "org.myire.quill.cpd.impl.";
    static private final String IMPLEMENTATION_CLASS = "CpdRunnerImpl";


    // Task properties.
    private String fToolVersion;
    private FileCollection fCpdClasspath;
    private CpdReports fReports;
    private final CpdParameters fCpdParameters = new CpdParameters();


    @Override
    @InputFiles
    @SkipWhenEmpty
    public FileTree getSource()
    {
        FileTree aSource = super.getSource();
        if (aSource == null || aSource.isEmpty())
        {
            SourceSet aMainSourceSet = Projects.getSourceSet(getProject(), SourceSet.MAIN_SOURCE_SET_NAME);
            if (aMainSourceSet != null)
            {
                aSource = aMainSourceSet.getAllJava();
                setSource(aSource);
            }
        }

        return aSource;
    }


    /**
     * Get the version of CPD to use. Default is the version specified in
     * {@code PmdExtension.toolVersion}, or, if that extension isn't available, version
     * &quot;6.1.0&quot;.
     */
    @Input
    public String getToolVersion()
    {
        if (fToolVersion == null)
        {
            VersionNumber aPmdVersion = parsePmdVersion();
            if (aPmdVersion != null && aPmdVersion.compareTo(MINIMUM_TOOL_VERSION) >= 0)
                // The version of PMD in use is compatible with the minimum tool version.
                fToolVersion = aPmdVersion.toString();
            else
                fToolVersion = DEFAULT_TOOL_VERSION;
        }

        return fToolVersion;
    }


    public void setToolVersion(String pToolVersion)
    {
        fToolVersion = pToolVersion;
    }


    /**
     * Get the classpath containing the CPD classes used by the task. The plugin sets this property
     * to its default value, which is the {@code cpd} configuration.
     */
    @InputFiles
    public FileCollection getCpdClasspath()
    {
        return fCpdClasspath;
    }


    public void setCpdClasspath(FileCollection pCpdClasspath)
    {
        fCpdClasspath = pCpdClasspath;
    }


    /**
     * Get the reports produced by this task.
     *
     * @return  The reports.
     */
    @Override
    @Nested
    public CpdReports getReports()
    {
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pClosure  A closure that configures the reports.
     *
     * @return  This task's reports.
     */
    @Override
    public CpdReports reports(Closure pClosure)
    {
        fReports.configure(pClosure);
        return fReports;
    }


    /**
     * Configure this task's reports.
     *
     * @param pAction   An action that configures the reports.
     *
     * @return  This task's reports.
     */
    @Override
    public CpdReports reports(Action<? super CpdReports> pAction)
    {
        pAction.execute(fReports);
        return fReports;
    }


    /**
     * Get the encoding used by CPD to read the source files and to produce the report. The
     * platform's default encoding will be used if this property isn't specified.
     */
    @Input
    @Optional
    public String getEncoding()
    {
        return fCpdParameters.getEncoding();
    }


    public void setEncoding(String pEncoding)
    {
        fCpdParameters.setEncoding(pEncoding);
    }


    /**
     * Get the language of the source files to analyze, e.g. &quot;cpp&quot;, &quot;java&quot;,
     * &quot;php&quot;, &quot;ruby&quot;, or &quot;ecmascript&quot;. See
     * <a href="http://pmd.sourceforge.net">the CPD documentation</a> for the list of languages
     * supported by the different versions of CPD. The default is &quot;java&quot;.
     */
    @Input
    @Optional
    public String getLanguage()
    {
        return fCpdParameters.getLanguage();
    }


    public void setLanguage(String pLanguage)
    {
        fCpdParameters.setLanguage(pLanguage);
    }


    /**
     * The minimum duplicate size to be reported. The default is 100.
     */
    @Input
    public int getMinimumTokenCount()
    {
        return fCpdParameters.getMinimumTokenCount();
    }


    public void setMinimumTokenCount(int pMinimumTokenCount)
    {
        fCpdParameters.setMinimumTokenCount(pMinimumTokenCount);
    }


    /**
     * If true, CPD ignores literal value differences when evaluating a duplicate block. This means
     * that {@code foo=42;} and {@code foo=43;} will be seen as equivalent. Default is false.
     */
    @Input
    public boolean isIgnoreLiterals()
    {
        return fCpdParameters.isIgnoreLiterals();
    }


    public void setIgnoreLiterals(boolean pIgnoreLiterals)
    {
        fCpdParameters.setIgnoreLiterals(pIgnoreLiterals);
    }


    /**
     * If true, differences in identifiers (like  variable names or methods names) will be ignored
     * in the same way as literals in {@link #isIgnoreLiterals()}. Default is false.
     */
    @Input
    public boolean isIgnoreIdentifiers()
    {
        return fCpdParameters.isIgnoreIdentifiers();
    }


    public void setIgnoreIdentifiers(boolean pIgnoreIdentifiers)
    {
        fCpdParameters.setIgnoreIdentifiers(pIgnoreIdentifiers);
    }


    /**
     * If true, annotations will be ignored. This property can be useful when analyzing code based
     * one some frameworks where annotations become very repetitive. Default is false.
     */
    @Input
    public boolean isIgnoreAnnotations()
    {
        return fCpdParameters.isIgnoreAnnotations();
    }


    public void setIgnoreAnnotations(boolean pIgnoreAnnotations)
    {
        fCpdParameters.setIgnoreAnnotations(pIgnoreAnnotations);
    }


    /**
     * If true, CPD will ignore multiple copies of files with the same name and length. Default is
     * false.
     */
    @Input
    public boolean isSkipDuplicateFiles()
    {
        return fCpdParameters.isSkipDuplicateFiles();
    }


    public void setSkipDuplicateFiles(boolean pSkipDuplicateFiles)
    {
        fCpdParameters.setSkipDuplicateFiles(pSkipDuplicateFiles);
    }


    /**
     * If true, CPD will skip files which can't be tokenized due to invalid characters instead of
     * aborting the analysis. Default is false.
     */
    @Input
    public boolean isSkipLexicalErrors()
    {
        return fCpdParameters.isSkipLexicalErrors();
    }


    public void setSkipLexicalErrors(boolean pSkipLexicalErrors)
    {
        fCpdParameters.setSkipLexicalErrors(pSkipLexicalErrors);
    }


    /**
     * If true, skipping of blocks is enabled with the patterns specified in the
     * {@code skipBlocksPattern} property. Default is false.
     */
    @Input
    public boolean isSkipBlocks()
    {
        return fCpdParameters.isSkipBlocks();
    }


    public void setSkipBlocks(boolean pSkipBlocks)
    {
        fCpdParameters.setSkipBlocks(pSkipBlocks);
    }


    /**
     * Specifies the pattern to find the blocks to skip when {@code skipBlocks} is true. The string
     * value contains two parts, separated by a vertical line ('|'). The first part is the start
     * pattern, the second part is the end pattern. The default value is &quot;#if 0|#endif&quot;.
     */
    @Input
    @Optional
    public String getSkipBlocksPattern()
    {
        return fCpdParameters.getSkipBlocksPattern();
    }


    public void setSkipBlocksPattern(String pSkipBlocksPattern)
    {
        fCpdParameters.setSkipBlocksPattern(pSkipBlocksPattern);
    }


    /**
     * If true, <i>using directives</i> in C# will be ignored when comparing text. Default is false.
     */
    @Input
    public boolean isIgnoreUsings()
    {
        return fCpdParameters.isIgnoreUsings();
    }


    public void setIgnoreUsings(boolean pIgnoreUsings)
    {
        fCpdParameters.setIgnoreUsings(pIgnoreUsings);
    }


    /**
     * Perform copy-paste detection on the sources and produces the enabled report(s).
     */
    @TaskAction
    public void run()
    {
        FormatChoiceReport aPrimaryReport = fReports.getPrimary();
        if (aPrimaryReport.isEnabled())
        {
            // Ensure the report's parent directory exists.
            Projects.ensureParentExists(aPrimaryReport.getDestination());

            // Perform the copy-paste detection and create the primary report.
            runCpd(aPrimaryReport);

            // Create the HTML report if enabled and the primary report is an XML report.
            TransformingReport aHtmlReport = fReports.getHtml();
            if (aHtmlReport.isEnabled() && CpdReports.FORMAT_XML.equals(aPrimaryReport.getFormat()))
                aHtmlReport.transform();
        }
        else
            getLogger().info("CPD primary is report disabled, skipping analysis");
    }


    /**
     * Perform CPD analysis on the task's sources.
     *
     * @param pPrimaryReport    The report to write the analysis result to.
     */
    private void runCpd(FormatChoiceReport pPrimaryReport)
    {
        try
        {
            loadCpdRunner().runCpd(
                getSource().getFiles(),
                pPrimaryReport.getDestination(),
                pPrimaryReport.getFormat(),
                fCpdParameters);
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            getLogger().error("Could not create an instance of '{}{}'",
                              IMPLEMENTATION_PACKAGE,
                              IMPLEMENTATION_CLASS,
                              e);
        }
        catch (IOException ioe)
        {
            getLogger().error("Could not perform CPD analysis", ioe);
        }
    }


    /**
     * Create the task's report container and specify the report related inputs and outputs.
     */
    void setupReports()
    {
        fReports = new CpdReportsImpl(this);

        // Only execute the task if its primary report is enabled.
        onlyIf(ignore -> getReports().getPrimary().isEnabled());

        // If any of the reports' enabled flag is modified the task should be rerun.
        Tasks.inputProperty(this, "'primaryReportEnabled'", () -> this.getReports().getPrimary().isEnabled());
        Tasks.inputProperty(this, "htmlReportEnabled", () -> this.getReports().getHtml().isEnabled());

        // The XSL file used to create the HTML report is an input to the task.
        Tasks.optionalInputFile(this, () -> this.getReports().getHtml().getXslFile());

        // Add both reports' destination as output of this task.
        Tasks.outputFile(this, () -> this.getReports().getPrimary().getDestination());
        Tasks.outputFile(this, () -> this.getReports().getHtml().getDestination());
    }


    /**
     * Add checks of the reports to the task's up-to-date check of its outputs.
     */
    void addUpToDateCheck()
    {
        // Let the HTML report decide if it is up to date.
        getOutputs().upToDateWhen(ignore -> this.getReports().getHtml().checkUpToDate());
    }


    /**
     * Get the default destination for the task's primary report.
     *
     * @return  The primary report's destination.
     */
    File defaultPrimaryDestination()
    {
        String aExtension = getReports().getPrimary().getFormat();
        if (CpdReports.FORMAT_CSV_LINECOUNT.equals(aExtension))
            aExtension = CpdReports.FORMAT_CSV;

        return new File(Projects.createReportDirectorySpec(getProject(), "cpd"), "cpd." + aExtension);
    }


    /**
     * Load the {@code CpdRunner} implementation and thereby the CPD classes specified by the
     * {@code cpdClasspath} property.
     *
     * @return  A new instance of the loaded {@code CpdRunner} implementation.
     *
     * @throws ClassNotFoundException   if the implementation class or any class it refers to could
     *                                  not be found.
     * @throws InstantiationException   if the implementation class is abstract or doesn't have a
     *                                  no-args constructor (or can't be instantiated for some other
     *                                  reason).
     * @throws IllegalAccessException   if the implementation class or its no-args constructor
     *                                  isn't accessible.
     */
    private CpdRunner loadCpdRunner()
        throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        ExternalToolLoader<CpdRunner> aLoader =
            new ExternalToolLoader<>(
                CpdRunner.class,
                IMPLEMENTATION_PACKAGE,
                IMPLEMENTATION_CLASS,
                this::getCpdClasspath);

        return aLoader.createToolProxy();
    }


    /**
     * Get the version number specified on the PMD extension, if available.
     *
     * @return  The PMD tool version, or null if the PMD extension isn't available in the task's
     *          project.
     */
    private VersionNumber parsePmdVersion()
    {
        PmdExtension aPmdExtension = Projects.getExtension(getProject(), PMD_EXTENSION_NAME, PmdExtension.class);
        return aPmdExtension != null ? VersionNumber.parse(aPmdExtension.getToolVersion()) : null;
    }
}
