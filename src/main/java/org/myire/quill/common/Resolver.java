/*
 * Copyright 2017, 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import static java.util.Objects.requireNonNull;



/**
 * A resolver resolves objects of various types to a single type. This base implementation simply
 * checks if objects are instances of the type to resolve to, and returns the object cast to the
 * type if that is the case.
 *<p>
 * Instances of {@code Callable} and {@code Supplier} are handled by recursively resolving the
 * object returned from the {@code call} or {@code get} method.
 *
 * @param <T>   The type to resolve objects to.
 */
public class Resolver<T>
{
    private final Class<T> fType;


    /**
     * Create a new {@code Resolver}.
     *
     * @param pType The type objects will be resolved to.
     *
     * @throws NullPointerException if {@code pType} is null.
     */
    public Resolver(Class<T> pType)
    {
        fType = requireNonNull(pType);
    }


    /**
     * Resolve an object into the type this instance operates on.
     *
     * @param pObject   The object to resolve.
     *
     * @return  The object as a {@code T} instance, or null if {@code pObject} is null or is not
     *          resolvable into a {@code T} instance.
     */
    public T resolve(Object pObject)
    {
        if (pObject == null)
            return null;
        else if (fType.isInstance(pObject))
            return fType.cast(pObject);
        else if (pObject instanceof Callable)
            return resolve(uncheckedCall((Callable) pObject));
        else if (pObject instanceof Supplier)
            return resolve(((Supplier) pObject).get());
        else
            return null;
    }


    /**
     * Invoke the {@code call()} method on a {@code Callable} and rethrow any thrown
     * {@code Exception} wrapped in a {@code RuntimeException}.
     *
     * @param pCallable The instance to invoke  {@code call()} on.
     *
     * @return  The result from {@code call()}.
     *
     * @throws RuntimeException if {@code call()} throws.
     */
    static private Object uncheckedCall(Callable<?> pCallable)
    {
        try
        {
            return pCallable.call();
        }
        catch (Exception e)
        {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;
            else
                throw new RuntimeException(e);
        }
    }
}
