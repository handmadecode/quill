/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsExtension
import org.gradle.api.plugins.quality.FindBugsPlugin

import org.myire.quill.common.Projects


/**
 * Enhancer for the FindBugs plugin. The FindBugs tasks will be configured to let the build continue
 * even if violations are found, and to include messages in the XML report. Each task will also have
 * a {@code TransformingReport} added to its convention. This report will, if enabled, create an
 * HTML report from the XML report by applying an XSL transformation.
 */
class FindBugsEnhancer extends AbstractPluginEnhancer<FindBugs>
{
    static private final String FINDBUGS_TOOL_NAME = 'FindBugs'
    static private final String FINDBUGS_EXTENSION_NAME = FINDBUGS_TOOL_NAME.toLowerCase()
    static private final String DEFAULT_TOOL_VERSION = '3.0.1'


    /**
     * Create an enhancer for the {@code FindBugsPlugin}.
     *
     * @param pProject  The project in which the plugin should be enhanced.
     */
    FindBugsEnhancer(Project pProject)
    {
        super(pProject, FindBugsPlugin.class, FINDBUGS_TOOL_NAME);
    }


    @Override
    Class<FindBugs> getTaskClass()
    {
        return FindBugs.class;
    }


    /**
     * Configure the FindBugs extension with some opinionated defaults.
     */
    @Override
    void configureExtension()
    {
        Projects.getExtension(project, FINDBUGS_EXTENSION_NAME, FindBugsExtension.class)?.with
        {
            toolVersion = DEFAULT_TOOL_VERSION;
            ignoreFailures = true;
        }
    }


    @Override
    AbstractCheckTaskEnhancer<FindBugs> createTaskEnhancer(FindBugs pTask)
    {
        return new FindBugsTaskEnhancer(pTask);
    }


    /**
     * Enhancer for {@code FindBugs} tasks.
     */
    static private class FindBugsTaskEnhancer extends AbstractCheckTaskEnhancer<FindBugs>
    {
        static private final String BUILTIN_FINDBUGS_XSL =
                '/org/myire/quill/rsrc/report/findbugs/findbugs.xsl'

        FindBugsTaskEnhancer(FindBugs pTask)
        {
            super(pTask);
        }

        @Override
        void enhance()
        {
            // Include bug descriptions in the XML report.
            task.reports.xml.withMessages = true;

            // Add an HTML report that is created by transforming the XML report.
            addTransformingReport(task.reports.getXml(), BUILTIN_FINDBUGS_XSL);
        }
    }
}
