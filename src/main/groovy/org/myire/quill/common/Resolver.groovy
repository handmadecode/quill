/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common

import java.util.concurrent.Callable


/**
 * A resolver resolves objects of various types to a single type. This base implementation simply
 * checks if objects are instances of the type to resolve to, and returns the object cast to the
 * type if that is the case.
 *<p>
 * Instances of {@code Closure} and {@code Callable} are handled by recursively resolving the object
 * returned from the {@code call} method.
 *
 * @param <T>   The type to resolve objects to.
 */
class Resolver<T>
{
    private final Class<T> fType;


    /**
     * Create a new {@code Resolver}.
     *
     * @param pType The type objects will be resolved to.
     *
     * @throws NullPointerException if {@code pType} is null.
     */
    Resolver(Class<T> pType)
    {
        fType = Objects.requireNonNull(pType);
    }


    /**
     * Resolve an object into the type this instance operates on.
     *
     * @param pObject   The object to resolve.
     *
     * @return  The object as a {@code T} instance, or null if {@code pObject} is null or is not
     *          resolvable into a {@code T} instance.
     */
    T resolve(Object pObject)
    {
        if (pObject == null)
            return null;
        else if (fType.isInstance(pObject))
            return fType.cast(pObject);
        else if (pObject instanceof Callable)
            return resolve(((Callable) pObject).call());
        else if (pObject instanceof Closure)
            return resolve(((Closure) pObject).call());
        else
            return null;
    }
}
