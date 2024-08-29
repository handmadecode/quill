/*
 * Copyright 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

import org.myire.quill.common.Projects
import org.myire.quill.meta.ProjectMetaDataExtension
import org.myire.quill.meta.ProjectMetaDataPlugin



/**
 * Utility methods for the tasks of the {@code module-info} plugin.
 */
class ModuleInfoUtil
{
    static private final String MODULE_INFO_CLASS_FILE_NAME = "module-info.class"
    static private final String TEMPORARY_JAR_FILE_NAME = 'module-main-class-tmp.jar';


    /**
     * Create a {@code Provider} for the name of the main class. Its default value is the main class
     * name specified in the {@code ProjectMetaDataExtension}, if available in the project.
     *
     * @param pProject  The project possibly having the project meta data extension.
     *
     * @return  A new {@code Provider}.
     */
    static Provider<String> createDefaultMainClassNameProvider(Project pProject)
    {
        return pProject.provider {
            ProjectMetaDataExtension aExtension =
                    Projects.getExtension(
                            pProject,
                            ProjectMetaDataPlugin.PROJECT_META_EXTENSION_NAME,
                            ProjectMetaDataExtension.class);
            return aExtension?.mainClass;
        };
    }


    /**
     * Add the {@code ModuleMainClass} attribute to a &quot;module-info.class&quot; file.
     *
     * @param pProject          The project with which to execute the {@code jar} command.
     * @param pClassesDirectory The directory where the &quot;module-info.class&quot; file is
     *                          located together with any other class directories and files it
     *                          refers to.
     * @param pMainClassName    The value for the {@code ModuleMainClass} attribute.
     */
    static void addModuleMainClassAttribute(
            Project pProject,
            Directory pClassesDirectory,
            String pMainClassName)
    {
        File aModuleInfoClassFile = new File(pClassesDirectory.asFile, MODULE_INFO_CLASS_FILE_NAME);
        if (aModuleInfoClassFile.exists())
        {
            // Create a temporary jar file that adds the main-class attribute (there doesn't seem to
            // be a way to do this when compiling the module-info.class file).
            pProject.logger.info("Creating temporary jar file " + TEMPORARY_JAR_FILE_NAME + " in " + pClassesDirectory);
            pProject.exec {
                executable 'jar'
                workingDir pClassesDirectory
                args '-c', '--main-class=' + pMainClassName, '-f', TEMPORARY_JAR_FILE_NAME
                args getSubDirectoryAndClassFileNames(pClassesDirectory)
            }

            // Extract the updated module-info.class file from the temporary jar file, replacing the
            // compiled file.
            pProject.logger.info("Extracting updated " + MODULE_INFO_CLASS_FILE_NAME + " file from " + TEMPORARY_JAR_FILE_NAME);
            pProject.exec {
                workingDir pClassesDirectory
                executable 'jar'
                args '-x', '-f', TEMPORARY_JAR_FILE_NAME, MODULE_INFO_CLASS_FILE_NAME
            }

            // Delete the temporary jar file.
            File aTemporaryJarFile = new File(pClassesDirectory.asFile, TEMPORARY_JAR_FILE_NAME);
            pProject.logger.info("Deleting " + aTemporaryJarFile);
            pProject.delete(aTemporaryJarFile);
        }
        else
        {
            pProject.logger.warn(
                    "The file " +
                    aModuleInfoClassFile +
                    " does not exist, cannot add ModuleMainClass attribute");
        }
    }


    static private List<String> getSubDirectoryAndClassFileNames(Directory pDirectory)
    {
        List<String> aFileNames = [];
        pDirectory.asFile.listFiles().each {
            File f ->
                String aFileName = f.getName();
                if (f.isDirectory() || aFileName.endsWith(".class"))
                    aFileNames.add(aFileName);
        }

        return aFileNames;
    }
}
