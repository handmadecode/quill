/*
 * Copyright 2014-2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package org.myire.quill.cobertura

import org.gradle.api.Project
import org.gradle.api.file.FileCollection

import org.myire.quill.common.ProjectAware
import org.myire.quill.common.Projects


/**
 * Project extension used to set properties for the Cobertura tasks on a global basis.
 *<p>
 * See <a href="https://github.com/cobertura/cobertura/wiki/Ant-Task-Reference">the Cobertura Ant
 * task reference</a> for more documentation on these properties.
 */
class CoberturaExtension extends ProjectAware
{
    static private final String DEFAULT_TOOL_VERSION = "2.1.1"


    // Properties accessed through getter and setter only.
    private File fWorkDir
    private FileCollection fCoberturaClassPath


    /**
     * The version of Cobertura to use. Default is &quot;2.1.1&quot;
     */
    String toolVersion

    /**
     * If true, constructors/methods that contain one line of code will be excluded from the
     * coverage analysis. Examples include constructors only calling a super constructor, and
     * getters/setters. Default is false.
     */
    boolean ignoreTrivial

    /**
     * A list of regular expressions specifying methods names to be excluded from the coverage
     * analysis. Note that the classes containing the methods will still be instrumented. Default is
     * an empty list.
     */
    List<String> ignoreMethodNames

    /**
     * A list of fully qualified names of annotations with which methods that should be excluded
     * from instrumentation are annotated with. Default is an empty list.
     */
    List<String> ignoreMethodAnnotations

    /**
     * The encoding used by Cobertura to read the source files. The platform's default encoding will
     * be used if this property isn't specified.
     */
    String sourceEncoding


    /**
     * Create a new {@code CoberturaExtension}.
     *
     * @param pProject The project this extension belongs to.
     */
    CoberturaExtension(Project pProject)
    {
        super(pProject);
    }


    String getToolVersion()
    {
        return toolVersion ?: DEFAULT_TOOL_VERSION;
    }


    List<String> getIgnoreMethodNames()
    {
        return ignoreMethodNames ?: [];
    }


    List<String> getIgnoreMethodAnnotations()
    {
        return ignoreMethodAnnotations ?: [];
    }


    /**
     * Get the work directory where by default all temporary files, such as instrumented classes and
     * the data file, are put. The default is a directory called "cobertura" in the project's
     * temporary directory.
     *
     * @return The work directory.
     */
    File getWorkDir()
    {
        if (fWorkDir == null)
            fWorkDir = Projects.createTemporaryDirectorySpec(project, 'cobertura');

        return fWorkDir;
    }


    /**
     * Set the work directory. The specified directory will be resolved relative to the project
     * directory.
     *
     * @param pDirectory    The work directory. Passing null will effectively restore the default
     *                      value.
     */
    void setWorkDir(Object pDirectory)
    {
        fWorkDir = pDirectory ? project.file(pDirectory) : null;
    }


    /**
     * Get the class path containing the Cobertura classes. Default is the {@code cobertura}
     * configuration.
     *
     * @return The Cobertura class path.
     */
    FileCollection getCoberturaClassPath()
    {
        if (fCoberturaClassPath == null)
            fCoberturaClassPath = project.configurations.getByName(CoberturaPlugin.CONFIGURATION_NAME);

        return fCoberturaClassPath;
    }


    /**
     * Set the Cobertura classpath. The specified classpath will be resolved relative to the project
     * directory.
     *
     * @param pClassPath    The classpath. Passing null will effectively restore the default value.
     */
    void setCoberturaClassPath(Object pClassPath)
    {
        fCoberturaClassPath = pClassPath ? project.files(pClassPath) : null;
    }
}
