/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.filter

import java.util.regex.Pattern

import org.junit.Test
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue


/**
 * Unit tests for the {@code RuleViolationMatchers} class.
 */
class RuleViolationMatchersTest
{
    @Test
    public void testExactFileNameMatcher()
    {
        // Given
        String aFileName = 'Xxx.java';
        Pattern aPattern = Pattern.compile(aFileName);

        // When
        RuleViolationMatcher aMatcher = RuleViolationMatchers.newMatcher(aPattern);

        // Then
        assertTrue(aMatcher.matches('AnyRule', aFileName, 1));
        assertTrue(aMatcher.matches('AnyRule', aFileName, Integer.MAX_VALUE));
        assertFalse(aMatcher.matches('AnyRule', 'X' + aFileName, 1));
    }


    @Test
    public void testFileNamePatternMatcher()
    {
        // Given
        Pattern aPattern = Pattern.compile('.*Parser.*\\.java');

        // When
        RuleViolationMatcher aMatcher = RuleViolationMatchers.newMatcher(aPattern);

        // Then
        assertTrue(aMatcher.matches('AnyRule', 'MyParser.java', 1));
        assertTrue(aMatcher.matches('AnyRule', 'YourParserSucks.java', Integer.MAX_VALUE));
        assertFalse(aMatcher.matches('AnyRule', 'Parser.groovy', 1));
    }


    @Test
    public void testFileNameAndRuleNamePatternsMatcher()
    {
        // Given
        Pattern aFilePattern = Pattern.compile('.*Parser.*\\.java');
        Pattern aRulePattern = Pattern.compile('.*Stupid.*');

        // When
        RuleViolationMatcher aMatcher = RuleViolationMatchers.newMatcher(aFilePattern, aRulePattern);

        // Then
        assertTrue(aMatcher.matches('StupidRule', 'MyParser.java', 1));
        assertTrue(aMatcher.matches('ReallyStupidRule', 'YourParserSucks.java', Integer.MAX_VALUE));
        assertFalse(aMatcher.matches('StupidRule', 'Parser.groovy', 1));
        assertFalse(aMatcher.matches('GoodRule', 'Parser.java', 1));
        assertFalse(aMatcher.matches('GoodRule', 'Parser.groovy', 1));
    }


    @Test
    public void testFileNamePatternAndExactRuleNameMatcher()
    {
        // Given
        Pattern aFilePattern = Pattern.compile('.*Parser.*\\.java');
        Pattern aRulePattern = Pattern.compile('AnnoyingRule');

        // When
        RuleViolationMatcher aMatcher = RuleViolationMatchers.newMatcher(aFilePattern, aRulePattern);

        // Then
        assertTrue(aMatcher.matches('AnnoyingRule', 'MyParser.java', 1));
        assertTrue(aMatcher.matches('AnnoyingRule', 'YourParserSucks.java', Integer.MAX_VALUE));
        assertFalse(aMatcher.matches('AnnoyingRules', 'Parser.groovy', 1));
        assertFalse(aMatcher.matches('MyAnnoyingRule', 'Parser.java', 1));
    }


    @Test
    public void testFileNameAndLineNumbersMatcher()
    {
        // Given
        Pattern aFilePattern = Pattern.compile('.*Parser.*\\.java');
        BitSet aLineNumbers = new BitSet();
        aLineNumbers.set(2);
        aLineNumbers.set(7);
        aLineNumbers.set(18, 32);

        // When
        RuleViolationMatcher aMatcher = RuleViolationMatchers.newMatcher(aFilePattern, aLineNumbers);

        // Then
        assertTrue(aMatcher.matches('AnyRule', 'MyParser.java', 2));
        assertTrue(aMatcher.matches('AnyRule', 'MyParser.java', 7));
        assertTrue(aMatcher.matches('AnyRule', 'YourParserSucks.java', 18));
        assertTrue(aMatcher.matches('AnyRule', 'YourParserSucks.java', 27));
        assertTrue(aMatcher.matches('AnyRule', 'YourParserSucks.java', 31));
        assertFalse(aMatcher.matches('AnyRule', 'Parser.java', 17));
        assertFalse(aMatcher.matches('AnyRule', 'Parser.java', 32));
        assertFalse(aMatcher.matches('AnyRule', 'Parser.groovy', 2));
        assertFalse(aMatcher.matches('AnyRule', 'Parser.groovy', 1));
    }


    @Test
    public void testFileNameRuleNameAndLineNumbersMatcher()
    {
        // Given
        Pattern aFilePattern = Pattern.compile('.*Parser.*\\.java');
        Pattern aRulePattern = Pattern.compile('.*Stupid.*');
        BitSet aLineNumbers = new BitSet();
        aLineNumbers.set(2);
        aLineNumbers.set(7);
        aLineNumbers.set(18, 32);

        // When
        RuleViolationMatcher aMatcher = RuleViolationMatchers.newMatcher(aFilePattern, aLineNumbers, aRulePattern);

        // Then
        assertTrue(aMatcher.matches('StupidRule', 'MyParser.java', 2));
        assertTrue(aMatcher.matches('StupidRule', 'MyParser.java', 7));
        assertTrue(aMatcher.matches('ReallyStupidRule', 'YourParserSucks.java', 18));
        assertTrue(aMatcher.matches('ReallyStupidRule', 'YourParserSucks.java', 27));
        assertTrue(aMatcher.matches('ReallyStupidRule', 'YourParserSucks.java', 31));

        assertFalse(aMatcher.matches('StupidRule', 'Parser.java', 8));
        assertFalse(aMatcher.matches('StupidRule', 'Parser.groovy', 2));
        assertFalse(aMatcher.matches('GoodRule', 'Parser.java', 18));
        assertFalse(aMatcher.matches('StupidRule', 'Parser.groovy', 32));
        assertFalse(aMatcher.matches('GoodRule', 'Parser.java', 47));
        assertFalse(aMatcher.matches('GoodRule', 'Parser.groovy', 2));
        assertFalse(aMatcher.matches('GoodRule', 'Parser.groovy', 99));
    }
}
