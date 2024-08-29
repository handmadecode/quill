/*
 * Copyright 2015, 2019, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.JavaBasePlugin;

import org.myire.quill.common.Projects;


/**
 * Gradle plugin for adding a copy-paste detection task based on CPD to a project. The plugin also
 * creates a configuration that specifies the classpath used when running the CPD task.
 */
public class CpdPlugin implements Plugin<Project>
{
    static private final String TASK_NAME = "cpd";
    static private final String CONFIGURATION_NAME = "cpd";

    // The group and artifact IDs of the CPD/PMD dependency.
    static private final String CPD_GROUP_ARTIFACT_ID = "net.sourceforge.pmd:pmd-dist";


    private Project fProject;
    private CpdTask fTask;
    private Configuration fConfiguration;


    @Override
    public void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the Java base plugin is applied. This will create the check task, and if that
        // task is available when the cpd task is created, the former will be configured to depend
        // on the latter, which will trigger the execution of cpd when running the check task.
        pProject.getPlugins().apply(JavaBasePlugin.class);

        // Create the CPD configuration and add it to the project. The CPD classpath is specified
        // through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Create the task.
        fTask = createCpdTask();
    }


    /**
     * Create the CPD configuration and define it to depend on the default CPD artifacts unless
     * explicit dependencies have been defined.
     *
     * @return  The created configuration.
     */
    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.getConfigurations().maybeCreate(CONFIGURATION_NAME);

        aConfiguration.setVisible(false);
        aConfiguration.setTransitive(true);
        aConfiguration.setDescription("The CPD classes used by the CPD task");

        // Add an action that adds a default dependency on the PMD/CPD artifact with the version
        // specified by the task's toolVersion property.
        aConfiguration.defaultDependencies(this::addDefaultDependency);

        return aConfiguration;
    }


    /**
     * Add a dependency on the PMD/CPD artifact to a {@code DependencySet}. The artifact's version
     * will be taken from the {@code cpd} task's {@code toolVersion} property.
     *
     * @param pDependencies The dependency set to add the default dependency to.
     */
    private void addDefaultDependency(DependencySet pDependencies)
    {
        String aID = CPD_GROUP_ARTIFACT_ID + ':' + fTask.getToolVersion();
        pDependencies.add(fProject.getDependencies().create(aID));
    }


    /**
     * Create a CPD task.
     *
     * @return  The created task.
     */
    private CpdTask createCpdTask()
    {
        CpdTask aTask = fProject.getTasks().create(TASK_NAME, CpdTask.class);
        aTask.setDescription("Performs copy-paste detection on the main source files");
        aTask.setGroup("verification");
        aTask.setCpdClasspath(fConfiguration);
        aTask.setupReports();
        aTask.addUpToDateCheck();

        // Add the CPD task to the check task's dependencies.
        Task aCheckTask = Projects.getTask(fProject, "check", Task.class);
        if (aCheckTask != null)
            aCheckTask.dependsOn(aTask);

        return aTask;
    }
}
