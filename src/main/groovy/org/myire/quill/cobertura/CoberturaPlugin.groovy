/*
 * Copyright 2014-2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cobertura

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.ReportingBasePlugin
import org.gradle.api.tasks.testing.Test

import org.myire.quill.common.Projects


/**
 * Plugin that provides support for generating Cobertura code coverage reports. The plugin adds two
 * tasks to the project for each task of type {@code Test}t; an instrumentation task and a reports
 * task. Furthermore, this  plugin will add a configuration that specifies the classpath used when
 * running the Cobertura  tasks. An extension through which the tasks are configured is also added
 * to the project.
 */
class CoberturaPlugin implements Plugin<Project>
{
    static final String CONFIGURATION_NAME = 'cobertura'
    static final String PROJECT_EXTENSION_NAME = 'cobertura'

    static private final String INSTRUMENT_TASK_NAME_SUFFIX = 'CoberturaInstrument'
    static private final String REPORTS_TASK_NAME_PREFIX = 'cobertura'
    static private final String REPORTS_TASK_NAME_SUFFIX = 'Report'

    static private final String COBERTURA_GROUP_ARTIFACT_ID = "net.sourceforge.cobertura:cobertura"


    private Project fProject
    private CoberturaExtension fExtension
    private Configuration fConfiguration


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the reporting base plugin is applied.
        pProject.plugins.apply(ReportingBasePlugin.class);

        // Create the global Cobertura extension and add it to the project. The test task
        // enhancements and contexts get some default property values from this extension.
        fExtension = createExtension();

        // Create the Cobertura configuration and add it to the project. The cobertura classpath is
        // specified through this configuration's dependencies.
        fConfiguration = createConfiguration();

        // Enhance all test tasks.
        enhanceTestTasks();
    }


    private CoberturaExtension createExtension()
    {
        return fProject.extensions.create(PROJECT_EXTENSION_NAME, CoberturaExtension.class, fProject);
    }


    private Configuration createConfiguration()
    {
        Configuration aConfiguration = fProject.configurations.maybeCreate(CONFIGURATION_NAME);

        aConfiguration.with
        {
            visible = false;
            transitive = true;
            description = 'The Cobertura classes used by the Gradle tasks';
        }

        aConfiguration.incoming.beforeResolve
        {
            // If no dependencies are explicitly declared, a dependency on the Cobertura artifact
            // with the version specified in the Cobertura extension property 'toolVersion' is
            // added.
            if (aConfiguration.dependencies.empty)
            {
                String aID = "${COBERTURA_GROUP_ARTIFACT_ID}:${fExtension.toolVersion}";
                aConfiguration.dependencies.add(fProject.dependencies.create(aID));
            }
        }

        return aConfiguration;
    }


    private void enhanceTestTasks()
    {
        fProject.tasks.withType(Test) { enhanceTestTask(it) }
    }


    /**
     * Enhance a test task by creating a {@code CoberturaContext} and a
     * {@code CoberturaTestEnhancement} for the task, as well as an instrumentation task and a
     * reports task.
     *
     * @param pTestTask The test task to enhance with Cobertura functionality.
     */
    private void enhanceTestTask(Test pTestTask)
    {
        fProject.logger.debug('Enhancing task {} with Cobertura functionality', pTestTask.name);

        // Create the context for the Cobertura enhancement of this task and add it as an extension
        // to the project.
        CoberturaContext aContext = fProject.extensions.create(PROJECT_EXTENSION_NAME + pTestTask.name.capitalize(),
                                                               CoberturaContext.class,
                                                               fExtension,
                                                               pTestTask.name.toLowerCase());

        // Add the appropriate values from the context to the test task's inputs and outputs.
        addInputsAndOutputs(pTestTask, aContext)

        // Create an enhancement for the test task.
        CoberturaTestTaskEnhancement aEnhancement =
                new CoberturaTestTaskEnhancement(pTestTask, fExtension, aContext);

        // Add the code coverage analysis preparation step to the test task.
        pTestTask.doFirst
        {
            aEnhancement.beforeTestExecution();
        }

        // Add the code coverage analysis restoration step to the test task.
        pTestTask.doLast
        {
            aEnhancement.afterTestExecution();
        }

        // Create the instrumentation task and make the test task depend on it.
        Task aInstrumentTask = createInstrumentTask(aContext);
        pTestTask.dependsOn += aInstrumentTask;

        // Create the reports task and make it depend on the test task. Also make the build task
        // depend on the reports task to including the Cobertura reports in the build.
        Task aReportsTask = createReportsTask(aContext);
        aReportsTask.dependsOn += pTestTask;
        Projects.getTask(fProject, 'build', Task.class)?.dependsOn(aReportsTask);

    }


    private CoberturaInstrumentTask createInstrumentTask(CoberturaContext pContext)
    {
        String aTaskName = pContext.name + INSTRUMENT_TASK_NAME_SUFFIX;
        CoberturaInstrumentTask aTask = fProject.tasks.create(aTaskName, CoberturaInstrumentTask.class);
        aTask.init(fExtension, pContext);
        aTask.description = 'Instruments the main classes for test coverage analysis';
        aTask.group = 'verification';

        return aTask;
    }


    private CoberturaReportsTask createReportsTask(CoberturaContext pContext)
    {
        String aTaskName = REPORTS_TASK_NAME_PREFIX + pContext.name.capitalize() + REPORTS_TASK_NAME_SUFFIX;
        CoberturaReportsTask aTask = fProject.tasks.create(aTaskName, CoberturaReportsTask.class);
        aTask.init(fExtension, pContext);
        aTask.description = 'Creates the Cobertura test coverage reports';
        aTask.group = 'verification';

        return aTask;
    }


    private void addInputsAndOutputs(Test pTestTask, CoberturaContext pContext)
    {
        // If the context's enabled flag is modified the test task should be rerun.
        pTestTask.inputs.property('coberturaEnabled', { -> pContext.enabled })

        // The task enhancement uses the Cobertura classpath, and the output from the
        // instrumentation task.
        pTestTask.inputs.files({ -> fExtension.coberturaClassPath })
        pTestTask.inputs.dir({ -> pContext.instrumentedClassesDir })
        pTestTask.inputs.file({ -> pContext.instrumentationDataFile })

        // The task enhancement produces the updated Cobertura data file.
        pTestTask.outputs.file({ -> pContext.executionDataFile })
    }
}
