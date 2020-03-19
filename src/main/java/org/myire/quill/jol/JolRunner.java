/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import java.util.Collection;


/**
 * A {@code JolRunner} analyzes the layout of objects specified in a collection of class files using
 * the Jol library.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public interface JolRunner
{
    /**
     * Initialize this instance.
     *
     * @param pToolVersion  The version of Jol this instance is running.
     */
    void init(String pToolVersion);

    /**
     * Analyze a collection of classes and report their object layout.
     *
     * @param pClasses      The fully qualified names of the classes to analyze.
     * @param pParameters   The analysis parameters.
     *
     * @return  A new {@code JolResult} with the analysis result, never null.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    JolResult analyze(Collection<String> pClasses, JolParameters pParameters);
}
