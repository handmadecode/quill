/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.quality.CodeQualityExtension
import org.gradle.api.reporting.Report
import org.gradle.api.tasks.SourceSet


/**
 * Enhancer for the SpotBugs plugin. The SpotBugs tasks will be configured to let the build continue
 * even if violations are found, and to include messages in the XML report. Each task will also have
 * a {@code TransformingReport} added to its convention. This report will, if enabled, create an
 * HTML report from the XML report by applying an XSL transformation.
 */
class SpotBugsEnhancer extends AbstractPluginEnhancer<Task>
{
    static private final String SPOTBUGS_TOOL_NAME = 'SpotBugs'
    static private final String SPOTBUGS_EXTENSION_NAME = SPOTBUGS_TOOL_NAME.toLowerCase()

    private final Class<Task> fTaskClass;


    /**
     * Create an enhancer for the {@code SpotBugsPlugin}.
     *
     * @param pProject  The project in which the plugin should be enhanced.
     */
    SpotBugsEnhancer(Project pProject)
    {
        super(pProject, SPOTBUGS_TOOL_NAME);
        fTaskClass = loadSpotBugsTaskClass(pProject);
    }


    @Override
    Class<Task> getTaskClass()
    {
        return fTaskClass;
    }


    /**
     * Configure the SpotBugs extension with some opinionated defaults.
     */
    @Override
    void configureExtension()
    {
        Object aExtension = project.getExtensions().findByName(SPOTBUGS_EXTENSION_NAME);
        if (aExtension instanceof CodeQualityExtension)
        {
            // Set the defaults shared by all code quality extensions.
            configureCodeQualityExtension((CodeQualityExtension) aExtension);
        }
        else if (aExtension != null)
        {
            // The extension in SpotBugsPlugin v4 or later does not implement CodeQualityExtension,
            // it must be configured differently.
            if (aExtension.hasProperty("ignoreFailures"))
                aExtension.ignoreFailures = true;

            // There is no sourceSets property in v4, so the disableTestChecks methods instead
            // clears the classes to analyze for the spotbugsTest task.
            aExtension.metaClass.disableTestChecks
            {
                project.tasks.withType(fTaskClass)
                {
                    if (it.name.toLowerCase().endsWith(SourceSet.TEST_SOURCE_SET_NAME))
                        it.classes = project.files();
                }
            }
        }
    }


    @Override
    AbstractCheckTaskEnhancer<Task> createTaskEnhancer(Task pTask)
    {
        return new SpotBugsTaskEnhancer(pTask);
    }


    /**
     * Load the {@code SpotBugsTask} class available to the current class loader. The
     * {@code SpotBugsTask} class was moved from package {@code com.github.spotbugs} to
     * {@code com.github.spotbugs.snom} in version 4 of the SpotBugsPlugin.
     *
     * @param pProject  The project to log the class loading with.
     *
     * @return  The loaded {@code SpotBugsTask} class.
     *
     * @throws GradleException  if no {@code SpotBugsTask} class could be loaded.
     * @throws NullPointerException if {@code pProject} is null.
     */
    static private Class<Task> loadSpotBugsTaskClass(Project pProject)
    {
        String[] aCandidates = [
                "com.github.spotbugs.SpotBugsTask",
                "com.github.spotbugs.snom.SpotBugsTask"
        ];

        for (String aCandidate in aCandidates)
        {
            try
            {
                pProject.logger.debug("Trying to load class {}", aCandidate);
                Class<?> aClass = Class.forName(aCandidate);
                if (Task.class.isAssignableFrom(aClass))
                {
                    pProject.logger.debug("Successfully loaded class {}", aCandidate);
                    return (Class<Task>) aClass;
                }
            }
            catch (ClassNotFoundException | NoClassDefFoundError ignore)
            {
                pProject.logger.debug("Failed to to load class {}", aCandidate);
            }
        }

        throw new GradleException("Could not load any known SpotBugsTask class");
    }


    /**
     * Enhancer for {@code SpotBugs} tasks.
     */
    static private class SpotBugsTaskEnhancer extends AbstractCheckTaskEnhancer<Task>
    {
        static private final String BUILTIN_SPOTBUGS_XSL =
                '/org/myire/quill/rsrc/report/spotbugs/spotbugs.xsl'

        SpotBugsTaskEnhancer(Task pTask)
        {
            super(pTask);
        }

        @Override
        void enhance()
        {
            // Starting with version 4 of the plugin the reports are referenced by name, not as
            // properties.
            if (task.reports.hasProperty("xml"))
            {
                // Include bug descriptions in the XML report.
                task.reports.xml.withMessages = true;

                // Add an HTML report that is created by transforming the XML report.
                addTransformingReport(task.reports.getXml(), BUILTIN_SPOTBUGS_XSL);
            }
            else
            {
                // This version of the XML report always includes bug descriptions, only install the
                // transforming report.
                def aXmlReport = task.reports.maybeCreate('xml');
                if (aXmlReport instanceof Report)
                    addTransformingReport((Report) aXmlReport, BUILTIN_SPOTBUGS_XSL);
            }
        }
    }
}
