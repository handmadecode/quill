/*
 * Copyright 2016, 2018, 2020, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.scent;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.JavaBasePlugin;

import org.myire.quill.common.Projects;


/**
 * Gradle plugin for adding a source code metrics task based on Scent to a project. The plugin also
 * creates a configuration that specifies the classpath to use when running the Scent task.
 */
public class ScentPlugin implements Plugin<Project>
{
    static private final String TASK_NAME = "scent";
    static private final String CONFIGURATION_NAME = "scent";

    static private final String SCENT_GROUP_ARTIFACT_ID = "org.myire:scent";


    private Project fProject;
    private ScentTask fTask;
    private Configuration fConfiguration;


    @Override
    public void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the Java base plugin is applied. This will create the build task, and if that
        // task is available when the scent task is created, the former will be configured to depend
        // on the latter, which will trigger the execution of the scent task when running the build
        // task.
        pProject.getPlugins().apply(JavaBasePlugin.class);

        // Create the scent configuration and add it to the project. The scent classpath is
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
        Configuration aConfiguration = fProject.getConfigurations().maybeCreate(CONFIGURATION_NAME);

        aConfiguration.setVisible(false);
        aConfiguration.setTransitive(true);
        aConfiguration.setDescription("The Scent classes used by the Gradle tasks");

        // Add an action that adds a default dependency on the Scent artifact with the version
        // specified by the task's toolVersion property.
        aConfiguration.defaultDependencies(this::addDefaultDependency);

        return aConfiguration;
    }


    /**
     * Add a dependency on the {@code scent} artifact to a {@code DependencySet}. The artifact's
     * version will be taken from the {@code scent} task's {@code toolVersion} property.
     *
     * @param pDependencies The dependency set to add the default dependency to.
     */
    private void addDefaultDependency(DependencySet pDependencies)
    {
        String aID = SCENT_GROUP_ARTIFACT_ID + ':' + fTask.getToolVersion();
        pDependencies.add(fProject.getDependencies().create(aID));
    }


    /**
     * Create a new {@code ScentTask}. The {@code build} task will depend on this new task.
     *
     * @return  The created {@code ScentTask} instance.
     */
    private ScentTask createTask()
    {
        ScentTask aTask = fProject.getTasks().create(TASK_NAME, ScentTask.class);
        aTask.setDescription("Runs Scent to create a source code metrics report");
        aTask.setScentClasspath(fConfiguration);
        aTask.setupReports();

        // Add the Scent task to the build task's dependencies.
        Task aBuildTask = Projects.getTask(fProject, "build", Task.class);
        if (aBuildTask != null)
            aBuildTask.dependsOn(aTask);

        return aTask;
    }
}
