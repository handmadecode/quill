/*
 * Copyright 2018 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;


/**
 * Base class for unit tests that create files that should be deleted when the test finishes.
 *
 * @author <a href="mailto:peter@myire.org">Peter Franzen</a>
 */
public class FileBasedTest
{
    private Collection<Path> fCreatedFiles = new ArrayList<>();


    /**
     * Delete all files created for the test.
     *
     * @throws IOException  if deleting a file fails.
     */
    @After
    public void deleteTemporaryFiles() throws IOException
    {
        for (Path aPath : fCreatedFiles)
            Files.deleteIfExists(aPath);
    }


    /**
     * Create a temporary file and write a sequence of text lines to it. If successfully created,
     * the file will be put into the list of files to be automatically deleted when the test
     * finishes.
     *
     * @param pPrefix   The temporary file's prefix, possibly null.
     * @param pSuffix   The temporary file's suffix, possibly null.
     * @param pLines    The text lines to write to the created file.
     *
     * @return  A {@code Path} referring to the created files.
     *
     * @throws IOException  if creating the file or writing to it fails.
     * @throws NullPointerException if {@code pLines} is null.
     */
    protected Path createTemporaryFile(String pPrefix, String pSuffix, Collection<String> pLines)
        throws IOException
    {
        Path aPath = Files.createTempFile(pPrefix, pSuffix);
        fCreatedFiles.add(aPath);
        Files.write(aPath, pLines);
        return aPath;
    }


    /**
     * Delete the file system object denoted by a {@code Path}, including any file system objects
     * contained within it.
     *
     * @param pPath The path to the file system object to delete.
     */
    static void deepDelete(Path pPath)
    {
        try
        {
            if (Files.isDirectory(pPath))
            {
                Files.newDirectoryStream(pPath).forEach(FileBasedTest::deepDelete);
                Files.delete(pPath);
            }
            else if (Files.isRegularFile(pPath))
                Files.delete(pPath);
        }
        catch (IOException ioe)
        {
            throw new UncheckedIOException(ioe);
        }
    }
}
