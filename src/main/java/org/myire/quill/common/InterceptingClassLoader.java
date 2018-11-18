/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static java.util.Objects.requireNonNull;

import org.gradle.api.logging.Logging;


/**
 * A class loader that intercepts the loading of some classes before its parent is given a chance to
 * load them, and instead directly attempts to load those intercepted classes by itself.
 *<p>
 * The main use case for an {@code InterceptingClassLoader} is the dynamic loading of an external
 * library for which the version to load is specified at task configuration time. The classes of
 * such a library will be referenced only by a special proxy class that encapsulates the
 * functionality of the library and only exposes the result as types not defined in the library,
 * e.g. standard jDK och Gradle types.
 *<p>
 * The library classes must be loaded by a special class loader that looks for classes in a
 * dynamically specified location. The proxy class must also be loaded by this class loader, since
 * it will reference the library classes. Given that the proxy class provides the interface to the
 * Gradle plugin or task that uses the external library, it cannot be part of the library but will
 * be part of the Gradle plugin. This means that the plugin's or task's class loader will be able to
 * load the proxy class but not the library classes.
 *<p>
 * Enter the intercepting class loader: this class loader will load classes both from the
 * location(s) where the dynamically loaded library resides, and from its parent class loader's
 * locations. It will intercept the loading of all classes whose name matches a {@code Predicate},
 * and attempt to load those classes directly without passing them up in the class loader hierarchy
 * first. If the {@code Predicate} matches the proxy class' name, that class will be loaded by the
 * intercepting class loader even though its parent is able to load it.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class InterceptingClassLoader extends URLClassLoader
{
    private final Predicate<String> fInterceptPredicate;


    /**
     * Create a new {@code InterceptingClassLoader}.
     *
     * @param pLocations            The locations to load classes from in addition to the ones from
     *                              the parent class loader.
     * @param pInterceptPredicate   A predicate that filters out the class names to intercept.
     * @param pParent               The parent class loader.
     *
     * @throws NullPointerException if {@code pLocations} or {@code pInterceptPredicate} is null.
     *
     */
    public InterceptingClassLoader(
        Collection<File> pLocations,
        Predicate<String> pInterceptPredicate,
        ClassLoader pParent)
    {
        super(createUrls(pLocations, pParent), pParent);
        fInterceptPredicate = requireNonNull(pInterceptPredicate);
    }


    @Override
    protected Class<?> loadClass(String pName, boolean pResolve) throws ClassNotFoundException
    {
        if (fInterceptPredicate.test(pName))
        {
            // Intercept the loading of this class, first check if t has already been loaded.
            Class<?> aClass = findLoadedClass(pName);
            if (aClass == null)
                // Load this class, if it can't be loaded a ClassNotFoundException will be thrown
                // without letting the parent have a chance at loading the class.
                aClass = findClass(pName);

            if (pResolve)
                resolveClass(aClass);

            return aClass;
        }
        else
            // No interception, go through the normal class loader hierarchy. This instance may
            // still end up loading the class, but only after the parent has failed to do so.
            return super.loadClass(pName, pResolve);
    }


    /**
     * Create the {@code URL}s a new {@code InterceptingClassLoader} should load classes from.
     *
     * @param pLocations    The explicit locations to load classes from in addition to the ones from
     *                      the parent class loader.
     * @param pParent       The parent class loader.
     *
     * @throws NullPointerException if {@code pLocations} is null.
     */
    static private URL[] createUrls(Collection<File> pLocations, ClassLoader pParent)
    {
        List<URL> aURLs =
            pLocations.stream()
                .map(InterceptingClassLoader::fileToUrl)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (pParent instanceof URLClassLoader)
            Collections.addAll(aURLs, ((URLClassLoader) pParent).getURLs());

        return aURLs.toArray(new URL[0]);
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
            Logging.getLogger(InterceptingClassLoader.class).debug(
                "Cannot convert file " + pFile + " to an URL, not adding to class loader");
            return null;
        }
    }
}
