/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.JDepend
import org.gradle.api.tasks.bundling.Jar

import org.myire.quill.common.Projects
import org.myire.quill.java.JavaAdditionsPlugin


/**
 * Gradle plugin for adding a &quot;compileModuleInfo&quot; task to the project. The task will be
 * configured to compile a &quot;module-info.java&quot; file in a pre-9 Java project and output the
 * resulting class into the output directory of the &quot;compileJava&quot; task.
 *<p>
 * The plugin also adds a &quot;moduleMainClass&quot; task that, if a main class is specified,
 * updates the jar file with a main class attribute.
 */
class ModuleInfoPlugin implements Plugin<Project>
{
    static private final String COMPILE_TASK_NAME = 'compileModuleInfo';
    static private final String MAIN_CLASS_TASK_NAME = 'moduleMainClass';

    private Project fProject;
    private CompileModuleInfoTask fCompileModuleInfoTask;


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the Java plugin is applied. This will create the compile and jar tasks on which
        // this plugin's tasks depend.
        pProject.plugins.apply(JavaPlugin.class);

        // Create and configure the compileModuleInfo task.
        fCompileModuleInfoTask = createCompileModuleInfoTask();

        // Create and configure the moduleMainClass task.
        createModuleMainClassTask();

        // Add the module-info.java file to the sources jar file if the task for creating that jar
        // is available when the project has been evaluated.
        pProject.afterEvaluate( { addModuleInfoToSourcesJar() } );

        // Exclude the &quot;module-info.class&quot; from FindBugs and JDepend analysis if any such
        // tasks are available in the project. These tools can (currently) not read class files
        // newer than version 52 (Java 8).
        pProject.afterEvaluate( { excludeModuleInfoFromTasks(FindBugs.class) } );
        pProject.afterEvaluate( { excludeModuleInfoFromTasks(JDepend.class) } );
    }


    /**
     * Create the &quot;compileModuleInfo&quot; task in the plugin's project.
     *
     * @return  The created task.
     */
    private CompileModuleInfoTask createCompileModuleInfoTask()
    {
        CompileModuleInfoTask aTask = fProject.tasks.create(COMPILE_TASK_NAME, CompileModuleInfoTask.class);
        aTask.init();

        // The module info compile task requires the main java classes and thus depends on the main
        // java compile task.
        Task aMainCompileTask = Projects.getTask(fProject, JavaPlugin.COMPILE_JAVA_TASK_NAME, Task.class);
        if (aMainCompileTask != null)
            aTask.dependsOn(aMainCompileTask);

        // The Jar file should include the module-info.class file, thus the Jar task depends on the
        // compileModuleInfo task.
        Projects.getTask(fProject, JavaPlugin.JAR_TASK_NAME, Task.class)?.dependsOn(aTask);

        return aTask;
    }


    /**
     * Create the &quot;moduleMainClass&quot; task in the plugin's project.
     */
    private void createModuleMainClassTask()
    {
        ModuleMainClassTask aTask = fProject.tasks.create(MAIN_CLASS_TASK_NAME, ModuleMainClassTask.class);
        aTask.init();

        Task aJarTask = Projects.getTask(fProject, JavaPlugin.JAR_TASK_NAME, Task.class);
        if (aJarTask != null)
            // The moduleMainClass task operates on the jar file and thus depends on the Jar task.
            aTask.dependsOn(aJarTask);

        // The assemble task should depend on the moduleMainClass task to have the jar's main class
        // attribute set when the project's artifacts are assembled.
        Projects.getTask(fProject, BasePlugin.ASSEMBLE_TASK_NAME, Task.class)?.dependsOn(aTask);
    }


    /**
     * Add the source(s) of the &quot;compileModuleInfo&quot; task to the &quot;sourcesJar&quot;
     * task added by the {@code JavaAdditionsPlugin}, if available.
     */
    private void addModuleInfoToSourcesJar()
    {
        Jar aSourcesJarTask =
                Projects.getTask(fProject, JavaAdditionsPlugin.SOURCES_JAR_TASK_NAME, Jar.class);
        aSourcesJarTask?.from(fCompileModuleInfoTask.source);
    }


    /**
     * Exclude the &quot;module-info.class&quot; file from all tasks of a certain type. This is done
     * by renaming the file before the task executes, and renaming it back when the task is
     * finished.
     */
    private void excludeModuleInfoFromTasks(Class<? extends Task> pTaskClass)
    {
        fProject.tasks.withType(pTaskClass)
        {
            File aModuleInfoClass = new File(fCompileModuleInfoTask.destinationDir, 'module-info.class');
            File aModuleInfoTmp = new File(fCompileModuleInfoTask.destinationDir, 'module-info.tmp');

            doFirst {
                if (aModuleInfoClass.exists())
                    aModuleInfoClass.renameTo(aModuleInfoTmp);
            }

            doLast {
                if (aModuleInfoTmp.exists())
                    aModuleInfoTmp.renameTo(aModuleInfoClass);
            }
        }
    }
}
