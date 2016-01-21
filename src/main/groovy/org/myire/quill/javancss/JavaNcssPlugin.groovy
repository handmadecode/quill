/*
 * Copyright 2014-2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.javancss

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

import org.myire.quill.common.Projects


/**
 * Gradle plugin for adding a NCSS counting task based on JavaNCSS to a project. The plugin also
 * creates a configuration that specifies the classpath used when running the JavaNCSS task.
 */
class JavaNcssPlugin implements Plugin<Project>
{
    static final String TASK_NAME = 'javancss'
    static final String CONFIGURATION_NAME = 'javancss'

    static private final String JAVANCSS_GROUP_ARTIFACT_ID = "org.codehaus.javancss:javancss"

    private Project fProject
    private JavaNcssTask fTask
    private Configuration fConfiguration


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Create the JavaNCSS configuration and add it to the project. The JavaNCSS classpath is
        // specified through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Create the task.
        fTask = createTask();
    }


    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.configurations.maybeCreate(CONFIGURATION_NAME);

        aConfiguration.with {
            visible = false;
            transitive = true;
            description = 'The JavaNCSS classes used by the Gradle tasks';
        }

        aConfiguration.incoming.beforeResolve {
            // If no dependencies are explicitly declared, a dependency to the JavaNCSS artifact
            // with the version specified in the extension property 'toolVersion' of the JavaNCSS
            // task is added.
            if (aConfiguration.dependencies.empty)
            {
                String aID = "${JAVANCSS_GROUP_ARTIFACT_ID}:${fTask.toolVersion}";
                aConfiguration.dependencies.add(fProject.dependencies.create(aID));
            }
        }

        return aConfiguration;
    }


    private JavaNcssTask createTask()
    {
        JavaNcssTask aTask = fProject.tasks.create(TASK_NAME, JavaNcssTask.class);
        aTask.description = 'Runs JavaNCSS to creates a report with ncss statistics';
        aTask.javancssClasspath = fConfiguration;
        aTask.setupReports();
        aTask.addUpToDateCheck();

        // Add the task to the build task's dependencies.
        Projects.getTask(fProject, 'build', Task.class)?.dependsOn(aTask);

        return aTask;
    }
}
