/*
 * Copyright 2014 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.TemporaryFileProvider
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.util.GFileUtils


/**
 * Project related utility methods.
 */
final class Projects
{
    /**
     * Get a source set from a project.
     *
     * @param pProject  The project.
     * @param pName     The name of the source set.
     *
     * @return  The source set with the specified name, or null if there is no source set with that
     *          name in the project (including if the project has no source sets at all).
     */
    static SourceSet getSourceSet(Project pProject, String pName)
    {
        if (pProject.hasProperty("sourceSets"))
        {
            Object aProperty = pProject.property("sourceSets");
            if (aProperty instanceof SourceSetContainer)
                return aProperty.findByName(pName);
        }

        return null;
    }


    /**
     * Get a task from a project.
     *
     * @param pProject      The project.
     * @param pName         The name of the task.
     * @param pTaskClass    The task's class (or a superclass of its class).
     *
     * @return  The task with the specified name, or null if there is no task with that name in the
     *          project. Null is also returned if there is a task with the specified name that is
     *          not a subtype of the specified task class.
     */
    static <T extends Task> T getTask(Project pProject, String pName, Class<T> pTaskClass)
    {
        try
        {
            Task aTask = pProject.tasks.getByName(pName);
            if (aTask != null && pTaskClass.isAssignableFrom(aTask.getClass()))
                return (T) aTask;
        }
        catch (UnknownTaskException ignore)
        {
            // No such task
        }

        return null;
    }


    /**
     * Get an extension from a project.
     *
     * @param pProject  The project.
     * @param pName     The name of the extension.
     * @param pExtensionClass
     *                  The extension's class (or a superclass of its class).
     *
     * @return  The extension with the specified name, or null if there is no extension with that
     *          name in the project. Null is also returned if there is an extension with the
     *          specified name that is not a subtype of the specified extension class.
     */
    static <T> T getExtension(Project pProject, String pName, Class<T> pExtensionClass)
    {
        Object aExtension = pProject.extensions.findByName(pName);
        if (aExtension != null && pExtensionClass.isAssignableFrom(aExtension.getClass()))
            return (T) aExtension;
        else
            return null;
    }


    /**
     * Create a file specification for a file or directory within the project's main report
     * directory. If the specified project has the reporting extension installed, that extension's
     * {@code baseDir} property is used as the main reporting directory, otherwise the project's
     * build directory is used as the main reporting directory.
     *
     * @param pProject              The project to create the report directory spec for.
     * @param pDirectoryItemName    If non-null, the name of the directory item (file or
     *                              subdirectory) within the project's main report directory to
     *                              create the file spec for. If null, the file spec for the main
     *                              report directory will be returned.
     *
     * @return  A file spec for the specified report directory and directory item.
     */
    static File createReportDirectorySpec(Project pProject, String pDirectoryItemName)
    {
        ReportingExtension aExtension = getExtension(pProject, ReportingExtension.NAME, ReportingExtension.class);
        File aBaseDir = aExtension?.baseDir ?: pProject.buildDir;
        return pDirectoryItemName ? new File(aBaseDir, pDirectoryItemName) : aBaseDir;
    }


    /**
     * Create a file specification for a temporary directory.
     *
     * @param pProject              The project to create the temporary directory spec for.
     * @param pDirectoryItemName    If non-null, the name of the directory item (file or
     *                              subdirectory) within the project's temporary directory to create
     *                              the file spec for. If null, the file spec for the project's
     *                              temporary directory itself will be returned.
     *
     * @return  A file spec for the specified work directory.
     */
    static File createTemporaryDirectorySpec(Project pProject, String pDirectoryItemName)
    {
        if (pProject instanceof ProjectInternal)
        {
            TemporaryFileProvider aProvider =
                    ((ProjectInternal) pProject).services.get(TemporaryFileProvider.class);
            if (pDirectoryItemName != null)
                return aProvider.newTemporaryFile(pDirectoryItemName);
            else
                return aProvider.newTemporaryFile('placeholder').parentFile;
        }
        else
        {
            File aTmpDir = new File(pProject.buildDir, 'tmp');
            return pDirectoryItemName ? new File(aTmpDir, pDirectoryItemName) : aTmpDir;
        }
    }


    /**
     * Get a project's file resolver.
     *
     * @param pProject  The project to get the file resolver from.
     *
     * @return  The project's file resolver, or null if the project is not of a type known to have a
     *          file resolver.
     */
    static FileResolver getFileResolver(Project pProject)
    {
        if (pProject instanceof ProjectInternal)
            return ((ProjectInternal) pProject).fileResolver;
        else
            return null;
    }


    /**
     * Extract a classpath resource to a file. The resource will be accessed through the class
     * loader of the {@code Projects} class.
     *
     * @param pResource The name of the resource.
     * @param pFile     The file to extract the resource to.
     *
     * @return  True if the file was created and the resource extracted to it, false if the file
     *          already existed (in which case it is left unmodified).
     */
    static boolean extractResource(String pResource, File pFile)
    {
        if (pFile.exists())
            return false;

        pFile.parentFile.mkdirs();
        GFileUtils.copyURLToFile(Projects.class.getResource(pResource), pFile);

        return true;
    }
}
