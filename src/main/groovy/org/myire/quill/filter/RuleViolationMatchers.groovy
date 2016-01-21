/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.filter

import java.util.regex.Pattern


/**
 * Factory methods for creating rule violation matchers.
 */
final class RuleViolationMatchers
{
    /**
     * Create a new {@code RuleViolationMatcher} that matches any rule violation anywhere in files
     * with names that match a pattern.
     *
     * @param pFilesPattern The file name pattern to match.
     *
     * @return  A new {@code RuleViolationMatcher}.
     */
    static RuleViolationMatcher newMatcher(Pattern pFilesPattern)
    {
        return { ruleName, fileName, lineNumber ->
                    pFilesPattern.matcher(fileName).matches() } as RuleViolationMatcher;
    }


    /**
     * Create a new {@code RuleViolationMatcher} that matches any rule violation in certain line
     * numbers in files with names that match a pattern.
     *
     * @param pFilesPattern The file name pattern to match.
     * @param pLineNumbers  The line numbers to match.
     *
     * @return  A new {@code RuleViolationMatcher}.
     */
    static RuleViolationMatcher newMatcher(Pattern pFilesPattern, BitSet pLineNumbers)
    {
        return { ruleName, fileName, lineNumber ->
                    pFilesPattern.matcher(fileName).matches() &&
                    pLineNumbers.get(lineNumber) } as RuleViolationMatcher;
    }


    /**
     * Create a new {@code RuleViolationMatcher} that matches violations of rules with names that
     * match a pattern anywhere in files with names that match another pattern.
     *
     * @param pFilesPattern The file name pattern to match.
     * @param pRulesPattern The rule name pattern to match.
     *
     * @return  A new {@code RuleViolationMatcher}.
     */
    static RuleViolationMatcher newMatcher(Pattern pFilesPattern, Pattern pRulesPattern)
    {
        return { ruleName, fileName, lineNumber ->
                    pFilesPattern.matcher(fileName).matches() &&
                    pRulesPattern.matcher(ruleName).matches() } as RuleViolationMatcher;
    }


    /**
     * Create a new {@code RuleViolationMatcher} that matches violations of a rule in a certain
     * line number range in a file.
     *
     * @param pFilesPattern The file name pattern to match.
     * @param pLineNumbers  The line numbers to match.
     * @param pRulesPattern The rule name pattern to match.
     *
     * @return  A new {@code RuleViolationMatcher}.
     */
    static RuleViolationMatcher newMatcher(Pattern pFilesPattern, BitSet pLineNumbers, Pattern pRulesPattern)
    {
        return { ruleName, fileName, lineNumber ->
                    pFilesPattern.matcher(fileName).matches() &&
                    pLineNumbers.get(lineNumber) &&
                    pRulesPattern.matcher(ruleName).matches() } as RuleViolationMatcher;
    }
}
