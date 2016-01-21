/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Project

import org.myire.quill.common.ProjectAware


/**
 * Wrapper class for specifying JDepend properties.
 */
class JDependProperties extends ProjectAware
{
    private Closure<File> fFile;


    /**
     * Create a new {@code JDependProperties}.
     *
     * @param pProject  The project to use when resolving properties file locations.
     * @param pFile     A closure that returns the default value for the file property.
     */
    JDependProperties(Project pProject, Closure<File> pFile)
    {
        super(pProject);
        fFile = pFile;
    }


    /**
     * Get the JDepend properties file to add to the classpath.
     */
    File getFile()
    {
        return fFile?.call();
    }


    void setFile(Object pFile)
    {
        fFile = pFile ? { project.file(pFile) } : null;
    }
}
