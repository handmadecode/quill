/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency

/**
 * Specification of a dependency exclusion.
 */
class ExclusionSpec extends MapBasedSpec
{
    /**
     * Create a new {@code ExclusionSpec}.
     *
     * @param pGroup    The exclusion's group.
     * @param pModule   The exclusion's module.
     */
    ExclusionSpec(String pGroup, String pModule)
    {
        super(group: pGroup, module: pModule);
    }
}
