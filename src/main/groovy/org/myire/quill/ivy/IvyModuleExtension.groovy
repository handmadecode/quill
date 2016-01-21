/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy

import org.gradle.api.Project

import org.myire.quill.common.ProjectAware


/**
 * Gradle project extension for specifying an Ivy module descriptor file and optionally an Ivy
 * settings file.
 */
class IvyModuleExtension extends ProjectAware
{
    // Properties accessed through getter and setter only.
    private File fIvyModuleFile;
    private File fIvySettingsFile;


    IvyModuleExtension(Project pProject)
    {
        super(pProject);
        fIvyModuleFile = pProject.file('ivy.xml');
    }


    /**
     * Get the Ivy module descriptor file to load configurations and dependencies from. The default
     * value is a file called &quot;ivy.xml&quot; in the Gradle project directory.
     */
    File getFrom()
    {
        return fIvyModuleFile;
    }


    void setFrom(Object pFile)
    {
        fIvyModuleFile = pFile ? project.file(pFile) : null;
    }


    /**
     * Get the Ivy settings file to use when parsing the module descriptor file. If this file is
     * null, the default settings in the Ivy distribution will be used.
     */
    File getSettings()
    {
        return fIvySettingsFile;
    }


    void setSettings(Object pFile)
    {
        fIvySettingsFile = pFile ? project.file(pFile) : null;
    }
}
