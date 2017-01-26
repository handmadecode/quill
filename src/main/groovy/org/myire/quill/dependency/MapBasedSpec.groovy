/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency

/**
 * Base class for specifications with map-based values. The specification contains a set of
 * key-value pairs that can be formatted as a string on <i>map notation</i>.
 */
class MapBasedSpec
{
    /**
     * The specification's map-based values.
     */
    final Map<String, String> mapping;


    /**
     * Create a new {@code MapBasedSpec}.
     *
     * @param pInitialValues    The mapping's initial values. The entries in this map will be copied
     *                          to this instance's mapping. Keys mapped to null will be ignored.
     */
    MapBasedSpec(Map<String, String> pInitialValues)
    {
        mapping = pInitialValues.findAll { it.value != null }
    }


    /**
     * Get the value of a key as a boolean.
     *
     * @param pKey  The key to get the value for.
     *
     * @return  True if the key has the value &quot;true&quot;, false if it has any other value or
     *          has no value in the mapping.
     */
    boolean getBoolean(String pKey)
    {
        return mapping[pKey] == 'true';
    }


    /**
     * Set value of a key to a boolean value.
     *
     * @param pKey      The key.
     * @param pValue    The value.
     */
    void putBoolean(String pKey, boolean pValue)
    {
        mapping[pKey] = pValue ? 'true' : 'false';
    }


    /**
     * Set the value for a key.
     *
     * @param pKey      The key.
     * @param pValue    The value. If this value is null the key will be removed from the mapping.
     */
    void putValue(String pKey, String pValue)
    {
        if (pValue != null)
            mapping[pKey] = pValue;
        else
            mapping.remove(pKey);
    }


    /**
     * Get a string with the map notation for this specification.
     *
     * @return  A string on the format &quot;key: 'value', ...&quot;.
     */
    String asMapNotation()
    {
        StringBuilder aNotation = new StringBuilder(256);
        int aNumEntries = mapping.size();
        for (aEntry in mapping)
        {
            aNotation.append(aEntry.key);
            aNotation.append(': \'');
            aNotation.append(aEntry.value);
            if (--aNumEntries > 0)
                // More entries will follow, add comma separator.
                aNotation.append('\', ');
            else
                // No more entries.
                aNotation.append('\'');
        }

        return aNotation.toString();
    }
}
