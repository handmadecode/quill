/*
 * Copyright 2015, 2021-2022 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Project
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.api.plugins.quality.CheckstyleExtension
import org.gradle.api.plugins.quality.CheckstylePlugin
import org.gradle.api.plugins.quality.CheckstyleReports
import org.gradle.api.reporting.Report

import org.myire.quill.common.Projects
import org.myire.quill.report.Reports


/**
 * Enhancer for the Checkstyle plugin. The Checkstyle tasks will be configured to let the build
 * continue even if violations are found and to be less verbose. Furthermore, the tasks will, unless
 * explicitly configured otherwise, use the configuration file distributed with Quill.
 *<p>
 * Each task will also have a {@code TransformingReport} added to its convention. This report will,
 * if enabled, create an HTML report from the XML report by applying an XSL transformation.
 */
class CheckstyleEnhancer extends AbstractPluginEnhancer<Checkstyle>
{
    static private final String CHECKSTYLE_TOOL_NAME = 'Checkstyle'
    static private final String CHECKSTYLE_EXTENSION_NAME = CHECKSTYLE_TOOL_NAME.toLowerCase()
    static private final String DEFAULT_TOOL_VERSION = '8.45.1'

    // The file name to use when extracting the built-in config file from its resource.
    static private final String BUILTIN_CONFIG_FILE_NAME = 'checkstyle_config.xml'


    /**
     * Create an enhancer for the {@code CheckstylePlugin}.
     *
     * @param pProject  The project in which the plugin should be enhanced.
     */
    CheckstyleEnhancer(Project pProject)
    {
        super(pProject, CheckstylePlugin.class, CHECKSTYLE_TOOL_NAME);
    }


    @Override
    Class<Checkstyle> getTaskClass()
    {
        return Checkstyle.class;
    }


    /**
     * Configure the Checkstyle extension with some opinionated defaults.
     */
    @Override
    void configureExtension()
    {
        // Set the common defaults for all code quality extensions.
        configureCodeQualityExtension(getExtension());

        getExtension()?.with
        {
            toolVersion = DEFAULT_TOOL_VERSION

            // Don't print each violation to the console.
            showViolations = false;

            // Use the built-in config file as default.
            configFile = createBuiltInConfigFileSpec();
        }
    }


    @Override
    AbstractCheckTaskEnhancer<Checkstyle> createTaskEnhancer(Checkstyle pTask)
    {
        return new CheckstyleTaskEnhancer(pTask, getExtension(), createBuiltInConfigFileSpec());
    }


    /**
     * Get the HTMl report from a {@code CheckstyleReports} instance. This report was introduced in
     * Gradle 2.10 and is not present in earlier versions.
     *
     * @param pReportContainer  The {@code CheckstyleReports} instance to get the HTML report from.
     *
     * @return  The HTML {@code Report}, or null if {@code CheckstyleReports} doesn't have a
     *          {@code getHtml} method.
     */
    static Report getHtmlReport(CheckstyleReports pReportContainer)
    {
        if (pReportContainer.metaClass.respondsTo(pReportContainer, 'getHtml'))
            return pReportContainer.getHtml();
        else
            return null;
    }


    /**
     * Create a file specification for the built-in config file extracted to disk.
     *
     * @return  The file specification of the built-in config file.
     */
    private File createBuiltInConfigFileSpec()
    {
        return createTemporaryFileSpec(BUILTIN_CONFIG_FILE_NAME);
    }


    /**
     * Get the Checkstyle extension from the project.
     *
     * @return  The extension.
     */
    private CheckstyleExtension getExtension()
    {
        return Projects.getExtension(project, CHECKSTYLE_EXTENSION_NAME, CheckstyleExtension.class);
    }


    /**
     * Enhancer for {@code Checkstyle} tasks.
     */
    static private class CheckstyleTaskEnhancer extends AbstractCheckTaskEnhancer<Checkstyle>
    {
        static private final String BUILTIN_CHECKSTYLE_XSL =
                '/org/myire/quill/rsrc/report/checkstyle/checkstyle.xsl'
        static private final String BUILTIN_CHECKSTYLE_CONFIG =
                '/org/myire/quill/rsrc/config/checkstyle/checkstyle_config.xml'

        static private final String CHECKSTYLE_SUPPRESSIONS_FILE_PROPERTY = 'suppressions.file';


        private final CheckstyleExtension fExtension;
        private final File fBuiltInConfigFileSpec;
        private boolean fAddedSuppressionsFileProperty;

        CheckstyleTaskEnhancer(Checkstyle pTask, CheckstyleExtension pExtension, File pBuiltInConfigFileSpec)
        {
            super(pTask);
            fExtension = pExtension;
            fBuiltInConfigFileSpec = pBuiltInConfigFileSpec;
        }

        @Override
        void enhance()
        {
            // Produce an xml report but no html report.
            Reports.setRequired(task.reports.getXml(), true);
            Reports.setRequired(getHtmlReport(task.reports), false);

            // By default tasks get the config file from the extension, which is fine, but we must
            // check if the extension specifies the built-in config file (which it is configured to
            // do by default) and if so make sure the built-in file has been extracted.
            if (Checkstyle.metaClass.hasProperty(task, "config"))
                // Starting with Gradle v2.2, the configuration is specified in the 'config'
                // property, which is of type TextResource.
                task.conventionMapping.config = { extensionConfigResourceWithCheckForBuiltIn() };
            else
                // Versions before 2.2 specify the configuration in the 'configFile' property, which
                // is of type File.
                task.conventionMapping.configFile = { extensionConfigFileWithCheckForBuiltIn() };

            // Make sure the "suppressions.file" config property is specified, and that the
            // config properties are restored after execution to avoid false detection of modified
            // input properties.
            task.doFirst({ setupConfigProperties() });
            task.doLast({ restoreConfigProperties() });

            // Add an HTML report that is created by transforming the XML report.
            addTransformingReport(task.reports.getXml(), BUILTIN_CHECKSTYLE_XSL);
        }

        /**
         * Get the config file specified in the extension, and if it is the built-in config file
         * make sure that is has been extracted from the classpath resource.
         *
         * @return The config file specified in the extension.
         */
        File extensionConfigFileWithCheckForBuiltIn()
        {
            File aConfigFile = fExtension.configFile;
            if (aConfigFile == fBuiltInConfigFileSpec)
                extractResourceToFile(BUILTIN_CHECKSTYLE_CONFIG, aConfigFile);

            return aConfigFile;
        }

        /**
         * Get the extension's config file as a {@code TextResource}. If it is the built-in config
         * file, make sure that is has been extracted from the classpath resource.
         *<p>
         * The return type is {@code Object} rather than {@code TextResource}. The reason for is to
         * allow this class to be loaded without referring to  {@code TextResource}. By doing this,
         * Gradle versions before 2.2, which don't define the {@code TextResource} class, can use
         * this class as long as this method isn't invoked.
         *
         * @return The extension's config file as a {@code TextResource}.
         */
        Object extensionConfigResourceWithCheckForBuiltIn()
        {
            return task.project.resources.text.fromFile(extensionConfigFileWithCheckForBuiltIn());
        }

        /**
         * Check if the property &quot;suppressions.file&quot; in the Checkstyle task's
         * configuration properties is set. If it isn't, set it to a file in the task's temporary
         * directory called &quot;checkstyle_suppressions.xml&quot;. If that file doesn't exist,
         * extract it from its classpath resource.
         */
        void setupConfigProperties()
        {
            // Remember if the suppressions file property was added or not so it can be restored.
            fAddedSuppressionsFileProperty = false;

            // Only set the suppressions file if the built-in config file is used and the property
            // isn't set already.
            if (task.configFile == fBuiltInConfigFileSpec)
            {
                String aSuppressionsFilePath = task.configProperties?.get(CHECKSTYLE_SUPPRESSIONS_FILE_PROPERTY);
                if (aSuppressionsFilePath == null)
                {
                    task.configProperties.put(CHECKSTYLE_SUPPRESSIONS_FILE_PROPERTY,
                                              'no-suppressions-file-' + System.nanoTime());
                    fAddedSuppressionsFileProperty = true;
                }
            }
        }

        /**
         * Restore the task's configuration properties if they were modified before the task
         * executed.
         */
        void restoreConfigProperties()
        {
            if (fAddedSuppressionsFileProperty)
                task.configProperties?.remove(CHECKSTYLE_SUPPRESSIONS_FILE_PROPERTY);
        }
    }
}
