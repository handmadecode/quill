/*
 * Copyright 2020 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.common;

import org.gradle.api.NamedDomainObjectCollection;


/**
 * Utility methods not related to entities having their own utility classes (like {@link Projects}).
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public final class Util
{
    /**
     * Private constructor to disallow instantiations of utility method class.
     */
    private Util()
    {
        // Empty default ctor, defined to override access scope.
    }


    /**
     * Case-insensitive variant of {@code NamedDomainObjectCollection::findByName}.
     *
     * @param pCollection   The collection to find the object in.
     * @param pName         The name to look for.
     * @param <T>           The type of the collection's objects.
     *
     * @return  The found object, or null if no object in the collection has the specified name.
     */
    static public <T> T findByNameIgnoreCase(NamedDomainObjectCollection<T> pCollection, String pName)
    {
        for (String aName : pCollection.getNames())
            if (aName.equalsIgnoreCase(pName))
                return pCollection.findByName(aName);

        return null;
    }
}
