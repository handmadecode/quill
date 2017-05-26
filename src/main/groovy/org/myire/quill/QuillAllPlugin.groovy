/*
 * Copyright 2015-2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.myire.quill.ivy.IvyPlugin
import org.myire.quill.maven.MavenImportPlugin


/**
 * Gradle project plugin that applies all Quill plugins.
 */
class QuillAllPlugin implements Plugin<Project>
{
    @Override
    void apply(Project pProject)
    {
        // Apply all Quill plugins to the project.
        pProject.plugins.apply(QuillCorePlugin.class);
        pProject.plugins.apply(IvyPlugin.class);
        pProject.plugins.apply(MavenImportPlugin.class);
    }
}
