/*
 * Copyright 2018-2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import static java.util.Objects.requireNonNull;

import org.gradle.api.logging.Logging;


/**
 * A class loader for dynamically loading the classes of an external tool and the proxy class that
 * references the external tool classes.
 *<p>
 * This class loader is a solution to the following problem: a Gradle plugin or task uses an
 * external tool and allows the exact version of that tool to be specified at runtime, e.g. through
 * a configuration property. To achieve this, the external tool classes must be loaded from a
 * dynamically specified location. This means that the classes of the plugin or task that uses the
 * tool cannot reference the external tool classes directly, since that would trigger loading the
 * external tool classes when the plugin/task classes are loaded, which would occur before the
 * plugin/task has a chance to specify the location of the external tool.
 *<p>
 * Instead, the external tool classes are referenced only through a special proxy class that
 * encapsulates the functionality of the tool and exposes the result as types not defined in the
 * tool classes, e.g. standard JDK or Gradle types.
 *<p>
 * To further complicate things, some external tools may be loaded by the class loader that loaded
 * the plugin/task classes for other reasons than being accessed by the plugin/task. For example, if
 * the external tool is Maven, and the built-in {@code maven} plugin is applied to the build script,
 * the Maven classes may be loaded before the plugin/task attempts to load them, and may be of the
 * wrong version.
 *<p>
 * The external tool classes must therefore be loaded by another class loader than the one that
 * loaded the plugin/task that uses them. The proxy class must also be loaded by this class loader,
 * since it references the tool classes, and loading it will trigger loading the external tool
 * classes. But the proxy class provides the interface to the Gradle plugin or task that uses the
 * external tool, so it must somehow be available to the plugin/task. This is solved by having the
 * proxy class implement an interface that the plugin/task uses. The implementation of the
 * interface, i.e. the proxy class, is then loaded dynamically wit the class loader that also loads
 * the external tool classes.
 *<p>
 * To summarize, this class loader should:
 *<ul>
 * <li>Load the external tool classes from a dynamically specified location</li>
 * <li>Load the proxy class and any helper classes from the location used by the plugin/task's class
 *     loader</li>
 * <li>Delegate the loading of all other classes to the plugin/task's class loader (this is
 *     especially important for the interface that the proxy class implements; the plugin/task will
 *     have loaded it, and should this class loader load it as part of loading the proxy class, it
 *     will not be the same interface and a {@code ClassCastException} will be thrown)</li>
 *</ul>
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ExternalToolProxyClassLoader extends URLClassLoader
{
    private final Predicate<String> fProxyClassPredicate;
    private final URLClassLoader fExternalToolClassLoader;


    /**
     * Create a new {@code ExternalToolProxyClassLoader}.
     *
     * @param pExternalToolLocations    The locations to load the external tool classes from.
     * @param pProxyClassPredicate      A predicate that returns true for the proxy classes that
     *                                  should be loaded by this class loader.
     * @param pParent                   The parent class loader to delegate all other class loading
     *                                  to.
     *
     * @throws NullPointerException if {@code pExternalToolLocations} or
     *                              {@code pProxyClassPredicate} is null.
     *
     */
    public ExternalToolProxyClassLoader(
        Collection<File> pExternalToolLocations,
        Predicate<String> pProxyClassPredicate,
        ClassLoader pParent)
    {
        // This instance should load the proxy classes identified by the predicate from the same
        // location as the parent class loader loads classes from.
        super(getUrls(pParent), pParent);

        // The external tool classes are loaded with a separate class loader to ensure they aren't
        // picked up from the parent.
        fExternalToolClassLoader = new URLClassLoader(createUrls(pExternalToolLocations), null);

        fProxyClassPredicate = requireNonNull(pProxyClassPredicate);
    }


    @Override
    protected Class<?> loadClass(String pName, boolean pResolve) throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(pName))
        {
            // First try to load the class from the external tool class locations.
            try
            {
                return fExternalToolClassLoader.loadClass(pName);
            }
            catch (ClassNotFoundException cnfe)
            {
                // Not found in the external tool class locations, continue.
            }

            if (fProxyClassPredicate.test(pName))
            {
                // A proxy class, should be loaded by this instance and not by the parent, first
                // check if it has already been loaded.
                Class<?> aClass = findLoadedClass(pName);
                if (aClass == null)
                    // Load this class, if it can't be loaded a ClassNotFoundException will be
                    // thrown without letting the parent have a chance at loading the class.
                    aClass = findClass(pName);

                if (pResolve)
                    resolveClass(aClass);

                return aClass;
            }
            else
                // Not an external tool class or a proxy class, go through the normal class loader
                // hierarchy.
                return super.loadClass(pName, pResolve);
        }
    }


    /**
     * Create an array of {@code URL} instances from a collection of {@code File} instances.
     *
     * @param pFiles    The files to create {@code URL} instances from.
     *
     * @throws NullPointerException if {@code pFiles} is null.
     */
    static private URL[] createUrls(Collection<File> pFiles)
    {
        return pFiles
            .stream()
            .map(ExternalToolProxyClassLoader::fileToUrl)
            .filter(Objects::nonNull)
            .toArray(URL[]::new);
    }


    /**
     * Get the {@code URL} instances a {@code ClassLoader} loads classes from.
     *
     * @param pClassLoader  The class loader to get the {@code URL} locations from.
     *
     * @return  An array of {@code URL} instances, possibly empty, never null.
     */
    static private URL[] getUrls(ClassLoader pClassLoader)
    {
        if (pClassLoader instanceof URLClassLoader)
            return ((URLClassLoader) pClassLoader).getURLs();
        else
            return new URL[0];
    }


    /**
     * Convert a {@code File} to a {@code URL}.
     *
     * @param pFile The file to convert.
     *
     * @return  The file as a url, or null if the conversion failed.
     */
    static private URL fileToUrl(File pFile)
    {
        try
        {
            return pFile.toURI().toURL();
        }
        catch (MalformedURLException mue)
        {
            Logging.getLogger(ExternalToolProxyClassLoader.class).debug(
                "Cannot convert file " + pFile + " to an URL, not adding to class loader");
            return null;
        }
    }
}
