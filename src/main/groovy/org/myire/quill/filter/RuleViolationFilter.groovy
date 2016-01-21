/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.filter

/**
 * A rule violation filter uses a collection of {@code RuleViolationMatcher} instances to filter
 * rule violations. If any of a filter's matchers matches a violation, the entire filter matches the
 * violation, which means it should be filtered out.
 */
class RuleViolationFilter implements RuleViolationMatcher
{
    private final Collection<RuleViolationMatcher> fMatchers = [];


    /**
     * Add a rule violation matcher to this filter.
     *
     * @param pMatcher  The matcher to add.
     */
    void addMatcher(RuleViolationMatcher pMatcher)
    {
        fMatchers.add pMatcher;
    }


    @Override
    boolean matches(String pRuleName, String pFileName, int pLineNumber)
    {
        return fMatchers.find { it.matches(pRuleName, pFileName, pLineNumber) } != null;
    }
}
