/*
 * Copyright 2020, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jol;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.JavaPlugin;

import org.myire.quill.common.Projects;


/**
 * Gradle plugin for adding an Object Layout analysis task based on the Jol tool to a project. The
 * plugin also creates a configuration that specifies the tool classpath to use when running the Jol
 * task.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class JolPlugin implements Plugin<Project>
{
    static private final String TASK_NAME = "jol";
    static private final String CONFIGURATION_NAME = "jol";

    static private final String JOL_GROUP_ARTIFACT_ID = "org.openjdk.jol:jol-core";


    private Project fProject;
    private JolTask fTask;
    private Configuration fConfiguration;


    @Override
    public void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the Java plugin is applied. This will create the main source set, which is the
        // default source set to operate on for the Jol task. It will also create the build task; if
        // that task is available when the Jol task is created, the former will be configured to
        // depend on the latter, which will trigger the execution of the Jol task when running the
        // build task.
        pProject.getPlugins().apply(JavaPlugin.class);

        // Create the Jol configuration and add it to the project. The classpath to load the Jol
        // tool from is specified through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Create the Jol task.
        fTask = createTask();
    }


    /**
     * Create the Jol configuration if not already present in the project and define it to depend
     * on the default Jol artifact unless explicit dependencies have been defined.
     *
     * @return  The Jol configuration.
     */
    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.getConfigurations().maybeCreate(CONFIGURATION_NAME);

        aConfiguration.setVisible(false);
        aConfiguration.setTransitive(true);
        aConfiguration.setDescription("The Jol classes used by the Jol task");

        // Add an action that adds a default dependency on the Jol artifact with the version
        // specified by the task's toolVersion property.
        aConfiguration.defaultDependencies(this::addDefaultDependency);

        return aConfiguration;
    }


    /**
     * Add a dependency on the {@code jol-core} artifact to a {@code DependencySet}. The artifact's
     * version will be taken from the {@code jol} task's {@code toolVersion} property.
     *
     * @param pDependencies The dependency set to add the default dependency to.
     */
    private void addDefaultDependency(DependencySet pDependencies)
    {
        String aID = JOL_GROUP_ARTIFACT_ID + ':' + fTask.getToolVersion();
        pDependencies.add(fProject.getDependencies().create(aID));
    }


    /**
     * Create a new {@code JolTask}. The {@code build} task will depend on this new task.
     *
     * @return  The created {@code JolTask} instance.
     */
    private JolTask createTask()
    {
        JolTask aTask = fProject.getTasks().create(TASK_NAME, JolTask.class);
        aTask.setDescription("Runs Jol to create an object layout report");
        aTask.setToolClassPath(fConfiguration);
        aTask.setupReports();
        fProject.afterEvaluate(this::finalizeTaskConfiguration);

        // Add the Jol task to the build task's dependencies.
        Task aBuildTask = Projects.getTask(fProject, "build", Task.class);
        if (aBuildTask != null)
            aBuildTask.dependsOn(aTask);

        return aTask;
    }


    /**
     * Configure the jol task with the default classes to analyze if no explicit classes were
     * configured.
     *
     * @param pProject  The project the task is executing in.
     */
    private void finalizeTaskConfiguration(Project pProject)
    {
        fTask.maybeConfigureDefaultClasses();
    }
}
