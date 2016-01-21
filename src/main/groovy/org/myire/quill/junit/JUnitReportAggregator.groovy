/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.junit

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.regex.Pattern

import org.xml.sax.SAXException

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging


/**
 * An aggregator for one or more individual JUnit test reports.
 */
class JUnitReportAggregator
{
    static private final Logger cLogger = Logging.getLogger(JUnitReportAggregator.class);

    static private final String TIMESTAMP_FORMAT = 'yyyy-MM-dd\'T\'HH:mm:ss';
    static private final String DATE_ONLY_FORMAT ='yyyy-MM-dd';
    static private final String TIME_ONLY_FORMAT = 'HH:mm:ss';

    // The aggregated JUnit report uses GMT timestamps.
    static private final TimeZone TIMESTAMP_TZ = TimeZone.getTimeZone("GMT");

    private final DecimalFormat fDecimalParser = createDecimalParser();

    private int fNumTestSuites;
    private int fNumTests;
    private int fNumSkipped;
    private int fNumFailures;
    private int fNumErrors;
    private BigDecimal fTotalTime = BigDecimal.ZERO;
    private long fMinTimestamp = Long.MAX_VALUE;
    private long fMaxTimestamp;


    /**
     * Aggregate all JUnit report files in a directory and add the result to this instance.
     *
     * @param pDirectory        The directory to look for JUnit reports in.
     * @param pFileNamePattern  A regular expression with the pattern the report files' names must
     *                          match.
     */
    void aggregate(File pDirectory, String pFileNamePattern)
    {
        cLogger.debug('Creating JUnit summary report from files in \'{}\'', pDirectory.absolutePath);

        XmlParser aParser = new XmlParser();
        Pattern aPattern = pFileNamePattern ? Pattern.compile(pFileNamePattern) : null;
        pDirectory.listFiles().each
        {
            // Check if this directory entry is a readable file with a name that matches the JUnit
            // report file name pattern.
            if (it.isFile() && it.canRead() && matches(aPattern, it.getName()))
            {
                cLogger.debug('Parsing \'{}\'', it.absolutePath);

                try
                {
                    // Parse the JUnit XML report file and add it to this aggregation.
                    add(aParser.parse(it));
                }
                catch (SAXException saxe)
                {
                    cLogger.error('Failed to parse JUnit report file \'{}\', ignoring ({})',
                                  it.absolutePath,
                                  saxe.message);
                }
            }
            else
                cLogger.debug('Skipping \'{}\'', it.absolutePath);
        }
    }


    /**
     * Write this aggregation to an XML file.
     *
     * @param pFile The file to write to.
     */
    void writeXmlFile(File pFile)
    {
        def aAttributes = ['testsuites' : fNumTestSuites,
                           'tests' : fNumTests,
                           'skipped' : fNumSkipped,
                           'failures' : fNumFailures,
                           'errors' : fNumErrors,
                           'total-time' : fTotalTime,
                           'big-time' : fTotalTime.toString()];
        if (fMinTimestamp <= fMaxTimestamp)
        {
            SimpleDateFormat aDateOnly = new SimpleDateFormat(DATE_ONLY_FORMAT);
            SimpleDateFormat aTimeOnly = new SimpleDateFormat(TIME_ONLY_FORMAT);
            aAttributes['start-date'] = aDateOnly.format(new Date(fMinTimestamp));
            aAttributes['start-time'] = aTimeOnly.format(new Date(fMinTimestamp));
            aAttributes['end-date'] = aDateOnly.format(new Date(fMaxTimestamp));
            aAttributes['end-time'] = aTimeOnly.format(new Date(fMaxTimestamp));
        }

        pFile.parentFile?.mkdirs();
        cLogger.debug('Creating Junit summary report \'{}\'', pFile.absolutePath);
        new XmlNodePrinter(new PrintWriter(pFile)).print(new Node(null, 'junit-summary', aAttributes));
    }


    /**
     * Add a parsed JUnit test suite report to this aggregation.
     *
     * @param pTestSuiteReport  The parsed test suite report.
     */
    private void add(Node pTestSuiteReport)
    {
        fNumTestSuites++;
        fNumTests += parseInteger(getAttribute(pTestSuiteReport, 'tests'));
        fNumSkipped += parseInteger(getAttribute(pTestSuiteReport, 'skipped'));
        fNumFailures += parseInteger(getAttribute(pTestSuiteReport, 'failures'));
        fNumErrors += parseInteger(getAttribute(pTestSuiteReport, 'errors'));
        fTotalTime = fTotalTime.add(parseBigDecimal(getAttribute(pTestSuiteReport, 'time')));

        String aTimestampValue = getAttribute(pTestSuiteReport, 'timestamp');
        if (aTimestampValue != null)
        {
            SimpleDateFormat aTimestampParser = new SimpleDateFormat(TIMESTAMP_FORMAT);
            aTimestampParser.setTimeZone(TIMESTAMP_TZ);
            long aTimestamp = aTimestampParser.parse(aTimestampValue).getTime();
            if (fMinTimestamp > aTimestamp)
                fMinTimestamp = aTimestamp;
            if (fMaxTimestamp < aTimestamp)
                fMaxTimestamp = aTimestamp;
        }
    }


    private BigDecimal parseBigDecimal(String pValue)
    {
        try
        {
            if (pValue != null)
                return (BigDecimal) fDecimalParser.parse(pValue);
        }
        catch (ParseException pe)
        {
            cLogger.warn('Failed to parse decimal value \'{}\'', pValue, pe);
        }

        return BigDecimal.ZERO;
    }


    static private DecimalFormat createDecimalParser()
    {
        DecimalFormatSymbols aSymbols = new DecimalFormatSymbols();
        aSymbols.setDecimalSeparator('.' as char);
        DecimalFormat aFormat = new DecimalFormat("#.#", aSymbols);
        aFormat.setParseBigDecimal(true);
        return aFormat;
    }


    static private int parseInteger(String pValue)
    {
        try
        {
            if (pValue != null)
                return Integer.parseInt(pValue);
        }
        catch (NumberFormatException nfe)
        {
            cLogger.warn('Failed to parse integer value \'{}\'', pValue, nfe);
        }

        return 0;
    }


    static private boolean matches(Pattern pPattern, String pValue)
    {
        return pPattern ? pPattern.matcher(pValue).matches() : true;
    }


    static private String getAttribute(Node pNode, String pName)
    {
        Object aValue = pNode.attribute(pName);
        return aValue != null ? aValue.toString() : null;
    }
}
