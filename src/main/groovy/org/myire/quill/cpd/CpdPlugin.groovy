/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.util.VersionNumber

import org.myire.quill.common.Projects


/**
 * Gradle plugin for adding a copy-paste detection task based on CPD to a project. The plugin also
 * creates a configuration that specifies the classpath used when running the CPD task.
 */
class CpdPlugin implements Plugin<Project>
{
    static final String TASK_NAME = 'cpd'
    static final String CONFIGURATION_NAME = 'cpd'

    // The group and artifact IDs of the CPD/PMD dependency have changed on two occasions.
    static private final String CPD_GROUP_ARTIFACT_ID_GEN1 = "pmd:pmd"
    static private final String CPD_GROUP_ARTIFACT_ID_GEN2 = "net.sourceforge.pmd:pmd"
    static private final String CPD_GROUP_ARTIFACT_ID_GEN3 = "net.sourceforge.pmd:pmd-dist"

    // The CPD/PMD version where the group and artifact IDs were changed.
    static private final VersionNumber VERSION_CPD_GROUP_ARTIFACT_ID_GEN2 = VersionNumber.parse('5.0')
    static private final VersionNumber VERSION_CPD_GROUP_ARTIFACT_ID_GEN3 = VersionNumber.parse('5.2')


    private Project fProject
    private CpdTask fTask;
    private Configuration fConfiguration


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

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
        Configuration aConfiguration = fProject.configurations.maybeCreate(CONFIGURATION_NAME)

        aConfiguration.with {
            visible = false;
            transitive = true;
            description = 'The CPD classes used by the CPD plugin tasks';
        }

        aConfiguration.incoming.beforeResolve {
            // If no dependencies are explicitly declared, a dependency on the CPD artifact with the
            // version specified in the CPD task property 'toolVersion' is added.
            if (aConfiguration.dependencies.empty)
            {
                // Check the CPD version number to determine which group and artifact ID to use.
                VersionNumber aVersion = VersionNumber.parse(fTask.toolVersion);
                String aID = "${getGroupAndArtifactID(aVersion)}:${fTask.toolVersion}";
                aConfiguration.dependencies.add(fProject.dependencies.create(aID));
            }
        }

        return aConfiguration;
    }


    /**
     * Create a CPD task.
     *
     * @return  The created task.
     */
    private CpdTask createCpdTask()
    {
        CpdTask aTask = fProject.tasks.create(TASK_NAME, CpdTask.class);
        aTask.description = 'Performs copy-paste detection on the source files';
        aTask.cpdClasspath = fConfiguration;
        aTask.group = 'verification';
        aTask.setupReports();
        aTask.addUpToDateCheck();

        // Add the CPD task to the check task's dependencies.
        Projects.getTask(fProject, 'check', Task.class)?.dependsOn(aTask);

        return aTask;
    }


    static private String getGroupAndArtifactID(VersionNumber pVersionNumber)
    {
        if (pVersionNumber >= VERSION_CPD_GROUP_ARTIFACT_ID_GEN3)
            return CPD_GROUP_ARTIFACT_ID_GEN3;
        else if (pVersionNumber >= VERSION_CPD_GROUP_ARTIFACT_ID_GEN2)
            return CPD_GROUP_ARTIFACT_ID_GEN2;
        else
            return CPD_GROUP_ARTIFACT_ID_GEN1;
    }
}
