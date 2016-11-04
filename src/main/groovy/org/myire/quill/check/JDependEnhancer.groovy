/*
 * Copyright 2015-2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.plugins.quality.JDepend
import org.gradle.api.plugins.quality.JDependExtension
import org.gradle.api.plugins.quality.JDependPlugin
import org.gradle.util.GFileUtils

import org.myire.quill.common.Projects


/**
 * Enhancer for the JDepend plugin. The enhancer replaces the default JDepend library with the
 * <a href="https://github.com/nidi3/jdepend">guru-nidi fork</a>.
 *<p>
 * The JDepend extension is enhanced with two properties that control the version of the guru-nidi
 * fork to use and the version of the Ant task to use.
 *<p>
 * The JDepend tasks will be enhanced with the possibility to use a properties file specified in the
 * task property &quot;jdependProperties.file&quot;. This file  contains runtime options for JDepend
 * as described in the
 * <a href="http://clarkware.com/software/JDepend.html#customize">documentation</a>.
 *<p>
 * Each task will also have a {@code TransformingReport} added to its convention. This report will,
 * if enabled, create an HTML report from the XML report by applying an XSL transformation.
 */
class JDependEnhancer extends AbstractPluginEnhancer<JDepend>
{
    static private final String JDEPEND_TOOL_NAME = 'JDepend'
    static private final String JDEPEND_EXTENSION_NAME = 'jdepend'
    static private final String JDEPEND_CONFIGURATION_NAME = 'jdepend'
    static private final String JDEPEND_PROPERTIES_FILE_NAME = 'jdepend.properties'

    static private final String JDEPEND_GROUP_ID = 'jdepend'
    static private final String JDEPEND_ARTIFACT_ID = 'jdepend'
    static private final String GURU_NIDI_GROUP_ARTIFACT_ID = 'guru.nidi:jdepend'
    static private final String DEFAULT_GURU_NIDI_VERSION = '2.9.5'
    static private final String ANT_TASK_GROUP_ARTIFACT_ID = 'org.apache.ant:ant-jdepend'
    static private final String DEFAULT_ANT_TASK_VERSION = '1.9.7'


    JDependEnhancer(Project pProject)
    {
        super(pProject, JDependPlugin.class, JDEPEND_TOOL_NAME);
    }


    @Override
    void enhance()
    {
        super.enhance();
        configureDependencies();
    }


    @Override
    Class<JDepend> getTaskClass()
    {
        return JDepend.class;
    }


    /**
     * Configure the JDepend extension with some opinionated defaults.
     */
    @Override
    void configureExtension()
    {
        JDependExtension aExtension = getExtension();

        // Set the common defaults for all code quality extensions.
        configureCodeQualityExtension(aExtension);

        // Add properties that specify the version of the guru-nidi fork and the version of the
        // JDepend Ant task.
        aExtension?.metaClass?.guruNidiVersion = DEFAULT_GURU_NIDI_VERSION;
        aExtension?.metaClass?.antTaskVersion = DEFAULT_ANT_TASK_VERSION;
    }


    @Override
    AbstractCheckTaskEnhancer<JDepend> createTaskEnhancer(JDepend pTask)
    {
        return new JDependTaskEnhancer(pTask, createBuiltInPropertiesFileSpec());
    }


    /**
     * Get the JDepend extension from the project.
     *
     * @return  The extension.
     */
    private JDependExtension getExtension()
    {
        return Projects.getExtension(project, JDEPEND_EXTENSION_NAME, JDependExtension.class);
    }


    /**
     * Configure the JDepend configuration's dependencies by replacing the default JDepend
     * dependency with a dependency on the guru-nidi fork.
     */
    private void configureDependencies()
    {
        def aConfiguration = project.configurations[JDEPEND_CONFIGURATION_NAME];
        aConfiguration?.incoming?.beforeResolve
        {
            JDependExtension aExtension = getExtension();

            String aGuruNidiVersion = aExtension?.guruNidiVersion;
            if (aGuruNidiVersion == null)
                // The version of the guru-nidi fork is explicitly cleared, don't modify the
                // dependencies.
                return;

            // Remove the default JDepend dependency if present.
            aConfiguration.dependencies.removeAll({
                it.group == JDEPEND_GROUP_ID && it.name == JDEPEND_ARTIFACT_ID;
            });

            // Add a dependency on the guru-nidi fork.
            aConfiguration.dependencies.add(project.dependencies.create("$GURU_NIDI_GROUP_ARTIFACT_ID:$aGuruNidiVersion"));

            // Add a dependency on the Ant task unless the version is cleared.
            String aAntTaskVersion = aExtension.antTaskVersion;
            if (aAntTaskVersion != null)
                aConfiguration.dependencies.add(createAntTaskDependency(aAntTaskVersion));

        }
    }


    /**
     * Create a dependency for the JDepend Ant task with the transitive dependency on JDepend
     * excluded.
     *
     * @param pVersion  The version of the dependency.
     *
     * @return  A new {@code Dependency}.
     */
    Dependency createAntTaskDependency(String pVersion)
    {
        Dependency aDependency = project.dependencies.create("$ANT_TASK_GROUP_ARTIFACT_ID:$pVersion");
        if (aDependency instanceof  ModuleDependency)
            ((ModuleDependency) aDependency).exclude([group: 'jdepend', module: 'jdepend']);

        return aDependency;
    }


    /**
     * Get a file specification for the built-in properties file extracted to disk.
     *
     * @return  The file specification of the built-in properties file.
     */
    private File createBuiltInPropertiesFileSpec()
    {
        return createTemporaryFileSpec(JDEPEND_PROPERTIES_FILE_NAME);
    }


    /**
     * Enhancer for {@code JDepend} tasks.
     */
    static private class JDependTaskEnhancer extends AbstractCheckTaskEnhancer<JDepend>
    {
        static private final String BUILTIN_JDEPEND_XSL =
                '/org/myire/quill/rsrc/report/jdepend/jdepend.xsl'
        static private final String BUILTIN_JDEPEND_PROPERTIES =
                '/org/myire/quill/rsrc/config/jdepend/jdepend.properties'

        static private final String JDEPEND_PROPERTIES_CONVENTION_NAME = 'jdependProperties'
        static private final String JDEPEND_PROPERTIES_TEMP_DIRECTORY_NAME = 'jdepend_properties'

        private final File fBuiltInPropertiesFileSpec;
        private FileCollection fJDependClasspath;

        JDependTaskEnhancer(JDepend pTask, File pBuiltInPropertiesFileSpec)
        {
            super(pTask);
            fBuiltInPropertiesFileSpec = pBuiltInPropertiesFileSpec;
        }

        @Override
        void enhance()
        {
            // Add the properties to the task's convention. The properties file must be accessed
            // through an intermediate object, since it isn't possible to modify an extension
            // directly from the DSL.
            JDependProperties aJDependProperties =
                    new JDependProperties(task.project, { getExtractedBuiltInPropertiesFile() });
            task.convention.add(JDEPEND_PROPERTIES_CONVENTION_NAME, aJDependProperties);
            task.inputs.file({ -> aJDependProperties.file });

            // Add an action that makes sure the built-in properties file is extracted if it is
            // specified as the properties file to use.
            task.doFirst({ ensureBuiltInPropertiesFile() });

            // Lazily create the classpath to add the properties file (if one is specified) to the
            // standard classpath. This cannot be done in a task action; the classpath is an input
            // to the task and must be set before the task executes.
            FileCollection aDefaultClasspath = task.getJdependClasspath();
            task.conventionMapping.jdependClasspath = { getClassPathWithProperties(aDefaultClasspath) };

            // Add an HTML report that is created by transforming the XML report.
            addTransformingReport(task.reports.getXml(), BUILTIN_JDEPEND_XSL);
        }

        /**
         * Ensure that the built-in properties file has been extracted from the classpath resource
         * if the task is configured to use it.
         */
        void ensureBuiltInPropertiesFile()
        {
            File aPropertiesFile = getPropertiesFile();
            if (aPropertiesFile == fBuiltInPropertiesFileSpec)
                extractResourceToFile(BUILTIN_JDEPEND_PROPERTIES, aPropertiesFile);
        }

        /**
         * Get the built-in properties file specification, making has been extracted from the
         *  classpath resource.
         *
         * @return  The built-in properties file specification.
         */
        File getExtractedBuiltInPropertiesFile()
        {
            extractResourceToFile(BUILTIN_JDEPEND_PROPERTIES, fBuiltInPropertiesFileSpec);
            return fBuiltInPropertiesFileSpec;
        }

        /**
         * Get the properties file specification from the {@code JDepend} task's convention.
         *
         * @return  The properties file specification, possibly null.
         */
        private File getPropertiesFile()
        {
            Object aProperties = task.convention.findByName(JDEPEND_PROPERTIES_CONVENTION_NAME);
            if (aProperties instanceof JDependProperties)
                return ((JDependProperties) aProperties).file;
            else
                return null;
        }

        /**
         * Create the classpath for the JDepend task by adding the properties file if one is
         * specified.
         *
         * @param pDefaultClassPath The task's default classpath.
         *
         * @return  {@code pDefaultClassPath} with the properties file appended, if specified.
         */
        FileCollection getClassPathWithProperties(FileCollection pDefaultClassPath)
        {
            if (fJDependClasspath != null)
                // Already calculated.
                return fJDependClasspath;

            File aPropertiesFile = getPropertiesFile();
            if (aPropertiesFile != null && aPropertiesFile != fBuiltInPropertiesFileSpec)
                // A project specific properties file, copy it to a new directory in the task's
                // temporary directory where it is the only file (it is the directory, not the file,
                // that will be added to  the classpath). Also make sure the file has the name
                // expected by JDepend.
                aPropertiesFile = copyPropertiesFileToNewDirectory(task.temporaryDir, aPropertiesFile);

            if (aPropertiesFile != null)
                // JDepend looks for the 'jdepend.properties' file in all directories on the
                // classpath, thus the file's parent must be added to the classpath.
                fJDependClasspath = pDefaultClassPath + task.project.files(aPropertiesFile.parentFile);
            else
                fJDependClasspath = pDefaultClassPath;

            return fJDependClasspath;
        }

        /**
         * Create a new directory and copy a JDepend properties file there.
         *
         * @param pParentDir        The directory to create the new directory in.
         * @param pPropertiesFile   The properties file to copy.
         *
         * @return  A {@code File} specifying the copied properties file.
         */
        private File copyPropertiesFileToNewDirectory(File pParentDir, File pPropertiesFile)
        {
            if (pPropertiesFile.canRead())
            {
                // Create the new directory.
                File aPropertiesDir = new File(pParentDir, JDEPEND_PROPERTIES_TEMP_DIRECTORY_NAME);
                aPropertiesDir.mkdirs();

                // Copy the properties file.
                File aTempPropertiesFile = new File(aPropertiesDir, JDEPEND_PROPERTIES_FILE_NAME);
                GFileUtils.copyFile(pPropertiesFile, aTempPropertiesFile);
                return aTempPropertiesFile;
            }
            else
            {
                task.logger.warn('JDepend properties file {} cannot be read, ignoring',
                                 pPropertiesFile.absolutePath);
                return null;
            }
        }
    }
}
