/*
 * Copyright 2017 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven

import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.settings.Mirror


/**
 * Matcher for Maven mirror patterns. See the
 * <a href="https://maven.apache.org/guides/mini/guide-mirror-settings.html">documentation</a> for
 * a description of these mirror patterns. In short, they are:
 *<ul>
 * <li> * = everything</li>
 * <li>external:* = everything not on the localhost and not file based</li>
 * <li>repo1,repo2 = repo1 or repo2</li>
 * <li>*,!repo1 = everything except repo1</li>
 *</ul>
 */
class MirrorMatcher
{
    static private final String WILDCARD = "*";
    static private final String EXTERNAL_WILDCARD = "external:*";


    private final Set<String> fPatterns = []
    private final boolean fHasWildcard
    private final boolean fHasExternalWildcard


    /**
     * Create a new {@code MirrorMatcher}.
     *
     * @param pMirrors  The mirrors to use the patterns of when matching.
     */
    MirrorMatcher(Collection<Mirror> pMirrors)
    {
        boolean aHasWildcard = false, aHasExternalWildcard = false;

        for (aMirror in pMirrors)
        {
            StringTokenizer aTokenizer = new StringTokenizer(aMirror.mirrorOf, ",");
            while (aTokenizer.hasMoreTokens())
            {
                String aPattern = aTokenizer.nextToken().trim();
                if (aPattern == WILDCARD)
                    aHasWildcard = true;
                else if (aPattern == EXTERNAL_WILDCARD)
                    aHasExternalWildcard = true;
                else if (!aPattern.empty)
                    fPatterns.add(aPattern);
            }
        }

        fHasWildcard = aHasWildcard;
        fHasExternalWildcard = aHasExternalWildcard;
    }


    /**
     * Check if an {@code ArtifactRepository} matches one of the mirror patterns specified in the
     * constructor.
     *
     * @param pRepository   The repository to check.
     *
     * @return  True if the repository matches one of the mirror patterns, false if not.
     */
    boolean matches(ArtifactRepository pRepository)
    {
        // Negative match has higher priority than wildcard.
        if (fPatterns.contains('!' + pRepository.id))
            // The repository ID matches a negative pattern, it is explicitly excluded and thereby
            // doesn't match.
            return false;

        if (fHasWildcard)
            return true;

        if (fHasExternalWildcard && isExternalRepository(pRepository))
            return true;

        return fPatterns.contains(pRepository.id);
    }


    /**
     * Check if a repository is external. An external repository is any repository that doesn't
     * reside on localhost or is file-based.
     *
     * @param pArtifactRepository   The repository to check.
     *
     * @return  True if the repository is external, false if not.
     */
    static private boolean isExternalRepository(ArtifactRepository pArtifactRepository)
    {
        return !(pArtifactRepository.protocol == 'file' ||
                 pArtifactRepository.url.contains('localhost') ||
                 pArtifactRepository.url.contains('127.0.0.1'));
    }
}
