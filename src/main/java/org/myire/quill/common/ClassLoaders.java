/*
 * Copyright 2016-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

package org.myire.quill.common;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.Logging;


/**
 * Class loader related utility methods.
 */
public final class ClassLoaders
{
    /**
     * Private constructor to disallow instantiations of utility method class.
     */
    private ClassLoaders()
    {
        // Empty default ctor, defined to override access scope.
    }


    /**
     * Inject the files in a {@code FileCollection} into a {@code ClassLoader}, thus adding them to
     * the locations where the class loader looks for classes to load.
     *<p>
     * If the specified class loader isn't a (subclass of) {@code java.net.URLClassLoader}, this
     * method is a no-op.
     *
     * @param pClassLoader      The class loader to inject files into.
     * @param pFileCollection   The files to inject.
     *
     * @throws RuntimeException if any reflective operation used for injecting the files fails.
     */
    @Deprecated
    static public void inject(ClassLoader pClassLoader, FileCollection pFileCollection)
    {
        if (pClassLoader instanceof URLClassLoader)
        {
            try
            {
                // Get the 'addURL' method from the class loader's class.
                Method aMethod = findAddUrlMethod(pClassLoader.getClass());

                for (File aFile : pFileCollection.getFiles())
                {
                    try
                    {
                        aMethod.invoke(pClassLoader, aFile.toURI().toURL());
                    }
                    catch (MalformedURLException mue)
                    {
                        Logging.getLogger(ClassLoaders.class).debug(
                            "Cannot convert file " + aFile + " to an URL, not adding to class loader");
                    }
                }
            }
            catch (ReflectiveOperationException e)
            {
                throw new RuntimeException(
                    "Could not injects files into class loader " + pClassLoader.getClass().getName(),
                    e);
            }
        }
        else
            Logging.getLogger(ClassLoaders.class).warn(
                "Cannot inject files into a " + pClassLoader.getClass().getName());
    }


    /**
     * Find the &quot;addURL&quot; method in a class or one of its superclasses and make sure it is
     * accessible.
     *
     * @param pClass    The class to find the method in.
     *
     * @return  The &quot;addURL&quot; method.
     *
     * @throws NoSuchMethodException    if the &quot;addURL&quot; method isn't found in the
     *                                  specified class loader or any of its superclasses.
     */
    static private Method findAddUrlMethod(Class<?> pClass) throws NoSuchMethodException
    {
        try
        {
            Method aMethod = pClass.getDeclaredMethod("addURL", URL.class);
            if (!aMethod.isAccessible())
                aMethod.setAccessible(true);
            return aMethod;
        }
        catch (NoSuchMethodException e)
        {
            Class<?> aSuperClass = pClass.getSuperclass();
            if (aSuperClass != null)
                return findAddUrlMethod(aSuperClass);
            else
                throw e;
        }
    }
}
