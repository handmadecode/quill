/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.ivy;

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
import org.myire.quill.configuration.ConfigurationSpec;
import org.myire.quill.dependency.DependencySpec;
import org.myire.quill.dependency.ModuleDependencySpec;


/**
 * A task that imports configurations and/or dependencies from an Ivy file and writes them to an
 * output file on Gradle notation.
 */
public class IvyFileConvertTask extends DefaultTask
{
    static final String TASK_NAME = "convertIvyModule";

    static private final String TASK_DESCRIPTION =
        "Imports configurations and/or dependencies from an Ivy module file and writes them to a Gradle file";


    private File fIvyFile;
    private File fDestination;
    private boolean fConvertConfigurations = true;
    private boolean fConvertDependencies = true;
    private boolean fOverwrite = true;


    /**
     * Initialize the task with the execution precondition and inputs.
     *
     * @param pExtension    The extension to get input dependencies from.
     *
     * @throws NullPointerException if any {@code pExtension} is null.
     */
    void init(IvyImportExtension pExtension)
    {
        setDescription(TASK_DESCRIPTION);

        // Only execute the task if something should be converted.
        onlyIf(ignore -> fConvertConfigurations || fConvertDependencies);

        // The extension's settings file is an input file.
        Tasks.inputFile(this, pExtension::getSettingsFile);

        // The extension's external library version is an input property.
        Tasks.inputProperty(this, "ivyVersion", pExtension::getIvyVersion);

        // The extension's Ivy classpath is an input file collection.
        Tasks.inputFiles(this, pExtension::getIvyClassPath);
    }


    /**
     * Should the configurations from the Ivy file be imported and converted? Default is true.
     */
    @Input
    public boolean isConvertConfigurations()
    {
        return fConvertConfigurations;
    }


    public void setConvertConfigurations(boolean pConvertConfigurations)
    {
        fConvertConfigurations = pConvertConfigurations;
    }


    /**
     * Should the dependencies from the Ivy file be imported and converted? Default is true.
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
     * Get the specification for the Ivy file to import from. The default value is a file named
     * &quot;ivy.xml&quot; in the project directory.
     *
     * @return  The Ivy file specification.
     */
    @InputFile
    public File getIvyFile()
    {
        if (fIvyFile == null)
            fIvyFile = getProject().file("ivy.xml");

        return fIvyFile;
    }


    /**
     * Set the path of the Ivy file to import from. The specified path will be resolved relative to
     * the project directory.
     *
     * @param pPath The Ivy file path. Passing null will cause the default value to be returned from
     *              {@code getIvyFile}.
     */
    public void setIvyFile(Object pPath)
    {
        fIvyFile = pPath != null ? getProject().file(pPath) : null;
    }


    /**
     * Get the specification for the destination file to create. The default value is a file named
     * &quot;dependencies.gradle&quot; in the same directory as the Ivy file.
     *
     * @return  The destination file specification.
     */
    @OutputFile
    public File getDestination()
    {
        if (fDestination == null)
            fDestination = new File(getIvyFile().getParentFile(), "dependencies.gradle");

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
     * Import configurations and/or dependencies from the Ivy file specified in the {@code ivyFile}
     * property and write them on Gradle notation to the file specified in the {@code destination}
     * property.
     */
    @TaskAction
    public void convertIvyFile()
    {
        File aDestination = getDestination();
        if (!aDestination.exists() || fOverwrite)
        {
            // The destination file doesn't exist, or the task is configured to overwrite existing
            // destinations, it's OK to import and write.
            IvyFileImporter aImporter = IvyFileImporter.getInstance(getProject(), getIvyFile());
            writeDestination(aDestination, importConfigurations(aImporter), importDependencies(aImporter));
        }
        else
        {
            getLogger().warn(
                "Destination file '{}' exists and overwrite property is false, nothing will be converted",
                aDestination.getAbsolutePath());
        }
    }


    /**
     * Import configurations from an Ivy file if the {@code convertConfigurations} property is true.
     *
     * @param pImporter The importer that will load the Ivy file.
     *
     * @return  A collection of {@code ConfigurationSpec}, possibly empty, never null.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    private Collection<ConfigurationSpec> importConfigurations(IvyFileImporter pImporter)
    {
        if (fConvertConfigurations)
        {
            Collection<ConfigurationSpec> aConfigurations = pImporter.importConfigurations();
            getLogger().debug(
                "Imported {} configurations from '{}'",
                aConfigurations.size(),
                pImporter.getIvyFile().getAbsolutePath());

            return aConfigurations;
        }
        else
            return Collections.emptyList();
    }


    /**
     * Import dependencies from an Ivy file if the {@code convertDependencies} property is true.
     *
     * @param pImporter The importer that will load the Ivy file.
     *
     * @return  A collection of {@code ModuleDependencySpec}, possibly empty, never null.
     *
     * @throws GradleException  if loading the Ivy file fails.
     */
    private Collection<ModuleDependencySpec> importDependencies(IvyFileImporter pImporter)
    {
        if (fConvertDependencies)
        {
            // Import from the Ivy file.
            Collection<ModuleDependencySpec> aDependencies = pImporter.importDependencies();
            getLogger().debug(
                "Imported {} dependencies from '{}'",
                aDependencies.size(),
                pImporter.getIvyFile().getAbsolutePath());

            return aDependencies;
        }
        else
            return Collections.emptyList();
    }


    /**
     * Write configuration and dependency specifications to a destination file.
     *
     * @param pDestination      The destination file.
     * @param pConfigurations   The configuration specifications to write.
     * @param pDependencies     The dependency specifications to write.
     */
    private void writeDestination(
        File pDestination,
        Collection<ConfigurationSpec> pConfigurations,
        Collection<ModuleDependencySpec> pDependencies)
    {
        int aNumEntities = pConfigurations.size() + pDependencies.size();
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
                ConfigurationSpec.prettyPrintConfigurations(aPrettyPrinter, pConfigurations);
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
