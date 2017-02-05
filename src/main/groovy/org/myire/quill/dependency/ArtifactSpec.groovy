/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.dependency

/**
 * Specification of a dependency artifact.
 */
class ArtifactSpec extends MapBasedSpec
{
    /**
     * Create a new {@code ArtifactSpec}.
     *
     * @param pName         The name of the artifact.
     * @param pType         The artifact's type. Normally the same value as the extension, e.g.
     *                      &quot;jar&quot;.
     * @param pExtension    The artifact's extension. Normally the same value as the type.
     * @param pClassifier   The artifact's classifier.
     * @param pUrl          The URL under which the artifact can be retrieved.
     */
    ArtifactSpec(String pName, String pType, String pExtension, String pClassifier, String pUrl)
    {
        super(name: pName, type: pType, extension: pExtension, classifier: pClassifier, url: pUrl);
    }
}
