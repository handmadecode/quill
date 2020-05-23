/*
 * Copyright 2015, 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension
import org.gradle.api.plugins.quality.PmdPlugin
import org.gradle.util.VersionNumber

import org.myire.quill.common.Projects
import org.myire.quill.common.Tasks


/**
 * Enhancer for the PMD plugin. The PMD tasks will be configured to let the build continue even if
 * violations are found and to use use the rule file distributed with Quill, unless explicitly
 * configured otherwise.
 *<p>
 * Each task will also have a {@code TransformingReport} added to its convention. This report will,
 * if enabled, create an HTML report from the XML report by applying an XSL transformation.
 *<p>
 * Finally, the dependencies of the {@code pmd} configuration are adjusted with respect to the
 * PMD version in use, since the name of the PMD artifact has changed over time.
 */
class PmdEnhancer extends AbstractPluginEnhancer<Pmd>
{
    static private final String PMD_TOOL_NAME = 'PMD'
    static private final String PMD_EXTENSION_NAME = PMD_TOOL_NAME.toLowerCase()
    static private final String DEFAULT_TOOL_VERSION = '6.23.0'

    // The file name to use when extracting the built-in rule file from its resource.
    static private final String BUILTIN_RULE_FILE_NAME = 'pmd_rules.xml'

    // The PMD version where the artifacts were restructured and the plugin should depend on
    // 'pmd-dist' rather than 'pmd'.
    static private final VersionNumber PMD_DIST_ARTIFACT_VERSION = VersionNumber.parse('5.2')


    /**
     * Create an enhancer for the {@code PmdPlugin}.
     *
     * @param pProject  The project in which the plugin should be enhanced.
     */
    PmdEnhancer(Project pProject)
    {
        super(pProject, PmdPlugin.class, PMD_TOOL_NAME);
    }


    @Override
    void enhance()
    {
        super.enhance();
        configureDependencies();
    }


    @Override
    Class<Pmd> getTaskClass()
    {
        return Pmd.class;
    }


    /**
     * Configure the PMD extension with some opinionated defaults.
     */
    @Override
    void configureExtension()
    {
        // Set the common defaults for all code quality extensions.
        configureCodeQualityExtension(getExtension());

        getExtension()?.with
        {
            toolVersion = DEFAULT_TOOL_VERSION;

            // Don't use the PMD built-in rule sets by default, instead use the Quill built-in rule
            // file.
            ruleSets = [];
            ruleSetFiles = project.files(createBuiltInRuleFileSpec());
        }
    }


    @Override
    AbstractCheckTaskEnhancer<Pmd> createTaskEnhancer(Pmd pTask)
    {
        return new PmdTaskEnhancer(pTask, getExtension(), createBuiltInRuleFileSpec());
    }


    /**
     * Get a file specification for the built-in rule file extracted to disk.
     *
     * @return  The file specification of the built-in rule file.
     */
    private File createBuiltInRuleFileSpec()
    {
        return createTemporaryFileSpec(BUILTIN_RULE_FILE_NAME);
    }


    /**
     * Get the PMD extension from the project.
     *
     * @return  The extension.
     */
    private PmdExtension getExtension()
    {
        return Projects.getExtension(project, PMD_EXTENSION_NAME, PmdExtension.class);
    }


    /**
     * Configure the PMD configuration's dependencies. Starting with version 5.2, the PMD binaries
     * were restructured, and the artifact to depend upon to get the entire distribution has the
     * name &quot;pmd-dist&quot;, not &quot;pmd&quot;.
     */
    private void configureDependencies()
    {
        def aConfig = project.configurations['pmd'];
        aConfig?.incoming?.beforeResolve
        {
            PmdExtension aExtension = getExtension();
            VersionNumber aVersion = VersionNumber.parse(aExtension.toolVersion);
            if (aVersion >= PMD_DIST_ARTIFACT_VERSION)
            {
                // The specified version of PMD requires the 'pmd-dist' artifact, not the 'pmd'
                // artifact. Remove any dependency on the latter.
                boolean aWasRemoved = aConfig.dependencies.removeAll({ it.name == 'pmd' });
                if (aWasRemoved)
                {
                    // A dependency on 'pmd' was removed, replace it with the correct dependency on
                    // 'pmd-dist'.
                    String aDependencySpec = "net.sourceforge.pmd:pmd-dist:$aExtension.toolVersion";
                    aConfig.dependencies.add(project.dependencies.create(aDependencySpec));
                }
            }
        }
    }


    /**
     * Enhancer for {@code Pmd} tasks.
     */
    static private class PmdTaskEnhancer extends AbstractCheckTaskEnhancer<Pmd>
    {
        static private final String BUILTIN_PMD_XSL = '/org/myire/quill/rsrc/report/pmd/pmd.xsl'
        static private
        final String BUILTIN_PMD_RULES = '/org/myire/quill/rsrc/config/pmd/pmd_rules.xml'

        private final PmdExtension fExtension;
        private final File fBuiltInRuleFileSpec;


        PmdTaskEnhancer(Pmd pTask, PmdExtension pExtension, File pBuiltInRuleFileSpec)
        {
            super(pTask);
            fExtension = pExtension;
            fBuiltInRuleFileSpec = pBuiltInRuleFileSpec;
        }

        @Override
        void enhance()
        {
            // Produce an xml report but no html report.
            task.reports.getXml().enabled = true;
            task.reports.getHtml().enabled = false;

            // By default tasks get the rule set files from the extension, which is fine, but we
            // must check if the extension specifies the built-in rule file (which it is configured
            // to do by default) in the set of files, and if so make sure the built-in file has been
            // extracted.
            task.conventionMapping.ruleSetFiles = { extensionRuleSetFilesWithCheckForBuiltIn() };

            // Add a convention property for the filter and an action that invokes it.
            PmdFilter aFilter = new PmdFilter(task);
            task.convention.add('filter', aFilter);
            task.doLast({ aFilter.apply() });

            // The filter file and its enabled flag are task inputs.
            Tasks.optionalInputFile(task, { -> aFilter.file });
            Tasks.inputProperty(task, 'filterEnabled', { -> aFilter.enabled });

            // Add an HTML report that is created by transforming the XML report.
            addTransformingReport(task.reports.getXml(), BUILTIN_PMD_XSL);
        }

        /**
         * Get the rule set files specified in the extension, and if one of those files is the
         * built-in config file make sure that is has been extracted from the classpath resource.
         *
         * @return The rule set files specified in the extension.
         */
        FileCollection extensionRuleSetFilesWithCheckForBuiltIn()
        {
            FileCollection aRuleSetFiles = fExtension.ruleSetFiles;
            aRuleSetFiles.find
            {
                if (it == fBuiltInRuleFileSpec)
                {
                    extractResourceToFile(BUILTIN_PMD_RULES, fBuiltInRuleFileSpec);
                    return true;
                }
                else
                    return false;
            }

            return aRuleSetFiles;
        }
    }
}
