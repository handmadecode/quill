/*
 * Copyright 2024 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;


/**
 * Utility methods related to {@code java.lang.invoke}.
 */
public final class Invocations
{
    /**
     * Look up a virtual method in a class.
     *
     * @param pLookup       The instance to perform the lookup with.
     * @param pClass        The class to look up a virtual method in.
     * @param pMethodName   The name of the method to look up.
     * @param pMethodType   The method's parameters and return type.
     *
     * @return  A {@code MethodHandle} to the method if found, null if not found.
     *
     * @throws NullPointerException if any of the parameters is null.
     */
    static public MethodHandle lookupVirtualMethod(
        MethodHandles.Lookup pLookup,
        Class<?> pClass,
        String pMethodName,
        MethodType pMethodType)
    {
        try
        {
            return pLookup.findVirtual(pClass, pMethodName, pMethodType);
        }
        catch (ReflectiveOperationException | RuntimeException ignore)
        {
            return null;
        }
    }
}
