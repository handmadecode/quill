/*
 * Copyright 2015 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.check

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import org.myire.quill.common.ProjectAware
import org.myire.quill.common.Projects


/**
 * Abstract base class for enhancers that modify and extend the project extension and tasks of a
 * code quality plugin.
 *
 * @param T   The type of task this enhancer operates on.
 */
abstract class AbstractPluginEnhancer<T extends Task> extends ProjectAware
{
    private final String fToolName;


    /**
     * Create a new {@code AbstractPluginEnhancer}.
     *
     * @param pProject      The enhanced plugin's project.
     * @param pPluginClass  The plugin's class.
     * @param pToolName     The plugin's tool name.
     */
    protected AbstractPluginEnhancer(Project pProject,
                                     Class<? extends Plugin<Project>> pPluginClass,
                                     String pToolName)
    {
        super(pProject);
        fToolName = pToolName;

        // Make sure the task's plugin is applied.
        pProject.plugins.apply(pPluginClass);
    }


    /**
     * Configure the extension related to the task type this enhancer operates on and then enhance
     * all tasks in the project that have that type.
     */
    void enhance()
    {
        configureExtension();
        project.tasks.withType(getTaskClass()) { createTaskEnhancer(it).enhance() };
    }


    /**
     * Get the task class this enhancer operates on.
     *
     * @return  The task class.
     */
    abstract Class<T> getTaskClass();


    /**
     * Configure the task's extension with sensible defaults.
     */
    abstract void configureExtension();


    /**
     * Create a new enhancer for a task.
     *
     * @param pTask The task to enhance.
     *
     * @return  A new {@code AbstractTaskEnhancer}.
     */
    abstract AbstractCheckTaskEnhancer<T> createTaskEnhancer(T pTask);


    /**
     * Create a specification for a file in the project's temporary directory for the enhancer's
     * tool.
     *
     * @param pFileName The name of the file.
     *
     * @return  A {@code File} with the specification.
     */
    protected File createTemporaryFileSpec(String pFileName)
    {
        File aDirectory = Projects.createTemporaryDirectorySpec(project, fToolName.toLowerCase());
        return new File(aDirectory, pFileName);
    }
}
