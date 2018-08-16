/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.bundling.Jar

import org.myire.quill.common.Projects
import org.myire.quill.meta.ProjectMetaDataExtension
import org.myire.quill.meta.ProjectMetaDataPlugin


/**
 * Task that updates the jar file produced by the Jar task with a main class attribute.
 */
class ModuleMainClassTask extends Exec
{
    private String fClassName;


    /**
     * Initialize the task by setting default values for some properties and adding an action for
     * setting the executable's arguments before the task is executed.
     */
    void init()
    {
        description = 'Updates the jar file with a main class attribute in the module-info class';
        workingDir = project.getBuildDir();
        executable = 'jar';

        doFirst( { specifyArguments() } );

        // Only execute the task if a class name is specified.
        onlyIf { getClassName() != null};
    }


    /**
     * Get the fully qualified name of the class to specify as main class when updating the jar.
     *
     * @return  The fully qualified class name, or null if not specified.
     */
    @Input
    @Optional
    String getClassName()
    {
        if (fClassName == null)
        {
            def aExtension =
                    Projects.getExtension(
                            project,
                            ProjectMetaDataPlugin.PROJECT_META_EXTENSION_NAME,
                            ProjectMetaDataExtension.class);
            fClassName = aExtension?.mainClass;
        }

        return fClassName;
    }


    /**
     * Set the fully qualified name of the class to specify as main class when updating the jar.
     *
     * @param pClassName    The fully qualified class name, possibly null.
     */
    void setClassName(String pClassName)
    {
        fClassName = pClassName;
    }


    /**
     * Set the arguments for this task's executable.
     */
    void specifyArguments()
    {
        Jar aJarTask = Projects.getTask(project, JavaPlugin.JAR_TASK_NAME, Jar.class);
        args('-u', "--main-class=${className}", '-f', aJarTask?.archivePath);
    }
}
