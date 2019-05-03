/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;

import org.myire.quill.common.GradlePrettyPrinter;
import org.myire.quill.common.PrettyPrintable;
import static org.myire.quill.common.PrettyPrintable.quote;


/**
 * Specification of a dependency exclusion.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
class ExclusionSpec implements PrettyPrintable
{
    static private final String KEY_EXCLUDE = "exclude";
    static private final String ATTRIBUTE_GROUP = "group";
    static private final String ATTRIBUTE_MODULE = "module";

    private final String fGroup;
    private final String fModule;


    /**
     * Create a new {@code ExclusionSpec}.
     *
     * @param pGroup    The exclusion's group.
     * @param pModule   The exclusion's module.
     *
     * @throws NullPointerException if both parameters are null.
     */
    ExclusionSpec(String pGroup, String pModule)
    {
        if (pGroup == null)
            requireNonNull(pModule);

        fGroup = pGroup;
        fModule = pModule;
    }


    public String getGroup()
    {
        return fGroup;
    }


    public String getModule()
    {
        return fModule;
    }


    /**
     * Create a {@code Map} with the exclusion attributes.
     *
     * @return  A new Map, never null.
     */
    Map<String, String> toMap()
    {
        if (fGroup != null)
        {
            if (fModule != null)
            {
                // Both group and module specified.
                Map<String, String> aMap = new LinkedHashMap<>();
                aMap.put(ATTRIBUTE_GROUP, fGroup);
                aMap.put(ATTRIBUTE_MODULE, fModule);
                return aMap;
            }
            else
                // Only group specified.
                return Collections.singletonMap(ATTRIBUTE_GROUP, fGroup);
        }
        else
            // Only module specified.
            return Collections.singletonMap(ATTRIBUTE_MODULE, fModule);
    }


    @Override
    public void prettyPrint(GradlePrettyPrinter pPrinter)
    {
        pPrinter.printMethodCall(KEY_EXCLUDE, toMapNotation(), false, false);
    }


    /**
     * Create a string with the exclusion attributes on map notation.
     *
     * @return  A string on the format &quot;group: 'org.myire', module: 'quill'&quot;.
     */
    private String toMapNotation()
    {
        if (fGroup != null)
        {
            if (fModule != null)
                // Both group and module specified.
                return ATTRIBUTE_GROUP + ": " + quote(fGroup) + ", " + ATTRIBUTE_MODULE + ": " + quote(fModule);
            else
                // Only group specified.
                return ATTRIBUTE_GROUP + ": " + quote(fGroup);
        }
        else
            // Only module specified.
            return ATTRIBUTE_MODULE + ": " + quote(fModule);
    }
}
