/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Project

import com.github.spotbugs.SpotBugsExtension
import com.github.spotbugs.SpotBugsTask

import org.myire.quill.common.Projects


/**
 * Enhancer for the SpotBugs plugin. The SpotBugs tasks will be configured to let the build continue
 * even if violations are found, and to include messages in the XML report. Each task will also have
 * a {@code TransformingReport} added to its convention. This report will, if enabled, create an
 * HTML report from the XML report by applying an XSL transformation.
 */
class SpotBugsEnhancer extends AbstractPluginEnhancer<SpotBugsTask>
{
    static private final String SPOTBUGS_TOOL_NAME = 'SpotBugs'
    static private final String SPOTBUGS_EXTENSION_NAME = SPOTBUGS_TOOL_NAME.toLowerCase()
    static private final String DEFAULT_TOOL_VERSION = '3.1.12'


    /**
     * Create an enhancer for the {@code SpotBugsPlugin}.
     *
     * @param pProject  The project in which the plugin should be enhanced.
     */
    SpotBugsEnhancer(Project pProject)
    {
        super(pProject, SPOTBUGS_TOOL_NAME);
    }


    @Override
    Class<SpotBugsTask> getTaskClass()
    {
        return SpotBugsTask.class;
    }


    /**
     * Configure the SpotBugs extension with some opinionated defaults.
     */
    @Override
    void configureExtension()
    {
        // Set the common defaults for all code quality extensions and the default tool version.
        SpotBugsExtension aExtension = Projects.getExtension(project, SPOTBUGS_EXTENSION_NAME, SpotBugsExtension.class);
        configureCodeQualityExtension(aExtension);
        aExtension?.toolVersion = DEFAULT_TOOL_VERSION;
    }


    @Override
    AbstractCheckTaskEnhancer<SpotBugsTask> createTaskEnhancer(SpotBugsTask pTask)
    {
        return new SpotBugsTaskEnhancer(pTask);
    }


    /**
     * Enhancer for {@code SpotBugs} tasks.
     */
    static private class SpotBugsTaskEnhancer extends AbstractCheckTaskEnhancer<SpotBugsTask>
    {
        static private final String BUILTIN_SPOTBUGS_XSL =
                '/org/myire/quill/rsrc/report/spotbugs/spotbugs.xsl'

        SpotBugsTaskEnhancer(SpotBugsTask pTask)
        {
            super(pTask);
        }

        @Override
        void enhance()
        {
            // Include bug descriptions in the XML report.
            task.reports.xml.withMessages = true;

            // Add an HTML report that is created by transforming the XML report.
            addTransformingReport(task.reports.getXml(), BUILTIN_SPOTBUGS_XSL);
        }
    }
}
