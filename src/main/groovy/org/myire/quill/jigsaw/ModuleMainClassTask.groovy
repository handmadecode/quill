/*
 * Copyright 2018, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.jigsaw

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.JavaCompile

import org.myire.quill.common.Projects


/**
 * Task that updates a {@code module-info.class} file with a {@code ModuleMainClass} attribute.
 */
class ModuleMainClassTask extends DefaultTask
{
    private final Property<String> fMainClassName = project.objects.property(String.class);
    private final DirectoryProperty fClassesDirectory = project.objects.directoryProperty();


    // Set property default values.
    {
        group = 'build';
        description = 'Adds the ModuleMainClass attribute to a module-info class file';

        fMainClassName.set(ModuleInfoUtil.createDefaultMainClassNameProvider(project));
        fClassesDirectory.set(createDefaultClassesDirectoryProvider(project));
    }


    /**
     * Get the fully qualified name of the class to specify as main class in the {@code module-info}
     * class file.
     *
     * @return  The fully qualified class name.
     */
    @Input
    Property<String> getMainClassName()
    {
        return fMainClassName;
    }


    /**
     * Get the directory where the {@code module-info} class file to update is located, together
     * with any classes it depends on.
     *
     * @return  The directory containing the {@code module-info} class file.
     */
    @InputDirectory
    DirectoryProperty getClassesDirectory()
    {
        return fClassesDirectory
    }


    @TaskAction
    void addModuleMainClassAttribute()
    {
        if (fClassesDirectory.isPresent() && fMainClassName.isPresent())
            ModuleInfoUtil.addModuleMainClassAttribute(project, fClassesDirectory.get(), fMainClassName.get());
    }


    static private Provider<Directory> createDefaultClassesDirectoryProvider(Project pProject)
    {
        return pProject.provider {
            JavaCompile aMainCompileTask =
                    Projects.getTask(pProject, JavaPlugin.COMPILE_JAVA_TASK_NAME, JavaCompile.class);
            return aMainCompileTask?.destinationDirectory?.get();
        };
    }
}
