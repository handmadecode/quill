/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common

import org.gradle.api.Task


/**
 * Task related utility methods.
 */
final class Tasks
{
    /**
     * Specify a file as input to a task, and mark it as an optional input if supported by the
     * runtime Gradle version.
     *
     * @param pTask The task.
     * @param pFile The input file.
     */
    static void setOptionalInputFile(Task pTask, Object pFile)
    {
        def aInputs = pTask.inputs.file(pFile);
        if (aInputs.metaClass.respondsTo(aInputs, "optional", boolean.class))
            aInputs.optional(true);
    }
}
