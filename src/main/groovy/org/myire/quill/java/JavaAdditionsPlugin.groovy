/*
 * Copyright 2014 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.java

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.java.archives.Manifest
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.MinimalJavadocOptions
import org.gradle.external.javadoc.StandardJavadocDocletOptions

import org.myire.quill.common.Projects
import org.myire.quill.meta.ProjectMetaDataExtension
import org.myire.quill.meta.SemanticVersionExtension
import org.myire.quill.meta.ProjectMetaDataPlugin


/**
 * Gradle plugin for adding Java related tasks and configuring some standard Java tasks with
 * sensible (albeit opinionated) defaults.
 */
class JavaAdditionsPlugin implements Plugin<Project>
{
    static final String SOURCES_JAR_TASK_NAME = 'sourcesJar'
    static final String JAVADOC_JAR_TASK_NAME = 'javadocJar'

    static private final String MANIFEST_ATTRIBUTE_CLASSPATH = "Class-Path"
    static private final String MANIFEST_SECTION_BUILD_INFO = "Build-Info"


    private Project fProject


    @Override
    void apply(Project pProject)
    {
        fProject = pProject

        // Make sure the standard java plugin is applied.
        pProject.plugins.apply(JavaPlugin.class);

        // Configure the CompileJava tasks created by the java plugin.
        configureJavaCompileTasks();

        // Configure the Test task created by the java plugin.
        configureTestTask();

        // Configure the Javadoc task created by the java plugin.
        configureJavaDocTask();

        // Create tasks for assembling jar files containing the main sources and the JavaDocs
        // distributing the Javadoc files.
        createSourcesJarTask();
        createJavaDocJarTask();

        // Add enhancements to the manifest of all jar tasks.
        enhanceJarManifests();
    }


    private void configureJavaCompileTasks()
    {
        // Enable warnings of deprecated use and use UTF-8 as the source file encoding instead of
        // the platform default encoding.
        fProject.tasks.withType(JavaCompile.class)
        {
            it.options.deprecation = true;
            it.options.encoding = 'UTF-8';
        }
    }


    private void configureTestTask()
    {
        // Ignore failures, i.e. do not break the build if a test fails.
        Test aTask = Projects.getTask(fProject, JavaPlugin.TEST_TASK_NAME, Test.class);
        aTask?.ignoreFailures = true;
    }


    private void configureJavaDocTask()
    {
        Javadoc aTask = Projects.getTask(fProject, JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class);

        // Ignore failures, i.e. do not break the build if a JavaDoc comment is malformed.
        aTask?.failOnError = false;

        // Include protected and public members.
        MinimalJavadocOptions aOptions = aTask?.options;
        aOptions?.showFromProtected();

        if (aOptions instanceof StandardJavadocDocletOptions)
        {
            // Include author and version tags.
            aOptions.author = true;
            aOptions.version = true;

            // Create class and package usage pages.
            aOptions.use = true;
        }
    }


    /**
     * Create a task that assembles a jar file with the main sources and add it to the
     * &quot;archives&quot; artifact.
     */
    private void createSourcesJarTask()
    {
        SourceSet aSourceSet = Projects.getSourceSet(fProject, SourceSet.MAIN_SOURCE_SET_NAME);
        if (aSourceSet == null)
            return;

        Jar aTask = fProject.tasks.create(SOURCES_JAR_TASK_NAME, Jar.class);
        aTask.description = 'Assembles a jar archive containing the main source code.';
        aTask.from aSourceSet.allSource;
        aTask.classifier = 'sources';
        aTask.extension = 'jar';
        aTask.group = 'build';

        fProject.artifacts.add('archives', aTask);
    }


    /**
     * Create a task that assembles a jar file with the main JavaDocs and add it to the
     * &quot;archives&quot; artifact.
     */
    private void createJavaDocJarTask()
    {
        Javadoc aJavadocTask = Projects.getTask(fProject, JavaPlugin.JAVADOC_TASK_NAME, Javadoc.class);
        if (aJavadocTask == null)
            return;

        Jar aTask = fProject.tasks.create(JAVADOC_JAR_TASK_NAME, Jar.class);
        aTask.description = 'Assembles a jar archive containing the main JavaDocs.';
        aTask.dependsOn += aJavadocTask;
        aTask.from aJavadocTask.destinationDir;
        aTask.classifier = 'javadoc';
        aTask.extension = 'jar';
        aTask.group = 'build';

        fProject.artifacts.add('archives', aTask);
    }


    /**
     * Add methods to the meta class of all Jar tasks' manifest.
     */
    private void enhanceJarManifests()
    {
        Set<MetaClass> aEnhancedClasses = [];
        fProject.tasks.withType(Jar.class)
        {
            // Enhance the manifest's meta class if it hasn't been enhanced already.
            MetaClass aManifestMetaClass = it.manifest.class.metaClass;
            if (aEnhancedClasses.add(aManifestMetaClass))
            {
                // Add a method that adds a Class-Path attribute to the manifest. The attribute's
                // value will be a space-separated list of the jar files in the specified source
                // sets' runtime classpath(s).
                aManifestMetaClass.addClassPathAttribute =
                {
                    SourceSet... pSourceSets ->
                        addClassPathAttribute((Manifest) delegate, pSourceSets);
                }

                // Add a method that adds a Build-Info section to the manifest.
                aManifestMetaClass.addBuildInfoSection
                {
                    addBuildInfoSection((Manifest) delegate);
                }

                // Add a method that adds a java.lang.Package section to the manifest. The name of
                // the section will be the specified package name.
                aManifestMetaClass.addPackageSection =
                {
                    String pPackageName ->
                        addPackageSection((Manifest) delegate, pPackageName, fProject);
                }
            }
        }
    }


    /**
     * Add a Class-Path attribute to a jar's manifest. The value will be a space-separated list of
     * the jar file's in the specified source sets' runtime classpath(s). If none of the specified
     * source sets has any runtime jar files, the attribute will not be added.
     *
     * @param pManifest     The manifest to add the attribute to.
     * @param pSourceSets   The source set(s) to get the runtime jar files from.
     */
    static private void addClassPathAttribute(Manifest pManifest, SourceSet... pSourceSets)
    {
        def aClassPath = "";
        def aJarFiles = new HashSet<String>();
        pSourceSets.each
        {
            it.runtimeClasspath?.each
            {
                // Only add jar files that haven't been added previously to the class path.
                if (it.name.endsWith(".jar") && aJarFiles.add(it.name))
                    aClassPath += it.name + ' ';
            }
        }

        if (aClassPath.length() > 0 )
            pManifest.attributes.put(MANIFEST_ATTRIBUTE_CLASSPATH, aClassPath);
    }


    /**
     * Add a Build-Info section to a jar's manifest.
     *
     * @param pManifest The manifest to add the section to.
     */
    static private void addBuildInfoSection(Manifest pManifest)
    {
        // Use a linked hash map to preserve attribute order.
        def aBuildInfoAttributes = new LinkedHashMap();

        aBuildInfoAttributes.put("Build-Time", new Date().format("yyyyMMdd'T'HHmmss.SSSZ"));
        aBuildInfoAttributes.put("Build-JRE",  System.getProperty("java.runtime.version"));
        aBuildInfoAttributes.put("Build-VM",   System.getProperty("java.vm.name") + ' ' + System.getProperty("java.vm.version"));
        aBuildInfoAttributes.put("Build-OS",   System.getProperty("os.name") + ' ' + System.getProperty("os.version"));

        pManifest.attributes(aBuildInfoAttributes, MANIFEST_SECTION_BUILD_INFO);
    }


    /**
     * Add a package section to a jar's manifest.
     *
     * @param pManifest     The manifest to add the attribute to.
     * @param pPackageName  If non-null, the name of the section.
     * @param pProject      The project to get the section's attribute values from.
     */
    static private void addPackageSection(Manifest pManifest, String pPackageName, Project pProject)
    {
        // Use values from the project meta data and semantic version extensions if available.
        def aProjectMetaExtension =
                Projects.getExtension(
                        pProject,
                        ProjectMetaDataPlugin.PROJECT_META_EXTENSION_NAME,
                        ProjectMetaDataExtension.class);
        def aSemanticVersionExtension =
                Projects.getExtension(
                        pProject,
                        ProjectMetaDataPlugin.SEMANTIC_VERSION_EXTENSION_NAME,
                        SemanticVersionExtension.class);

        // If no package name is specified, use the main package name from the project meta data
        // extension, and abort if that value also is null.
        if (pPackageName == null)
        {
            pPackageName = aProjectMetaExtension?.mainPackage;
            if (pPackageName == null)
                return;
        }

        // Use a linked hash map to preserve attribute order.
        def aPackageAttributes = new LinkedHashMap();

        aPackageAttributes.put("Specification-Title",
                               aProjectMetaExtension?.shortName ?: pProject.name);
        aPackageAttributes.put("Specification-Version",
                               aSemanticVersionExtension?.shortVersionString ?: pProject.version);
        aPackageAttributes.put("Implementation-Title",
                               aProjectMetaExtension?.longName ?: pProject.name);
        aPackageAttributes.put("Implementation-Version",
                               aSemanticVersionExtension?.longVersionString ?: pProject.version);

        pManifest.attributes(aPackageAttributes, pPackageName);
    }
}
