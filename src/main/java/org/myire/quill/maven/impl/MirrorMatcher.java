/*
 * Copyright 2017-2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.maven.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.settings.Mirror;


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


    private final Set<String> fPatterns = new HashSet<>();
    private final boolean fHasWildcard;
    private final boolean fHasExternalWildcard;


    /**
     * Create a new {@code MirrorMatcher}.
     *
     * @param pMirrors  The mirrors to use the patterns of when matching.
     */
    MirrorMatcher(Collection<Mirror> pMirrors)
    {
        boolean aHasWildcard = false, aHasExternalWildcard = false;

        for (Mirror aMirror : pMirrors)
        {
            StringTokenizer aTokenizer = new StringTokenizer(aMirror.getMirrorOf(), ",");
            while (aTokenizer.hasMoreTokens())
            {
                String aPattern = aTokenizer.nextToken().trim();
                if (WILDCARD.equals(aPattern))
                    aHasWildcard = true;
                else if (EXTERNAL_WILDCARD.equals(aPattern))
                    aHasExternalWildcard = true;
                else if (!aPattern.isEmpty())
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
        if (fPatterns.contains('!' + pRepository.getId()))
            // The repository ID matches a negative pattern, it is explicitly excluded and thereby
            // doesn't match.
            return false;

        if (fHasWildcard)
            return true;

        if (fHasExternalWildcard && isExternalRepository(pRepository))
            return true;

        return fPatterns.contains(pRepository.getId());
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
        return !("file".equals(pArtifactRepository.getProtocol()) ||
                 pArtifactRepository.getUrl().contains("localhost") ||
                 pArtifactRepository.getUrl().contains("127.0.0.1"));
    }
}
