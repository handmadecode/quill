/*
 * Copyright 2018, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
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


    private final Property<String> fModuleVersion = project.objects.property(String.class);
    private final Property<String> fMainClassName = project.objects.property(String.class);


    // Set property default values.
    {
        group = 'build';
        description = 'Compiles a module-info.java file into the destination directory of compileMainJava';

        fModuleVersion.set(createDefaultModuleVersionProvider(project));
        fMainClassName.set(ModuleInfoUtil.createDefaultMainClassNameProvider(project));
    }


    /**
     * Initialize the task by setting default values for some properties and adding an action for
     * setting the compiler arguments before the task is executed.
     */
    void init()
    {
        // Compiling module-info.java requires Java 9
        sourceCompatibility = JavaVersion.VERSION_1_9;
        targetCompatibility = JavaVersion.VERSION_1_9;

        // Specify the default location of the module-info.java file as the only source.
        setSource(DEFAULT_SOURCE);

        // Copy some configuration values from the main compile task.
        JavaCompile aMainCompileTask =
                Projects.getTask(project, JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);
        if (aMainCompileTask != null)
        {
            classpath = aMainCompileTask.classpath;
            destinationDirectory.set(aMainCompileTask.destinationDirectory);
        }
        else
            destinationDirectory.set(project.buildDir);

        // Add an action to configure the compiler arguments before the task is executed.
        doFirst( { configureCompilerArgs() } );

        // Add an action to add the ModuleMainClass attribute after the module-info.java file has
        // been compiled.
        doLast ( { addMainClassAttribute() } );
    }


    /**
     * Get the version string to compile into the module-info class, if any.
     *
     * @return  The version string.
     */
    @Input
    @Optional
    Property<String> getModuleVersion()
    {
        return fModuleVersion;
    }


    /**
     * Get the fully qualified name of the class to specify as main class in the {@code module-info}
     * class file, if any.
     *
     * @return  The fully qualified class name.
     */
    @Input
    @Optional
    Property<String> getMainClassName()
    {
        return fMainClassName;
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
        String aModuleVersion = fModuleVersion.getOrNull();
        if (aModuleVersion != null)
            aCompilerArgs += ['--module-version', aModuleVersion];

        project.logger.info("Setting compiler arguments " + aCompilerArgs);
        options.setCompilerArgs(aCompilerArgs);

        // Clear the classpath, only the module path is used.
        setClasspath(project.files());
    }


    /**
     * Add the {@code ModuleMainClass} attribute to the compiled &quot;module-info.class&quot; file
     * if the main class name property is set.
     */
    void addMainClassAttribute()
    {
        if (destinationDirectory.isPresent() && fMainClassName.isPresent())
            ModuleInfoUtil.addModuleMainClassAttribute(project, destinationDirectory.get(), fMainClassName.get());
    }


    static private Provider<String> createDefaultModuleVersionProvider(Project pProject)
    {
        return pProject.provider {
            String aVersion = pProject.version?.toString();
            return "unspecified" == aVersion ? null : aVersion;
        }
    }
}
