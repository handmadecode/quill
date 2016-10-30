/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration

import org.myire.quill.common.Projects


/**
 * Gradle plugin for adding a source code metrics task based on Scent to a project. The plugin also
 * creates a configuration that specifies the classpath to use when running the Scent task.
 */
class ScentPlugin implements Plugin<Project>
{
    static final String TASK_NAME = 'scent'
    static final String CONFIGURATION_NAME = 'scent'

    static private final String SCENT_GROUP_ARTIFACT_ID = "org.myire:scent"

    private Project fProject
    private ScentTask fTask
    private Configuration fConfiguration


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Create the Scent configuration and add it to the project. The Scent classpath is
        // specified through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Create the task.
        fTask = createTask();
    }


    /**
     * Create the Scent configuration if not already present in the project and define it to depend
     * on the default Scent artifact unless explicit dependencies have been defined.
     *
     * @return  The Scent configuration.
     */
    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.configurations.maybeCreate(CONFIGURATION_NAME);

        aConfiguration.with {
            visible = false;
            transitive = true;
            description = 'The Scent classes used by the Gradle tasks';
        }

        aConfiguration.incoming.beforeResolve {
            // If no dependencies are explicitly declared, a dependency on the Scent artifact with
            // the version specified in the Scent task property 'toolVersion' is added.
            if (aConfiguration.dependencies.empty)
            {
                String aID = "${SCENT_GROUP_ARTIFACT_ID}:${fTask.toolVersion}";
                aConfiguration.dependencies.add(fProject.dependencies.create(aID));
            }
        }

        return aConfiguration;
    }


    /**
     * Create a new {@code ScentTask}. The {@code build} task will depend on this new task.
     *
     * @return  The created {@code ScentTask} instance.
     */
    private ScentTask createTask()
    {
        ScentTask aTask = fProject.tasks.create(TASK_NAME, ScentTask.class);
        aTask.description = 'Runs Scent to create a source code metrics report';
        aTask.scentClasspath = fConfiguration;
        aTask.setupReports();
        aTask.addUpToDateCheck();

        // Add the task to the build task's dependencies.
        Projects.getTask(fProject, 'build', Task.class)?.dependsOn(aTask);

        return aTask;
    }
}
