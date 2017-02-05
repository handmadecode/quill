/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

import org.myire.quill.dependency.DependencyPrettyPrinter
import org.myire.quill.dependency.DependencySpec
import org.myire.quill.repository.MavenRepositorySpec
import org.myire.quill.repository.RepositoryPrettyPrinter


/**
 * A task that imports dependencies and/or repositories from a Maven pom file and writes them to an
 * output file on Gradle notation.
 */
class PomConvertTask extends DefaultTask
{
    // Properties accessed through getters and setters only.
    private File fPomFile;
    private File fDestination;

    /**
     * Should the repositories from the pom file be imported and converted? Default is true.
     */
    @Input
    boolean convertRepositories = true

    /**
     * Should the dependencies from the pom file be imported and converted? Default is true.
     */
    @Input
    boolean convertDependencies = true

    /**
     * Should the destination file be overwritten if it exists? If this property is false and the
     * destination file exists, the task will do nothing. Default is true.
     */
    @Input
    boolean overwrite = true

    /**
     * Classpath containing the Maven related classes used by the task. The plugin sets this
     * property to its default value, which is the {@code mavenImport} configuration.
     */
    @InputFiles
    FileCollection mavenClasspath


    /**
     * Configure the task's execution precondition and input depending on the project's extension.
     *
     * @param pExtension    The extension to add input dependencies on.
     */
    void init(MavenImportExtension pExtension)
    {
        // Only execute the task if something should be converted.
        onlyIf { convertRepositories || convertDependencies }

        // The extension's settings file is an input file.
        inputs.file({ -> pExtension.settingsFile });

        // The extension's scope mapping is an input property.
        inputs.property('scopeMapping', { -> pExtension.scopeToConfiguration });

        // The extension's external library versions are input properties.
        inputs.property('mavenVersion', { -> pExtension.mavenVersion });
        inputs.property('sisuPlexusVersion', { -> pExtension.sisuPlexusVersion });
        inputs.property('sisuGuiceVersion', { -> pExtension.sisuGuiceVersion });
    }


    /**
     * Get the specification for the pom file to import dependencies from. The default value is a
     * file named &quot;pom.xml&quot; in the project directory.
     *
     * @return  The pom file specification.
     */
    @InputFile
    File getPomFile()
    {
        if (fPomFile == null)
            fPomFile = project.file('pom.xml');

        return fPomFile;
    }


    /**
     * Set the path of the pom file to import dependencies from. The specified path will be resolved
     * relative to the project directory.
     *
     * @param pPath The pom file path. Passing null will restore the default value the next time
     *              {@code getPomFile} is called.
     */
    void setPomFile(Object pPath)
    {
        fPomFile = pPath ? project.file(pPath) : null;
    }


    /**
     * Get the specification for the destination file to create. The default value is a file named
     * &quot;dependencies.gradle&quot; in the same directory as the pom file.
     *
     * @return  The destination file specification.
     */
    @OutputFile
    File getDestination()
    {
        if (fDestination == null)
            fDestination = new File(getPomFile().parentFile, 'dependencies.gradle');

        return fDestination;
    }


    /**
     * Set the path of the destination file to create. The specified path will be resolved relative
     * to the project directory.
     *
     * @param pPath The destination file path. Passing null will restore the default value the next
     *              time {@code getDestination} is called.
     */
    void setDestination(Object pPath)
    {
        fDestination = pPath ? project.file(pPath) : null;
    }


    /**
     * Import repositories and/or dependencies from the pom file specified in the {@code pomFile}
     * property and write them on Gradle notation to the file specified in the {@code destination}
     * property.
     */
    @TaskAction
    void convertPom()
    {
        File aDestination = getDestination();
        if (!aDestination.exists() || overwrite)
        {
            // The destination file doesn't exist, or the task is configured to overwrite existing
            // destinations, it's OK to import and write.
            PomImporter aImporter = new PomImporter(project, getPomFile(), { mavenClasspath });
            writeDestination(aDestination, importRepositories(aImporter), importDependencies(aImporter));
        }
        else
        {
            logger.warn('Destination file \'{}\' exists and overwrite property is false, nothing will be converted',
                        aDestination.absolutePath);
        }
    }


    /**
     * Import repositories from a pom file if the {@code convertRepositories} property is true.
     *
     * @param pImporter The importer that will load the pom file.
     *
     * @return  A collection of {@code MavenRepositorySpec}, possibly empty, never null.
     */
    private Collection<MavenRepositorySpec> importRepositories(PomImporter pImporter)
    {
        if (convertRepositories)
        {
            Collection<MavenRepositorySpec> aRepositories = pImporter.importRepositories();
            logger.debug('Imported {} repositories from \'{}\'', aRepositories.size(), pImporter.pomFile.absolutePath);
            return aRepositories;
        }
        else
            return [];
    }


    /**
     * Import dependencies from a pom file if the {@code convertDependencies} property is true.
     *
     * @param pImporter The importer that will load the pom file.
     *
     * @return  A collection of {@code DependencySpec}, possibly empty, never null.
     */
    private Collection<DependencySpec> importDependencies(PomImporter pImporter)
    {
        if (!convertDependencies)
            return [];

        // Import from the pom file.
        Collection<DependencySpec> aDependencies = pImporter.importDependencies();
        logger.debug('Imported {} dependencies from \'{}\'', aDependencies.size(), pImporter.pomFile.absolutePath);

        // Find the dependencies that have a configuration that doesn't exist in the project and log
        // a warning for them.
        aDependencies.findAll {
            project.configurations.findByName(it.configuration) == null;
        }.each {
            logger.warn('The configuration \'{}\' for dependency \'{}\' does not exist',
                        it.configuration,
                        it.asStringNotation());
        }

        return aDependencies;
    }


    /**
     * Write repository and dependency specifications to a destination file.
     *
     * @param pDestination  The destination file.
     * @param pRepositories The repository specifications to write.
     * @param pDependencies The dependency specifications to write.
     */
    private void writeDestination(
            File pDestination,
            Collection<MavenRepositorySpec> pRepositories,
            Collection<DependencySpec> pDependencies)
    {
        int aNumEntities = pRepositories.size() + pDependencies.size();
        if (aNumEntities > 0)
        {
            // Make sure the destination file's parent directories exist.
            pDestination.parentFile?.mkdirs();

            logger.debug('Writing imported entities to \'{}\'', pDestination.absolutePath);
            pDestination.withPrintWriter
            {
                new RepositoryPrettyPrinter(it).printRepositories(pRepositories);
                new DependencyPrettyPrinter(it).printDependencies(pDependencies);
            }
        }
        else
            logger.warn('Nothing converted and written to \'{}\'', pDestination.absolutePath);
    }
}
