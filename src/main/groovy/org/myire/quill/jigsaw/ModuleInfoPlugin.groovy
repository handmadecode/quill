/*
 * Copyright 2018, 2020-2021, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Jar

import org.myire.quill.common.Projects
import org.myire.quill.java.JavaAdditionsPlugin


/**
 * Gradle plugin for adding a &quot;compileModuleInfo&quot; task to the project. The task will be
 * configured to compile a &quot;module-info.java&quot; file in a pre-9 Java project and output the
 * resulting class into the output directory of the &quot;compileJava&quot; task.
 *<p>
 * The plugin also adds a &quot;addModuleMainClassAttribute&quot; task that updates a
 * &quot;module-info.class&quot; file with a {@code ModuleMainClass} attribute.
 */
class ModuleInfoPlugin implements Plugin<Project>
{
    static private final String COMPILE_TASK_NAME = 'compileModuleInfo';
    static private final String ADD_MAIN_CLASS_ATTRIBUTE_TASK_NAME = 'addModuleMainClassAttribute';


    private Project fProject;
    private TaskProvider<CompileModuleInfoTask> fCompileModuleInfoTaskProvider;


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the Java plugin is applied. This will create the compile and jar tasks on which
        // this plugin's tasks depend.
        pProject.plugins.apply(JavaPlugin.class);

        // Create and configure the compileModuleInfo task.
        fCompileModuleInfoTaskProvider = createCompileModuleInfoTask();

        // Create and configure the addModuleMainClassAttribute task.
        createModuleMainClassTask();

        // Add the module-info.java file to the sources jar file if the task for creating that jar
        // is available when the project has been evaluated.
        pProject.afterEvaluate( { addModuleInfoToSourcesJar() } );
    }


    /**
     * Create the &quot;compileModuleInfo&quot; task in the plugin's project.
     *
     * @return  The created task.
     */
    private TaskProvider<CompileModuleInfoTask> createCompileModuleInfoTask()
    {
        TaskProvider<CompileModuleInfoTask> aProvider =
                fProject.tasks.register(COMPILE_TASK_NAME, CompileModuleInfoTask.class, {t -> t.init()});

        // The module info compile task requires the main java classes to exist and thus depends on
        // the main java compile task.
        configureTask(JavaPlugin.COMPILE_JAVA_TASK_NAME, { t -> aProvider.get().dependsOn(t); });

        // The module-info.class file should be part of the classes produced by the project.
        configureTask(JavaPlugin.CLASSES_TASK_NAME, { t -> t.dependsOn(aProvider); });

        return aProvider;
    }


    /**
     * Create the &quot;addModuleMainClassAttribute&quot; task in the plugin's project.
     */
    private void createModuleMainClassTask()
    {
        TaskProvider<ModuleMainClassTask> aProvider =
            fProject.tasks.register(ADD_MAIN_CLASS_ATTRIBUTE_TASK_NAME, ModuleMainClassTask.class);

        // The add module main class attribute task requires the main java classes to exist and thus
        // depends on the main java compile task.
        configureTask(JavaPlugin.COMPILE_JAVA_TASK_NAME, { t -> aProvider.get().dependsOn(t); });
    }


    /**
     * Add the source(s) of the &quot;compileModuleInfo&quot; task to the &quot;sourcesJar&quot;
     * task added by the {@code JavaAdditionsPlugin}, if available.
     */
    private void addModuleInfoToSourcesJar()
    {
        Jar aSourcesJarTask =
                Projects.getTask(fProject, JavaAdditionsPlugin.SOURCES_JAR_TASK_NAME, Jar.class);
        aSourcesJarTask?.from(fCompileModuleInfoTaskProvider.get().source);
    }


    /**
     * Add a configuration action to a task in the plugin's project.
     *
     * @param pTaskName             The task's name.
     * @param pConfigurationAction  The configuration action.
     */
    private <T extends Task> void configureTask(String pTaskName, Action<? super T> pConfigurationAction)
    {
        try
        {
            fProject.tasks.named(pTaskName, pConfigurationAction);
        }
        catch (UnknownTaskException ute)
        {
            fProject.logger.warn("No task with name " + pTaskName + " found in project", ute);
        }
    }
}
