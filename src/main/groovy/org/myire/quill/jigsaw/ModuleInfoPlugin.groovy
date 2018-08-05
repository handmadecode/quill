/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.JDepend
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

import org.myire.quill.common.Projects
import org.myire.quill.java.JavaAdditionsPlugin


/**
 * Gradle plugin for adding a &quot;compileModuleInfo&quot; task to the project. The task will be
 * configured to compile a &quot;module-info.java&quot; file in a pre-9 Java project and output the
 * resulting class into the output directory of the &quot;compileJava&quot; task.
 */
class ModuleInfoPlugin implements Plugin<Project>
{
    static private final String COMPILE_TASK_NAME = 'compileModuleInfo';
    static private final String DEFAULT_SOURCE = 'src/main/module-info/module-info.java';

    private Project fProject;
    private JavaCompile fTask;


    @Override
    void apply(Project pProject)
    {
        fProject = pProject;

        // Make sure the Java plugin is applied. This will create the compile tasks from which some
        // default configuration values are taken.
        pProject.plugins.apply(JavaPlugin.class);

        // Create and configure the compileModuleInfo task.
        fTask = createCompileModuleInfoTask();

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
    private JavaCompile createCompileModuleInfoTask()
    {
        JavaCompile aTask = fProject.tasks.create(COMPILE_TASK_NAME, JavaCompile.class);

        // Compiling module-info.java requires Java 9
        aTask.sourceCompatibility = '1.9';
        aTask.targetCompatibility = '1.9';

        // Specify the default location of the module-info.java file as the only source.
        aTask.setSource(DEFAULT_SOURCE);

        // Copy some configuration values from the main compile task.
        JavaCompile aMainCompileTask =
                Projects.getTask(fProject, JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);
        if (aMainCompileTask != null)
        {
            aTask.classpath = aMainCompileTask.classpath;
            aTask.destinationDir = aMainCompileTask.destinationDir;

            // The module info compile task requires the main java classes and thus depends on the
            // main java compile task .
            aTask.dependsOn(aMainCompileTask);
        }

        // The Jar file should include the module-info.class file, thus the Jar task depends on the
        // compile module-info task.
        Projects.getTask(fProject, JavaPlugin.JAR_TASK_NAME, Task.class)?.dependsOn(aTask);

        // Add an action that does some final configuration immediately before the task is executed.
        aTask.doFirst( { beforeExecution(it) } );

        return aTask;
    }


    /**
     * Do right-before-execution configuration of a &quot;compileModuleInfo&quot; task.
     *
     * @param pCompileModuleInfoTask    The task.
     */
    private void beforeExecution(JavaCompile pCompileModuleInfoTask)
    {
        // Set the module path to the main compile task's classpath to make any modules read by the
        // module being compiled available.
        pCompileModuleInfoTask.options.compilerArgs= ['--module-path', pCompileModuleInfoTask.classpath.asPath];
        pCompileModuleInfoTask.classpath = fProject.files();
    }


    /**
     * Add the source(s) of the &quot;compileModuleInfo&quot; task to the &quot;sourcesJar&quot;
     * task added by the {@code JavaAdditionsPlugin}, if available.
     */
    private void addModuleInfoToSourcesJar()
    {
        Jar aSourcesJarTask =
                Projects.getTask(fProject, JavaAdditionsPlugin.SOURCES_JAR_TASK_NAME, Jar.class);
        aSourcesJarTask?.from(fTask.source);
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
            File aModuleInfoClass = new File(fTask.destinationDir, 'module-info.class');
            File aModuleInfoTmp = new File(fTask.destinationDir, 'module-info.tmp');

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
