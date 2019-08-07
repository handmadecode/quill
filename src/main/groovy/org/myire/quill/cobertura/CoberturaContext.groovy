/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.SourceSet

import org.myire.quill.common.ProjectAware
import org.myire.quill.common.Projects


/**
 * A Cobertura context holds properties shared by Cobertura instrumentation, test execution and
 * report creation. Each {@code Test} task that is enhanced with Cobertura functionality has an
 * associated context that ensures that the properties common to all three steps are the same and
 * that they can only be configured in one place. Cobertura contexts are added to the project as
 * extensions to allow configuration from the build script. The name of the project extension will
 * be &quot;cobertura&quot; + the name of the context.
 *<p>
 * The properties in a context are to a large extent identical to the ones of the
 * {@code Cobertura-Instrument} and {@code Cobertura-Report} Ant tasks, see the
 * <a href="https://github.com/cobertura/cobertura/wiki/Ant-Task-Reference">reference</> for more
 * information.
 */
class CoberturaContext extends ProjectAware
{
    private final CoberturaExtension fExtension
    private final String fName

    // Properties accessed only through setters and getters.
    private File fWorkDir
    private FileCollection fInputClasses
    private File fInstrumentedClassesDir
    private FileCollection fAuxClassPath
    private File fInstrumentationDataFile
    private File fExecutionDataFile
    private FileCollection fSourceDirs
    private Closure<String> fSourceEncoding
    private Boolean fIgnoreTrivial
    private List<String> fIgnoreMethodNames
    private List<String> fIgnoreMethodAnnotations


    /**
     * Is the context enabled? In a disabled context, the tests are run with the original classes
     * under test, not the instrumented ones, and no coverage report will produced for the test run.
     * Default is {@code true}, meaning that the tests will run with instrumented classes and that a
     * coverage report will be produced.
     */
    boolean enabled = true


    /**
     * Create a new {@code CoberturaContext}.
     *
     * @param pExtension    The global Cobertura project extension with default values for some
     *                      of the context's properties.
     * @param pName         The name of this context, used as default name for the work directory.
     */
    CoberturaContext(CoberturaExtension pExtension, String pName)
    {
        super(pExtension.project);
        fExtension = pExtension;
        fName = pName;

        fSourceEncoding = { pExtension.sourceEncoding };
    }


    /**
     * Get this context's name.
     *
     * @return  The name.
     */
    String getName()
    {
        return fName;
    }


    /**
     * Get the work directory where by default all temporary files, such as instrumented classes and
     * the data file, are put. The default is a directory with the context's name in the global
     * Cobertura project extension's work directory.
     *
     * @return  The work directory.
     */
    File getWorkDir()
    {
        if (fWorkDir == null)
            fWorkDir = new File(fExtension.workDir, fName);

        return fWorkDir;
    }


    /**
     * Set the context's work directory. The specified directory will be resolved relative to the
     * project directory.
     *
     * @param pDirectory    The work directory. Passing null will effectively restore the default
     *                      value.
     */
    void setWorkDir(Object pDirectory)
    {
        fWorkDir = pDirectory ? project.file(pDirectory) : null;
    }


    /**
     * Get the classes to analyze for test coverage. The default value is all files in the output
     * classes directory of the main source set. Used as input by the instrumentation task.
     *
     *  @return The classes to analyze for test coverage.
     */
    FileCollection getInputClasses()
    {
        if (fInputClasses == null)
            fInputClasses = defaultInputClasses(project);

        return fInputClasses;
    }


    /**
     * Set the input classes. The specified classes will be resolved relative to the project
     * directory.
     *
     * @param pClasses  The input classes. Passing null will effectively restore the default value.
     */
    void setInputClasses(Object pClasses)
    {
        fInputClasses = pClasses ? project.files(pClasses) : null;
    }


    /**
     * Get the directory containing the instrumented versions of the classes to analyze. Used as
     * output directory by the instrumentation task and as input directory by the test task. The
     * default value is a directory named &quot;instrumented&quot; in the context's work directory.
     *
     * @return  The directory with the instrumented classes.
     */
    File getInstrumentedClassesDir()
    {
        if (fInstrumentedClassesDir == null)
            fInstrumentedClassesDir = new File(getWorkDir(), 'instrumented');

        return fInstrumentedClassesDir;
    }


    /**
     * Set the instrumented classes directory. The specified directory will be resolved relative to
     * the project directory.
     *
     * @param pDirectory    The instrumented classes directory. Passing null will effectively
     *                      restore the default value.
     */
    void setInstrumentedClassesDir(Object pDirectory)
    {
        fInstrumentedClassesDir = pDirectory ? project.file(pDirectory) : null;
    }


    /**
     * Get the path containing any classes that shouldn't be analyzed but are needed by the
     * instrumentation. Default is no auxiliary classpath.
     *
     * @return  The  auxiliary class path.
     */
    FileCollection getAuxClassPath()
    {
        return fAuxClassPath;
    }


    /**
     * Set the auxiliary classpath. The specified classpath will be resolved relative to the project
     * directory.
     *
     * @param pClassPath    The auxiliary classpath, possibly null.
     */
    void setAuxClassPath(Object pClassPath)
    {
        fAuxClassPath = pClassPath ? project.files(pClassPath) : null;
    }


    /**
     * Get the file holding metadata about the instrumented classes. This file contains information
     * about the names of the classes, their method names, line numbers, etc. It is created by the
     * instrumentation task. The default value is a file named
     * &quot;cobertura.instrumentation.ser&quot; in the context's work directory.
     *
     * @return  The instrumentation data file.
     */
    File getInstrumentationDataFile()
    {
        if (fInstrumentationDataFile == null)
            fInstrumentationDataFile = new File(getWorkDir(), 'cobertura.instrumentation.ser');

        return fInstrumentationDataFile;
    }


    /**
     * Set the instrumentation data file. The specified file will be resolved relative to the
     * project directory.
     *
     * @param pFile The instrumentation data. Passing null will effectively restore the default
     *              value.
     */
    void setInstrumentationDataFile(Object pFile)
    {
        fInstrumentationDataFile = pFile ? project.file(pFile) : null;
    }


    /**
     * Get the file holding metadata about the test execution of the instrumented classes. This file
     * contains updated information about the instrumented classes from the test runs. It is an
     * updated version of the instrumentation data file, created by the test task and used by the
     * reports task. The default value is a file named &quot;cobertura.execution.ser&quot; in the
     * context's work directory.
     *
     * @return  The execution data file.
     */
    File getExecutionDataFile()
    {
        if (fExecutionDataFile == null)
            fExecutionDataFile = new File(getWorkDir(), 'cobertura.execution.ser');

        return fExecutionDataFile;
    }


    /**
     * Set the execution data file. The specified file will be resolved relative to the project
     * directory.
     *
     * @param pFile The execution data. Passing null will effectively restore the default value.
     */
    void setExecutionDataFile(Object pFile)
    {
        fExecutionDataFile = pFile ? project.file(pFile) : null;
    }


    /**
     * Get the directories containing the sources of the analyzed classes.
     *
     * @return  The source file directories.
     */
    FileCollection getSourceDirs()
    {
        if (fSourceDirs == null)
            fSourceDirs = defaultSourceDirs(project);

        return fSourceDirs;
    }


    /**
     * Set the sources directory. The specified directory will be resolved relative to the project
     * directory.
     *
     * @param pDirectory    The sources directory. Passing null will effectively restore the default
     *                      value.
     */
    void setSourceDirs(Object pDirs)
    {
        fSourceDirs = pDirs ? project.files(pDirs) : null;
    }


    /**
     * Get the encoding to use when reading the source files in the {@code sourceDirs}. The
     * platform's default encoding will be used if this property is null. Default is the value from
     * the corresponding property in the global Cobertura project extension.
     *
     * @return  The source encoding, possibly null.
     */
    String getSourceEncoding()
    {
        return fSourceEncoding ? fSourceEncoding.call() : null;
    }


    /**
     * Set the source encoding.
     *
     * @param pClassPath    The source encoding. Specifying null means that the platform's default
     *                      encoding should be used..
     */
    void setSourceEncoding(String pEncoding)
    {
        fSourceEncoding = pEncoding ? { pEncoding } : null;
    }


    /**
     * Get the ignore trivial property. If this property is true, constructors/methods that contain
     * one line of code will be excluded from the overage analysis. Examples include constructors
     * only calling a super constructor, and getters/setters. Default is the value of the
     * corresponding property in the global Cobertura project extension.
     *
     * @return True if trivial methods should be ignored, false if not.
     */
    boolean getIgnoreTrivial()
    {
        if (fIgnoreTrivial == null)
            fIgnoreTrivial = Boolean.valueOf(fExtension.ignoreTrivial);

        return fIgnoreTrivial.booleanValue();
    }


    void setIgnoreTrivial(boolean pDoIgnore)
    {
        fIgnoreTrivial = Boolean.valueOf(pDoIgnore);
    }


    /**
     * Get the list of regular expressions specifying methods names to be excluded from the coverage
     * analysis. Note that the classes containing the methods will still be instrumented. Default is
     * is the value of the corresponding property in the global Cobertura project extension.
     *
     * @return  The list of "ignore method" names regular expressions.
     */
    List<String> getIgnoreMethodNames()
    {
        if (fIgnoreMethodNames == null)
            fIgnoreMethodNames = fExtension.ignoreMethodNames;

        return fIgnoreMethodNames;
    }


    void setIgnoreMethodNames(List<String> pIgnoreMethodsNames)
    {
        fIgnoreMethodNames = pIgnoreMethodsNames;
    }


    /**
     * Get the  list of fully qualified names of annotations with which methods that should be
     * excluded from instrumentation are annotated with. Default is the value of the corresponding
     * property in the global Cobertura project extension.
     *
     * @return  The list of "ignore method" annotation names.
     */
    List<String> getIgnoreMethodAnnotations()
    {
        if (fIgnoreMethodAnnotations == null)
            fIgnoreMethodAnnotations = fExtension.ignoreMethodAnnotations;

        return fIgnoreMethodAnnotations;
    }


    void setIgnoreMethodAnnotations(List<String> pIgnoreMethodsAnnotations)
    {
        fIgnoreMethodAnnotations = pIgnoreMethodsAnnotations;
    }


    /**
     * Get the default collection of classes to analyze for test coverage.
     *
     * @param pProject  The project to get the default input classes from.
     *
     * @return  The project's main source set's output classes.
     */
    static FileCollection defaultInputClasses(Project pProject)
    {
        return Projects.getSourceSet(pProject, SourceSet.MAIN_SOURCE_SET_NAME)?.output?.classesDirs;
    }


    /**
     * Get the default source directories for the classes to analyze for test coverage.
     *
     * @param pProject  The project to get the default source directories from.
     *
     * @return  The project's main source set's sources.
     */
    static FileCollection defaultSourceDirs(Project pProject)
    {
        return pProject.files(Projects.getSourceSet(pProject, SourceSet.MAIN_SOURCE_SET_NAME)?.allSource?.srcDirs);
    }
}
