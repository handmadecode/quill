/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.pom

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.MavenPlugin

import org.myire.quill.common.Projects


/**
 * Gradle plugin for creating pom files outside the context of uploading to a Maven repo. The plugin
 * adds a task called &quot;createPom&quot; when applied to a project.
 */
class PomPlugin implements Plugin<Project>
{
    static final String TASK_NAME = 'createPom'


    @Override
    void apply(Project pProject)
    {
        // Make sure the Maven plugin is applied.
        pProject.plugins.apply(MavenPlugin.class);

        // Create the pom file task.
        PomFileTask aTask = pProject.tasks.create(TASK_NAME, PomFileTask.class);
        aTask.description = 'Create a stand-alone pom file';
        aTask.init();

        // Trigger the pom file task when the build task has executed.
        Projects.getTask(pProject, 'build', Task.class)?.finalizedBy(aTask);
    }
}
