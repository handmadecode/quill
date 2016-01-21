/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.filter

import org.junit.After
import org.junit.Test
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue


/**
 * Unit tests for the {@code RuleViolationFilterLoader} class.
 */
class RuleViolationFilterLoaderTest
{
    private File fXmlFile;


    @After
    public void cleanup()
    {
        if (fXmlFile != null && fXmlFile.exists())
            fXmlFile.delete();
    }


    @Test
    public void testLoadOneFilter()
    {
        // Given
        String aFilePattern = 'Parser.java';
        String aXml = '<rule-violation-filter files="' + aFilePattern + '"/>';
        fXmlFile = createXmlFile(aXml);

        // When
        Collection<RuleViolationMatcher> aMatchers = new RuleViolationFilterLoader().loadXmlFile(fXmlFile);

        // Then
        assertEquals(1, aMatchers.size());
        RuleViolationMatcher aMatcher = aMatchers.first();
        assertTrue(aMatcher.matches('AnyRule', aFilePattern, 1));
        assertTrue(aMatcher.matches('AnyRule', aFilePattern, Integer.MAX_VALUE));
        assertFalse(aMatcher.matches('AnyRule', 'X' + aFilePattern, 1));
    }


    @Test
    public void testLoadTwoFilters()
    {
        // Given
        String aXml =
                '<rule-violation-filter files=".*Scanner\\.java"/>' +
                '<rule-violation-filter files="Parser.java" lines="1- 3, 17"/>';
        fXmlFile = createXmlFile(aXml);

        // When
        Collection<RuleViolationMatcher> aMatchers = new RuleViolationFilterLoader().loadXmlFile(fXmlFile);

        // Then
        assertEquals(2, aMatchers.size());

        // Should be matched by the first matcher.
        assertTrue(matches(aMatchers, 'AnyRule', 'Scanner.java', 1));
        assertTrue(matches(aMatchers, 'AnyRule', 'TheScanner.java', Integer.MAX_VALUE));

        // Should be matched by the second matcher.
        assertTrue(matches(aMatchers, 'AnyRule', 'Parser.java', 1));
        assertTrue(matches(aMatchers, 'AnyRule', 'Parser.java', 2));
        assertTrue(matches(aMatchers, 'AnyRule', 'Parser.java', 3));
        assertTrue(matches(aMatchers, 'AnyRule', 'Parser.java', 17));

        // Should be rejected by the first matcher due to unmatched file name.
        assertFalse(matches(aMatchers, 'AnyRule', 'Scanner.groovy', Integer.MAX_VALUE));

        // Should be rejected by the second matcher due to unmatched line number.
        assertFalse(matches(aMatchers, 'AnyRule', 'Parser.java', Integer.MAX_VALUE));

        // Should be rejected by the second matcher due to unmatched file name.
        assertFalse(matches(aMatchers, 'AnyRule', 'Parser2.java', 2));
    }


    @Test
    public void testLoadMultipleFilters()
    {
        // Given
        String aXml =
                '<rule-violation-filter files=".*Scanner\\.java"/>' +
                '<rule-violation-filter files="Parser.java" lines="1, 3-13, 17"/>' +
                '<rule-violation-filter files="Parser.java" rules=".*StupidRule"/>' +
                '<rule-violation-filter files=".*Parser.java" rules="StupidRule" lines="50"/>';
        fXmlFile = createXmlFile(aXml);

        // When
        Collection<RuleViolationMatcher> aMatchers = new RuleViolationFilterLoader().loadXmlFile(fXmlFile);

        // Then
        assertEquals(4, aMatchers.size());

        // Should be matched by the first matcher.
        assertTrue(matches(aMatchers, 'AnyRule', 'Scanner.java', 1));
        assertTrue(matches(aMatchers, 'AnyRule', 'BetterScanner.java', 4711));

        // Should be matched by the second matcher.
        assertTrue(matches(aMatchers, 'AnyRule', 'Parser.java', 1));
        assertTrue(matches(aMatchers, 'AnyRule', 'Parser.java', 3));
        assertTrue(matches(aMatchers, 'AnyRule', 'Parser.java', 13));

        // Should be matched by the third matcher.
        assertTrue(matches(aMatchers, 'StupidRule', 'Parser.java', 1000));
        assertTrue(matches(aMatchers, 'ReallyStupidRule', 'Parser.java', 1000));

        // Should be matched by the fourth matcher.
        assertTrue(matches(aMatchers, 'StupidRule', 'Parser.java', 50));
        assertTrue(matches(aMatchers, 'StupidRule', 'MyParser.java', 50));

        // Should be rejected by the first matcher due to unmatched file name.
        assertFalse(matches(aMatchers, 'AnyRule', 'Scanner.groovy', Integer.MAX_VALUE));

        // Should be rejected by the second matcher due to unmatched line number.
        assertFalse(matches(aMatchers, 'AnyRule', 'Parser.java', Integer.MAX_VALUE));

        // Should be rejected by the second matcher due to unmatched file name.
        assertFalse(matches(aMatchers, 'AnyRule', 'Parser2.java', 2));

        // Should be rejected by the third matcher due to unmatched file name.
        assertFalse(matches(aMatchers, 'StupidRule', 'Parser2.java', 2));

        // Should be rejected by the third matcher due to unmatched rule name.
        assertFalse(matches(aMatchers, 'StupidRules', 'Parser.java', 2));

        // Should be rejected by the fourth matcher due to unmatched rule name.
        assertFalse(matches(aMatchers, 'ReallyStupidRule', 'MyParser.java', 50));

        // Should be rejected by the fourth matcher due to unmatched file name.
        assertFalse(matches(aMatchers, 'StupidRule', 'Parser2.java', 50));

        // Should be rejected by the fourth matcher due to unmatched line number.
        assertFalse(matches(aMatchers, 'StupidRule', 'MyParser.java', 51));
    }


    @Test
    public void testLoadFilterWithMissingFilePattern()
    {
        // Given
        String aXml =
                '<rule-violation-filter file-names=".*Scanner\\.java"/>' +
                '<rule-violation-filter files=".*Parser.java" rules="StupidRule" lines="50"/>';
        fXmlFile = createXmlFile(aXml);

        // When
        Collection<RuleViolationMatcher> aMatchers = new RuleViolationFilterLoader().loadXmlFile(fXmlFile);

        // Then
        assertEquals(1, aMatchers.size());
    }


    @Test
    public void testLoadFilterWithMalformedFilePattern()
    {
        // Given
        String aXml = '<rule-violation-filter files="[z-a]*.java" rules="StupidRule" lines="50"/>';
        fXmlFile = createXmlFile(aXml);

        // When
        Collection<RuleViolationMatcher> aMatchers = new RuleViolationFilterLoader().loadXmlFile(fXmlFile);

        // Then
        assertEquals(0, aMatchers.size());
    }


    @Test
    public void testLoadFilterWithMalformedRulePattern()
    {
        String aXml =
                '<rule-violation-filter files=".*Scanner\\.java"/>' +
                '<rule-violation-filter files="Parser.java" lines="1, 3-13, 17"/>' +
                '<rule-violation-filter files="Parser.java" rules="[Z-N]+"/>' +
                '<rule-violation-filter files=".*Parser.java" rules="StupidRule" lines="50"/>';
        fXmlFile = createXmlFile(aXml);

        // When
        Collection<RuleViolationMatcher> aMatchers = new RuleViolationFilterLoader().loadXmlFile(fXmlFile);

        // Then
        assertEquals(3, aMatchers.size());
    }


    @Test
    public void testLoadFilterWithMalformedLineNumbers()
    {
        String aXml =
                '<rule-violation-filter files=".*Scanner\\.java"/>' +
                '<rule-violation-filter files="Parser.java" lines="9-8"/>' +
                '<rule-violation-filter files="Parser.java" lines="1, 4:13, 17"/>' +
                '<rule-violation-filter files=".*Parser.java" rules="StupidRule" lines="x"/>';
        fXmlFile = createXmlFile(aXml);

        // When
        Collection<RuleViolationMatcher> aMatchers = new RuleViolationFilterLoader().loadXmlFile(fXmlFile);

        // Then
        assertEquals(1, aMatchers.size());
    }


    static private boolean matches(Collection<RuleViolationMatcher> pMatchers, String pRule, String pFile, int pLine)
    {
        return pMatchers.any { it.matches(pRule, pFile, pLine) };
    }


    static private File createXmlFile(String pXml) {
        File aFile = File.createTempFile('RuleViolationFilterLoaderTest', '.xml');
        FileWriter aWriter = new FileWriter(aFile);
        aWriter.write('<?xml version="1.0"?><rule-violation-filters>');
        aWriter.write(pXml);
        aWriter.write('</rule-violation-filters>');
        aWriter.close();
        return aFile;
    }
}
