/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.compile.JavaCompile

import org.myire.quill.common.Projects


/**
 * Task for compiling a single &quot;module-info.java&quot; file.
 */
class CompileModuleInfoTask extends JavaCompile
{
    static private final String DEFAULT_SOURCE = 'src/main/module-info/module-info.java';
    static private final String UNSET_VERSION = '';

    private String fModuleVersion = UNSET_VERSION;


    /**
     * Initialize the task by setting default values for some properties and adding an action for
     * setting the compiler arguments before the task is executed.
     */
    void init()
    {
        description = 'Compiles a module-info.java file into the destination directory of compileMainJava';

        // Compiling module-info.java requires Java 9
        sourceCompatibility = '1.9';
        targetCompatibility = '1.9';

        // Specify the default location of the module-info.java file as the only source.
        setSource(DEFAULT_SOURCE);

        // Copy some configuration values from the main compile task.
        JavaCompile aMainCompileTask =
                Projects.getTask(project, JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);
        if (aMainCompileTask != null)
        {
            classpath = aMainCompileTask.classpath;
            setDestinationDir(aMainCompileTask.destinationDir);
        }

        // Add an action to configure the compiler arguments before the task is executed.
        doFirst( { configureCompilerArgs() } );
    }


    /**
     * Get the version string to compile into the module-info class.
     *
     * @return  The version string, or null to omit the version from the module-info class.
     */
    @Input
    @Optional
    String getModuleVersion()
    {
        if (fModuleVersion.is(UNSET_VERSION))
        {
            fModuleVersion = project.version?.toString();
            if (fModuleVersion == 'unspecified')
                fModuleVersion = null;
        }

        return fModuleVersion;
    }


    /**
     * Set the version string to compile into the module-info class.
     *
     * @param pModuleVersion    The version string, or null to omit the version from the module-info
     *                          class.
     */
    void setModuleVersion(String pModuleVersion)
    {
        fModuleVersion = pModuleVersion;
    }


    /**
     * Configure the compiler arguments.
     */
    void configureCompilerArgs()
    {
        // Set the module path to the main compile task's classpath to make any modules read by the
        // module being compiled available.
        List<String> aCompilerArgs = ['--module-path', classpath.asPath];

        // Specify the module version, if available.
        String aModuleVersion = getModuleVersion();
        if (aModuleVersion != null)
            aCompilerArgs += ['--module-version', aModuleVersion];

        options.setCompilerArgs(aCompilerArgs);

        // Clear the classpath, only the module path is used.
        setClasspath(project.files());
    }
}
