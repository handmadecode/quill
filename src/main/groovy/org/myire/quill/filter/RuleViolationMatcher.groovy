/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.filter

/**
 * A matcher for rule violations in files. Rule violations are described by the name of the violated
 * rule, the name of the file where the violation was found,  and the line number in the file where
 * the violation was found (or, in case of a multi-line violation, where the violation starts).
 */
interface RuleViolationMatcher
{
    /**
     * Check if this matcher matches a rule violation described as a tuple of <i>rule name - file
     * name - line number</i>.
     *
     * @param pRuleName     The rule name to check for a match.
     * @param pFileName     The file name to check for a match.
     * @param pLineNumber   The line number to check for a match.
     *
     * @return  True if this matcher matches the specified tuple,  false if not.
     */
    boolean matches(String pRuleName, String pFileName, int pLineNumber);
}
