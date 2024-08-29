/*
 * Copyright 2020, 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;


/**
 * Utility methods for creating {@code Provider} and {@code Property} instances.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class Providers
{
    static private final MethodHandle PROJECT_LAYOUT_FILE_PROPERTY;
    static private final MethodHandle PROJECT_LAYOUT_DIRECTORY_PROPERTY;
    static private final MethodHandle OBJECT_FACTORY_FILE_PROPERTY;
    static private final MethodHandle OBJECT_FACTORY_DIRECTORY_PROPERTY;

    // Lookup the methods for creating file and directory properties in the ProjectLayout and
    // ObjectFactory classes.
    static
    {
        MethodHandles.Lookup aLookup = MethodHandles.lookup();
        MethodType aRegularFilePropertyMethodType = MethodType.methodType(RegularFileProperty.class);
        MethodType aDirectoryPropertyMethodType = MethodType.methodType(DirectoryProperty.class);

        PROJECT_LAYOUT_FILE_PROPERTY =
            Invocations.lookupVirtualMethod(aLookup, ProjectLayout.class, "fileProperty", aRegularFilePropertyMethodType);
        PROJECT_LAYOUT_DIRECTORY_PROPERTY =
            Invocations.lookupVirtualMethod(aLookup, ProjectLayout.class, "directoryProperty", aDirectoryPropertyMethodType);
        OBJECT_FACTORY_FILE_PROPERTY =
            Invocations.lookupVirtualMethod(aLookup, ObjectFactory.class, "fileProperty", aRegularFilePropertyMethodType);
        OBJECT_FACTORY_DIRECTORY_PROPERTY =
            Invocations.lookupVirtualMethod(aLookup, ObjectFactory.class, "directoryProperty", aDirectoryPropertyMethodType);
    }


    /**
     * Create a new {@code RegularFileProperty}.
     *
     * @param pProject  The project to create the property with.
     *
     * @return  A new {@code RegularFileProperty}, or null if one couldn't be created. An error will
     *          be logged in the latter case.
     */
    static public RegularFileProperty createFileProperty(Project pProject)
    {
        RegularFileProperty aProperty = null;

        try
        {
            if (OBJECT_FACTORY_FILE_PROPERTY != null)
                aProperty = (RegularFileProperty) OBJECT_FACTORY_FILE_PROPERTY.invokeExact(pProject.getObjects());
            else if (PROJECT_LAYOUT_FILE_PROPERTY != null)
                aProperty = (RegularFileProperty) PROJECT_LAYOUT_FILE_PROPERTY.invokeExact(pProject.getLayout());
        }
        catch (Throwable t)
        {
            pProject.getLogger().error("Could not create a regular file property", t);
        }

        return aProperty;
    }


    /**
     * Create a new {@code DirectoryProperty}.
     *
     * @param pProject  The project to create the property with.
     *
     * @return  A new {@code DirectoryProperty}, or null if one couldn't be created. An error will
     *          be logged in the latter case.
     */
    static public DirectoryProperty createDirectoryProperty(Project pProject)
    {
        DirectoryProperty aProperty = null;

        try
        {
            if (OBJECT_FACTORY_DIRECTORY_PROPERTY != null)
                aProperty = (DirectoryProperty) OBJECT_FACTORY_DIRECTORY_PROPERTY.invokeExact(pProject.getObjects());
            else if (PROJECT_LAYOUT_DIRECTORY_PROPERTY != null)
                aProperty = (DirectoryProperty) PROJECT_LAYOUT_DIRECTORY_PROPERTY.invokeExact(pProject.getLayout());
        }
        catch (Throwable t)
        {
            pProject.getLogger().error("Could not create a directory property", t);
        }

        return aProperty;
    }
}
