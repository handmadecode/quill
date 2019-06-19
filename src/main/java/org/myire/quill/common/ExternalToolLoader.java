/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.util.function.Supplier;
import static java.util.Objects.requireNonNull;

import org.gradle.api.file.FileCollection;


/**
 * An external tool loader enables loading the classes of an external tool from a dynamically
 * created class path. It also ensures that the classes referencing those external tool classes are
 * loaded by the same class loader, and not by the class loader that knows about the referencing
 * classes but not the external tool classes.
 *
 * @param <T>   The type that the external tool is accessed through, will not reference the external
 *              tool classes.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ExternalToolLoader<T>
{
    private final Class<T> fProxyClass;
    private final ExternalToolProxyClassLoader fToolClassLoader;

    private final String fProxyImplementationFqn;
    private Class<?> fProxyImplementationClass;


    /**
     * Create a new {@code ExternalToolLoader}.
     *
     * @param pProxyClass   The class (normally an interface) through which the external tool is
     *                      accessed.
     * @param pProxyImplementationPackage
     *                      The name of the package in which the proxy implementation class and any
     *                      helper classes reside. All classes in this package will be loaded by the
     *                      class loader that also loads the external tool classes.
     * @param pProxyImplementationClass
     *                      The name of the class that extends/implements the proxy class/interface
     *                      and is allowed to reference the external tool classes.
     * @param pToolClassPathSource
     *                      A file collection containing the external tool classes.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    public ExternalToolLoader(
        Class<T> pProxyClass,
        String pProxyImplementationPackage,
        String pProxyImplementationClass,
        Supplier<FileCollection> pToolClassPathSource)
    {
        fProxyClass = requireNonNull(pProxyClass);

        // Create a class loader that loads the external tool classes (from the file collection
        // returned by the tool classpath supplier) and the classes in the proxy implementation
        // package (which reference the external tool classes). The loading of all other classes is
        // delegated to the class loader of the proxy class/interface.
        fToolClassLoader =
            new ExternalToolProxyClassLoader(
                pToolClassPathSource.get().getFiles(),
                s -> s.startsWith(pProxyImplementationPackage),
                fProxyClass.getClassLoader());

        // Create the fully qualified class name of the implementation class.
        StringBuilder aBuilder = new StringBuilder(pProxyImplementationPackage);
        if (!pProxyImplementationPackage.endsWith("."))
            aBuilder.append('.');
        aBuilder.append(requireNonNull(pProxyImplementationClass));
        fProxyImplementationFqn = aBuilder.toString();
    }


    /**
     * Create an instance of the proxy implementation class.
     *
     * @return  A new {@code T} implementation, never null.
     *
     * @throws ClassNotFoundException   if the implementation class cannot be found.
     * @throws IllegalAccessException   if the implementation class' default constructor isn't
     *                                  accessible.
     * @throws InstantiationException   if the implementation class doesn't have a default
     *                                  constructor, of if it is abstract, or cannot be instantiated
     *                                  for some other reason.
     * @throws ClassCastException       if the proxy implementation class cannot be cast to the
     *                                  proxy interface/class.
     */
    public T createToolProxy() throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        // Load the implementation class if not done before. This is a benign data race since the
        // returned class is always the same.
        if (fProxyImplementationClass == null)
            fProxyImplementationClass = Class.forName(fProxyImplementationFqn, false, fToolClassLoader);

        Object aProxy = fProxyImplementationClass.newInstance();
        return fProxyClass.cast(aProxy);
    }
}
