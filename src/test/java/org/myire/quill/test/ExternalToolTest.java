/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.test;

import org.gradle.api.file.FileCollection;

import org.myire.quill.common.ExternalToolLoader;


/**
 * Base class for unit tests of external tools loaded through an {@code ExternalToolLoader}
 * instance.
 *
 * @param <T>   The type that the external tool is accessed through.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class ExternalToolTest<T> extends FileBasedTest
{
    private final ExternalToolLoader<T> fToolLoader;


    /**
     * Create a new {@code ExternalToolTest}.
     *
     * @param pToolProxyClass
     *                      The class (normally an interface) through which the external tool is
     *                      accessed.
     * @param pToolProxyImplementationPackage
     *                      The name of the package in which the proxy implementation class and any
     *                      helper classes reside. All classes in this package will be loaded by the
     *                      class loader that also loads the external tool classes.
     * @param pToolProxyImplementationClassName
     *                      The name of the class that extends/implements the proxy class/interface
     *                      and is allowed to reference the external tool classes.
     * @param pToolJars     A file collection containing the jar file(s) with the external
     *                      tool's classes.
     */
    protected ExternalToolTest(
        Class<T> pToolProxyClass,
        String pToolProxyImplementationPackage,
        String pToolProxyImplementationClassName,
        FileCollection pToolJars)
    {
        fToolLoader =
            new ExternalToolLoader<>(
                pToolProxyClass,
                pToolProxyImplementationPackage,
                pToolProxyImplementationClassName,
                () -> pToolJars);
    }


    /**
     * Create a new proxy instance for the external tool being tested.
     *
     * @return  A new {@code T} instance.
     */
    protected T newToolProxy()
    {
        try
        {
            return fToolLoader.createToolProxy();
        }
        catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)
        {
            throw new RuntimeException(e);
        }
    }
}
