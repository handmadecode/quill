/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.filter

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import java.text.ParseException
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


/**
 * Utility methods for code rule filters.
 */
class RuleViolationFilterLoader
{
    static private final Logger cLogger = Logging.getLogger(RuleViolationFilterLoader.class);

    /**
     * Load an XML file containing one or more rule violation filters. The file has the following
     * format:
     *<pre>
     * <?xml version="1.0"?>
     * <rule-violation-filters>
     *   <rule-violation-filter .../>
     *   ...
     * </rule-violation-filters>
     *</pre>
     * A rule violation filter element has one mandatory attribute, <i>files</i>, that contains a
     * regular expression specifying the file name(s) that the filter applies to.
     *<p>
     * The optional <i>rules</i> attribute contains a regular expression specifying the names of the
     * rules that the filter applies to. If this attribute isn't specified, all rules are filtered
     * out in the (parts of) the file(s) specified.
     *<p>
     * The <i>lines</i> attribute contains a comma-separated list of line numbers and line number
     * ranges that specify the lines of the file(s) that the filter should be applied to. If this
     * attribute isn't specified, the filter applies to the entire file(s).
     *<p>
     * Examples
     *<p>
     * Filter out rule &quot;r1&quot; in file &quot;AAA.java&quot;:
     *<pre>
     *  <rule-violation-filter files="AAA.java" rules="r1"/>
     *</pre>
     * Filter out rule &quot;r1&quot; in file &quot;.XXX.java&quot; at lines 2, 5 and 130-156:
     *<pre>
     *  <rule-violation-filter files="XXX.java" lines="2,5,130-156" rules="r1"/>
     *</pre>
     * Filter out all rules between lines 17 and 26 in file &quot;Dummy.java&quot;:
     *<pre>
     *  <rule-violation-filter files="Dummy.java" lines="17-26"/>
     *</pre>
     * Filter out rules &quot;r1&quot; and &quot;r3&quot; in files matching the pattern
     * &quot;.*Parser.*\.java&quot;:
     *<pre>
     *  <rule-violation-filter files=".*Parser.*\.java" rules="^(r1|r3)$"/>
     *</pre>
     * Filter out everything in files matching the pattern &quot;.*Test.*\.java&quot;:
     *<pre>
     *  <rule-violation-filter files=".*Test.*\.java"/>
     *</pre>
     *
     * @param pFile The file to load.
     *
     * @return  A new collection of {@code RuleViolationMatcher} instances, empty if the load fails.
     */
    static Collection<RuleViolationMatcher> loadXmlFile(File pFile)
    {
        cLogger.debug('Loading filter file \'{}\'', pFile.absolutePath);

        Collection<RuleViolationMatcher> aMatchers = [];
        new XmlParser().parse(pFile).children()?.each
        {
            Node aNode ->
                RuleViolationMatcher aMatcher = createRuleViolationMatcher(aNode);
                if (aMatcher != null)
                    aMatchers.add(aMatcher);
        }

        return aMatchers;
    }


    static private RuleViolationMatcher createRuleViolationMatcher(Node pNode)
    {
        try
        {
            return createRuleViolationMatcher(getPatternAttribute(pNode, 'files'),
                                              getIntegerSetAttribute(pNode, 'lines'),
                                              getPatternAttribute(pNode, 'rules'));
        }
        catch (ParseException pe)
        {
            cLogger.warn('Skipping invalid rule violation filter: {}', pe.getMessage(), pe);
            return null;
        }
    }


    static private RuleViolationMatcher createRuleViolationMatcher(Pattern pFilesPattern,
                                                                   BitSet pLineNumbers,
                                                                   Pattern pRulesPattern) throws ParseException
    {
        if (pFilesPattern == null)
            throw new ParseException('missing "files" attribute', 0);

        if (pLineNumbers != null)
        {
            if (pRulesPattern != null)
                // Files pattern, line numbers and rules pattern specified.
                return RuleViolationMatchers.newMatcher(pFilesPattern, pLineNumbers, pRulesPattern);
            else
                // Files pattern and line numbers but no rules pattern.
                return RuleViolationMatchers.newMatcher(pFilesPattern, pLineNumbers);
        }
        else if (pRulesPattern != null)
            // Files and rules pattern but no line numbers.
            return RuleViolationMatchers.newMatcher(pFilesPattern, pRulesPattern);
        else
            // Only files pattern specified.
            return RuleViolationMatchers.newMatcher(pFilesPattern);
    }


    static private Pattern getPatternAttribute(Node pNode, String pAttribute) throws ParseException
    {
        String aPattern = pNode.attribute(pAttribute)?.toString();
        if (aPattern == null)
            return null;

        try
        {
            return Pattern.compile(aPattern);
        }
        catch (PatternSyntaxException pse)
        {
            String aMessage = String.format('Invalid regular expression "%s" in attribute "%s"', aPattern, pAttribute);
            ParseException pe = new ParseException(aMessage, 0);
            pe.initCause(pse);
            throw pe;
        }
    }


    static BitSet getIntegerSetAttribute(Node pNode, String pAttribute) throws ParseException
    {
        String[] aLineNumbers = getCommaSeparatedStrings(pNode.attribute(pAttribute)?.toString());
        if (aLineNumbers == null)
            return null;

        BitSet aBitSet = new BitSet();
        aLineNumbers.each
        {
            int aRangeSeparatorOffset = it.indexOf('-');
            if (aRangeSeparatorOffset != -1)
            {
                // Integer range, parse the values before and after the '-'.
                int aLower = parseLineNumber(it.substring(0, aRangeSeparatorOffset));
                int aUpper = parseLineNumber(it.substring(aRangeSeparatorOffset + 1));
                if (aLower <= aUpper)
                    aBitSet.set(aLower, aUpper + 1);
                else
                    throw new ParseException('Invalid line number range ' + it, 0);
            }
            else
                // Single integer value.
                aBitSet.set(parseLineNumber(it));
        }

        return aBitSet;
    }


    /**
     * Create a String array from a string containing a list of comma separated strings.
     *
     * @param pCommaSeparatedStrings    The comma separated strings to extract.
     *
     * @return  An array of Strings extracted from {@code pCommaSeparatedStrings}. If
     *          {@code pCommaSeparatedStrings} is null, an empty array will be returned.
     */
    static private String[] getCommaSeparatedStrings(String pCommaSeparatedStrings)
    {
        if (pCommaSeparatedStrings == null)
            return null;

        StringTokenizer aTokenizer =
                new StringTokenizer(pCommaSeparatedStrings, ',');
        String[] aStrings = new String[aTokenizer.countTokens()];
        for (int i=0; i<aStrings.length; i++)
            aStrings[i] = aTokenizer.nextToken();

        return aStrings;
    }


    static private int parseLineNumber(String pValue) throws ParseException
    {
        pValue = pValue.trim();
        try
        {
            int aLineNumber = Integer.parseInt(pValue);
            if (aLineNumber > 0)
                return aLineNumber;
            else
                throw new ParseException('Invalid line number: ' + pValue, 0);
        }
        catch (NumberFormatException nfe)
        {
            ParseException pe = new ParseException('Invalid line number: ' + pValue, 0);
            pe.initCause(nfe);
            throw pe;
        }
    }
}
