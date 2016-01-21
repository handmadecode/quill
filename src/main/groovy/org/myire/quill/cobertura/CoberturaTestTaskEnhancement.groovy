/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.util.GFileUtils


/**
 * An enhancement to a {@code Test} task that adds Cobertura code coverage functionality.
 */
class CoberturaTestTaskEnhancement
{
    // The system property used by Cobertura to specify the data file with meta data about the
    // instrumented classes.
    static private final String SYSTEM_PROPERTY_DATA_FILE = 'net.sourceforge.cobertura.datafile'


    private final Test fTask
    private final CoberturaExtension fExtension
    private final CoberturaContext fContext

    private FileCollection fOriginalClasspath
    private Object fOriginalSystemPropertyDataFile


    /**
     * Create a new {@code CoberturaTestEnhancement}.
     *
     * @param pTask         The task to enhance.
     * @param pExtension    The project's cobertura extension.
     * @param pContext      The context in which the enhanced test execution runs.
     */
    CoberturaTestTaskEnhancement(Test pTask, CoberturaExtension pExtension, CoberturaContext pContext)
    {
        fTask = pTask;
        fExtension = pExtension;
        fContext = pContext;
    }


    /**
     * Prepare the test task for code coverage analysis during test execution.
     */
    void beforeTestExecution()
    {
        if (!fContext.enabled)
            return;

        // Put the Cobertura instrumented classes and the Cobertura jars first in the classpath of
        // the forked test JVM. Keep the original classes in the classpath, as all of them may not
        // be instrumented due to classes being configured for instrumentation exclusion.
        FileCollection aClasspath = fTask.project.files(fContext.instrumentedClassesDir);
        aClasspath += fExtension.coberturaClassPath;
        fOriginalClasspath = fTask.classpath;
        aClasspath += fOriginalClasspath;
        fTask.classpath = aClasspath;

        // Copy the instrumentation data file to the execution data file. This is done to keep the
        // instrumentation data file unmodified by the test execution, otherwise the instrumentation
        // task is not able to determine if its output has been modified or not since the last run.
        GFileUtils.copyFile(fContext.instrumentationDataFile, fContext.executionDataFile);

        // The Cobertura data file should be defined in a system property of the forked test JVM.
        fOriginalSystemPropertyDataFile = fTask.systemProperties?.get(SYSTEM_PROPERTY_DATA_FILE);
        fTask.systemProperty(SYSTEM_PROPERTY_DATA_FILE, fContext.executionDataFile.absolutePath);
    }


    /**
     * Restore the test task from code coverage analysis during test execution.
     */
    void afterTestExecution()
    {
        if (!fContext.enabled)
            return;

        // Restore the original classpath and system properties as they affect the up-to-date check.
        fTask.classpath = fOriginalClasspath;
        if (fOriginalSystemPropertyDataFile != null)
            fTask.systemProperty(SYSTEM_PROPERTY_DATA_FILE, fOriginalSystemPropertyDataFile);
        else
            fTask.systemProperties.remove(SYSTEM_PROPERTY_DATA_FILE);
    }
}
