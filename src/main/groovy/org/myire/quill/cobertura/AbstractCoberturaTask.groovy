/*
 * Copyright 2014-2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.file.FileCollection
import org.gradle.api.internal.AbstractTask
import org.gradle.api.tasks.InputFiles


/**
 * Abstract base class for Cobertura tasks.
 */
abstract class AbstractCoberturaTask extends AbstractTask
{
    private CoberturaExtension fExtension
	private CoberturaContext fContext


    /**
     * Initialize this task.
     *
     * @param pExtension    The extension holding default values for the task.
     * @param pContext      The context in which the task should execute.
     */
    void init(CoberturaExtension pExtension, CoberturaContext pContext)
    {
        fExtension = pExtension;
        fContext = pContext;

        // Only execute a Cobertura task if its context is enabled.
        onlyIf { pContext.enabled };
    }


    /**
     * Get the extension holding default values for the task.
     */
    CoberturaExtension getExtension()
    {
        return fExtension;
    }


    /**
     * Get the context in which the task should execute.
     */
    CoberturaContext getContext()
    {
        return fContext;
    }


    /**
	 * Get the class path containing the Cobertura classes used by the task.
	 *
	 * @return	The Cobertura class path, never null.
	 */
	@InputFiles
	FileCollection getCoberturaClassPath()
	{
		return fExtension.getCoberturaClassPath();
	}
}
