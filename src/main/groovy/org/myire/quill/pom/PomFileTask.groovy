/*
 * Copyright 2016 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.pom

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.maven.MavenPom
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.plugins.BasePluginConvention
import org.gradle.api.plugins.MavenPluginConvention
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import org.myire.quill.common.Projects


/**
 * Task that creates a stand-alone pom file using the {@code MavenPom} implementation from the Maven
 * plugin.
 */
class PomFileTask extends DefaultTask
{
    private final List<File> fStaticPomDataFiles = [];
    private final Set<String> fExcludedScopes = [];

    private File fDestination;
    private FileResolver fDestinationResolver;


    /**
     * Initialize the task after is has been created.
     */
    void init()
    {
        fDestinationResolver = Projects.createBaseDirectoryFileResolver(project, project.buildDir);
    }


    /**
     * Specify one or more file paths from which static pom data should be loaded in
     * {@code createPomFile()}.
     *
     * @param pPaths    The file path(s) to load the static pom data from. Each path will be
     *                  resolved relative to the project directory.
     *
     * @return  This instance.
     */
    PomFileTask from(Object... pPaths)
    {
        for (Object aPath : pPaths)
        {
            File aResolvedFile = project.file(aPath);
            fStaticPomDataFiles.add(aResolvedFile);
            inputs.file(aResolvedFile);
        }

        return this;
    }


    /**
     * Specify one or more dependency scopes to exclude from the pom file.
     *
     * @param pScopes   The name(s) of the scope(s) to exclude.
     *
     * @return  This instance.
     */
    PomFileTask withoutScope(String... pScopes)
    {
        for (String aScope : pScopes)
        {
            fExcludedScopes.add(aScope);
            inputs.property(aScope, Boolean.TRUE);
        }

        return this;
    }


    /**
     * Get the specification for the pom file to create. The default value is a file named
     * &quot;&lt;archivesBaseName&gt;-&lt;version&gt;.pom&quot; in the default pom directory
     * of the Maven plugin.
     *
     * @return  The pom file specification.
     */
    @OutputFile
    File getDestination()
    {
        if (fDestination == null)
            fDestination = createDefaultPomFileSpec();

        return fDestination;
    }


    /**
     * Set the path of the pom file to create. The specified path will be resolved relative to the
     * project build directory.
     *
     * @param pPath The pom file path. Passing null will effectively restore the default value the
     *              next time {@code getDestination} is called.
     */
    void setDestination(Object pPath)
    {
        fDestination = pPath ? resolveDestination(pPath) : null;
    }


    /**
     * Create the pom file specified by the {@code destination} property, adding any static data
     * from files passed to {@code from} and removing any dependencies with scope passed to
     * {@code withoutScopes}.
     */
    @TaskAction
    void createPomFile()
    {
        // Create the pom instance through the plugin convention installed by the Maven plugin.
        MavenPluginConvention aConvention =
                Projects.getConventionPlugin(project, 'maven', MavenPluginConvention.class);
        MavenPom aPom = aConvention?.pom();
        if (aPom != null)
        {
            // Filter out dependencies with a scope specified in withoutScope() when the pom is
            // configured.
            aPom.whenConfigured{ it.dependencies = it.dependencies.findAll{ shouldIncludeDependency(it) } }

            // Add static pom data when the XML is generated.
            aPom.withXml { addStaticPomData(it.asNode()) }

            // Write the pom's XML representation to the destination file. This will trigger the two
            // closures declared above.
            aPom.writeTo(destination);
        }
        else
            project.logger.warn('Could not create pom file, the Maven plugin does not seem to have loaded correctly');
    }


    /**
     * Check if a dependency should be included in the pom file. Dependencies with a scope that has
     * been specified in a call to {@code withoutScope} should not be included.
     *
     * @param pDependency   The dependency to check.
     *
     * @return  True if the dependency should be included, false if it should be filtered out.
     */
    boolean shouldIncludeDependency(Object pDependency)
    {
        return !fExcludedScopes.contains(pDependency.properties['scope']);
    }


    /**
     * Add static pom data from the files specified in calls to {@code from} to the root node of a
     * pom's XML representation.
     *
     * @param pRootNode The pom's root node.
     */
    void addStaticPomData(Node pRootNode)
    {
        XmlParser aParser = new XmlParser();
        fStaticPomDataFiles.each
        {
            // The root element of the file should not be added, only its child elements.
            aParser.parse(it).children().each { pRootNode.append((Node) it) }
        }
    }


    /**
     * Create the file specification for the default pom file to create.
     *
     * @return  A specification for the default pom file.
     */
    private File createDefaultPomFileSpec()
    {
        BasePluginConvention aConvention =
                Projects.getConventionPlugin(project, 'base', BasePluginConvention.class);
        String aBaseName = aConvention?.archivesBaseName ?: project.name;
        String aFileName = aBaseName + '-' + project.version + '.pom';
        return new File(defaultPomDirectory(), aFileName);
    }


    /**
     * Get the specification for the default directory to create the pom file in.
     *
     * @return  The specification for the default pom directory.
     */
    private File defaultPomDirectory()
    {
        MavenPluginConvention aConvention =
                Projects.getConventionPlugin(project, 'maven', MavenPluginConvention.class);
        return aConvention?.mavenPomDir ?: project.buildDir;
    }


    /**
     * Resolve a file path relative to the build directory of the task's project. This method is
     * equivalent to {@code Project.file(Object)} in all aspects except that the build directory is
     * used as parent for relative paths instead of the project directory,
     *
     * @param pPath The path object to resolve as a file.
     *
     * @return  A file, never null.
     */
    private File resolveDestination(Object pPath)
    {
        if (fDestinationResolver != null)
            return fDestinationResolver.resolve(pPath);

        // No resolver available, resolve the path relative to the project directory and replace the
        // project directory prefix with the path to the build directory. This variant does not
        // resolve an absolute path to (a subpath of) the project directory correctly ; the resolved
        // file will erroneously be in the build directory.
        File aResolvedFile = project.file(pPath);
        String aResolvedFilePath = aResolvedFile.absolutePath;
        String aProjectDirPath = project.projectDir.absolutePath;
        if (aResolvedFilePath.startsWith(aProjectDirPath))
        {
            // The file is a child of the project directory, replace the project directory part with
            // the absolute path to the build directory.
            aResolvedFilePath = project.buildDir.absolutePath + aResolvedFilePath.substring(aProjectDirPath.length());
            return new File(aResolvedFilePath);
        }
        else
            // The file was not resolved relative to the project directory and should not be
            // relative to the build directory either.
            return aResolvedFile;
    }
}
