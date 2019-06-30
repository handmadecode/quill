/*
 * Copyright 2017-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.Projects;
import org.myire.quill.common.Tasks;
import org.myire.quill.dependency.DependencySpec;
import org.myire.quill.repository.RepositorySpec;


/**
 * A task that imports dependencies and/or repositories from a Maven pom file and writes them to an
 * output file on Gradle notation.
 */
public class PomConvertTask extends DefaultTask
{
    static final String TASK_NAME = "convertPom";

    static private final String TASK_DESCRIPTION =
        "Imports dependencies and/or repositories from a Maven pom file and writes them to a Gradle file";


    private File fPomFile;
    private File fDestination;
    private boolean fConvertRepositories = true;
    private boolean fConvertDependencies = true;
    private boolean fOverwrite = true;


    /**
     * Initialize the task with its execution precondition and inputs.
     *
     * @param pExtension    The extension to get input dependencies from.
     *
     * @throws NullPointerException if {@code pExtension} is null.
     */
    void init(MavenImportExtension pExtension)
    {
        setDescription(TASK_DESCRIPTION);

        // Only execute the task if something should be converted.
        onlyIf(ignore -> fConvertRepositories || fConvertDependencies);

        // The extension's settings file is an input file.
        Tasks.optionalInputFile(this, pExtension::getSettingsFile);

        // The extension's scope mapping is an input property.
        Tasks.inputProperty(this, "scopeMapping", pExtension::getScopeToConfiguration);

        // The extension's external library version is an input property.
        Tasks.inputProperty(this, "mavenVersion", pExtension::getMavenVersion);

        // The extension's maven classpath is an input file collection.
        Tasks.inputFiles(this, pExtension::getMavenClassPath);
    }


    /**
     * Should the repositories from the pom file be imported and converted? Default is true.
     */
    @Input
    public boolean isConvertRepositories()
    {
        return fConvertRepositories;
    }


    public void setConvertRepositories(boolean pConvertRepositories)
    {
        fConvertRepositories = pConvertRepositories;
    }


    /**
     * Should the dependencies from the pom file be imported and converted? Default is true.
     */
    @Input
    public boolean isConvertDependencies()
    {
        return fConvertDependencies;
    }


    public void setConvertDependencies(boolean pConvertDependencies)
    {
        fConvertDependencies = pConvertDependencies;
    }


    /**
     * Should the destination file be overwritten if it exists? If this property is false and the
     * destination file exists, the task will do nothing. Default is true.
     */
    @Input
    public boolean isOverwrite()
    {
        return fOverwrite;
    }


    public void setOverwrite(boolean pOverwrite)
    {
        fOverwrite = pOverwrite;
    }


    /**
     * Get the specification for the pom file to import from. The default value is a file named
     * &quot;pom.xml&quot; in the project directory.
     *
     * @return  The pom file specification.
     */
    @InputFile
    public File getPomFile()
    {
        if (fPomFile == null)
            fPomFile = getProject().file("pom.xml");

        return fPomFile;
    }


    /**
     * Set the path of the pom file to import from. The specified path will be resolved relative to
     * the project directory.
     *
     * @param pPath The pom file path. Passing null will cause the default value to be returned from
     *              {@code getPomFile}.
     */
    public void setPomFile(Object pPath)
    {
        fPomFile = pPath != null ? getProject().file(pPath) : null;
    }


    /**
     * Get the specification for the destination file to create. The default value is a file named
     * &quot;dependencies.gradle&quot; in the same directory as the pom file.
     *
     * @return  The destination file specification.
     */
    @OutputFile
    public File getDestination()
    {
        if (fDestination == null)
            fDestination = new File(getPomFile().getParentFile(), "dependencies.gradle");

        return fDestination;
    }


    /**
     * Set the path of the destination file to create. The specified path will be resolved relative
     * to the project directory.
     *
     * @param pPath The destination file path. Passing null will cause the default value to be
     *              returned from {@code getDestination}.
     */
    public void setDestination(Object pPath)
    {
        fDestination = pPath != null ? getProject().file(pPath) : null;
    }


    /**
     * Import repositories and/or dependencies from the pom file specified in the {@code pomFile}
     * property and write them on Gradle notation to the file specified in the {@code destination}
     * property.
     */
    @TaskAction
    public void convertPom()
    {
        File aDestination = getDestination();
        if (!aDestination.exists() || fOverwrite)
        {
            // The destination file doesn't exist, or the task is configured to overwrite existing
            // destinations, it's OK to import and write.
            PomImporter aImporter = PomImporter.getInstance(getProject(), getPomFile());
            writeDestination(aDestination, importRepositories(aImporter), importDependencies(aImporter));
        }
        else
        {
            getLogger().warn(
                "Destination file '{}' exists and overwrite property is false, nothing will be converted",
                aDestination.getAbsolutePath());
        }
    }


    /**
     * Import repositories from a pom file if the {@code convertRepositories} property is true.
     *
     * @param pImporter The importer that will load the pom file.
     *
     * @return  A collection of {@code RepositorySpec}, possibly empty, never null.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    private Collection<RepositorySpec> importRepositories(PomImporter pImporter)
    {
        if (fConvertRepositories)
        {
            Collection<RepositorySpec> aRepositories = pImporter.importRepositories();
            getLogger().debug(
                "Imported {} repositories from '{}'",
                aRepositories.size(),
                pImporter.getPomFile().getAbsolutePath());

            return aRepositories;
        }
        else
            return Collections.emptyList();
    }


    /**
     * Import dependencies from a pom file if the {@code convertDependencies} property is true.
     *
     * @param pImporter The importer that will load the pom file.
     *
     * @return  A collection of {@code DependencySpec}, possibly empty, never null.
     *
     * @throws GradleException  if loading the pom file fails.
     */
    private Collection<DependencySpec> importDependencies(PomImporter pImporter)
    {
        if (!fConvertDependencies)
            return Collections.emptyList();

        // Import from the pom file.
        Collection<DependencySpec> aDependencies = pImporter.importDependencies();
        getLogger().debug(
            "Imported {} dependencies from '{}'",
            aDependencies.size(),
            pImporter.getPomFile().getAbsolutePath());

        // Find the dependencies that have a configuration that doesn't exist in the project and log
        // a warning for them.
        for (DependencySpec aDependency : aDependencies)
        {
            String aConfiguration = aDependency.getConfiguration();
            if (getProject().getConfigurations().findByName(aConfiguration) == null)
            {
                getLogger().warn(
                    "The configuration '{}' for dependency '{}' does not exist",
                    aConfiguration,
                    aDependency.toDependencyNotation());
            }
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
            Collection<RepositorySpec> pRepositories,
            Collection<DependencySpec> pDependencies)
    {
        int aNumEntities = pRepositories.size() + pDependencies.size();
        if (aNumEntities > 0)
        {
            // Make sure the destination file's parent directories exist.
            Projects.ensureParentExists(pDestination);

            getLogger().debug(
                "Writing imported entities to '{}'",
                pDestination.getAbsolutePath());

            try (PrintWriter aWriter = new PrintWriter(pDestination))
            {
                GradlePrettyPrinter aPrettyPrinter = new GradlePrettyPrinter(aWriter);
                RepositorySpec.prettyPrintRepositories(aPrettyPrinter, pRepositories);
                DependencySpec.prettyPrintDependencies(aPrettyPrinter, pDependencies);
            }
            catch (IOException ioe)
            {
                getLogger().error(
                    "Could not write to file '{}'",
                    pDestination.getAbsolutePath(),
                    ioe);
            }
        }
        else
            getLogger().warn(
                "Nothing converted and written to '{}'",
                pDestination.getAbsolutePath());
    }
}
