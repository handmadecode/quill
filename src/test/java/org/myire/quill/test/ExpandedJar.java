/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.FileCollectionAdapter;
import org.gradle.api.internal.file.collections.MinimalFileSet;


/**
 * A {@code MinimalFileSet} holding the extracted jar files from a jar file.
 */
public class ExpandedJar implements MinimalFileSet
{
    private final Path fDirectory;
    private final String fJarFileName;
    private final Set<File> fFiles = new HashSet<>();


    /**
     * Create a new {@code ExpandedJar}. The jar file is assumed to be available as a resource on
     * the class path.
     *
     * @param pResourcePath The resource path where the jar file resides.
     * @param pJarFileName  The name of the jar file to expand.
     *
     * @throws IOException  if expanding the jar file fails.
     */
    public ExpandedJar(String pResourcePath, String pJarFileName) throws IOException
    {
        fDirectory = Files.createTempDirectory(pJarFileName);
        fJarFileName = pJarFileName;
        expand(pResourcePath + pJarFileName);
    }


    @Override
    public Set<File> getFiles()
    {
        return fFiles;
    }


    @Override
    public String getDisplayName()
    {
        return fJarFileName;
    }


    /**
     * Create a {@code FileCollection} with the expanded jar files.
     *
     * @return  A new {@code FileCollection}.
     */
    public FileCollection toFileCollection()
    {
        return new FileCollectionAdapter(this);
    }


    /**
     * Delete the extracted files.
     */
    public void deleteExtractedFiles()
    {
        FileBasedTest.deepDelete(fDirectory);
        fFiles.clear();
    }


    private void expand(String pResource) throws IOException
    {
        try (JarFile aJarFile = new JarFile(ExternalToolTest.class.getResource(pResource).getFile()))
        {
            Enumeration<JarEntry> aEntries = aJarFile.entries();
            while (aEntries.hasMoreElements())
            {
                JarEntry aEntry = aEntries.nextElement();
                if (aEntry.getName().endsWith(".jar"))
                {
                    try (InputStream aInputStream = aJarFile.getInputStream(aEntry))
                    {
                        Path aPath = toAbsolutePath(aEntry.getName());
                        Files.copy(aInputStream, aPath);
                        fFiles.add(aPath.toFile());
                    }
                }
            }
        }
    }


    private Path toAbsolutePath(String pFileName)
    {
        return fDirectory.resolve(pFileName);
    }
}
