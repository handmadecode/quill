/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.myire.quill.cpd.CpdParameters;
import org.myire.quill.cpd.CpdRunner;
import org.myire.quill.test.ExpandedJar;
import org.myire.quill.test.ExternalToolTest;


/**
 * Unit tests for {@code CpdRunnerImpl}.
 */
public class CpdRunnerImplTest extends ExternalToolTest<CpdRunner>
{
    static private ExpandedJar cPmdJar;


    public CpdRunnerImplTest()
    {
        super(
            CpdRunner.class,
            "org.myire.quill.cpd.impl",
            "CpdRunnerImpl",
            cPmdJar.toFileCollection());
    }


    /**
     * Expand the jar containing the PMD jar files into a temporary directory.
     *
     * @throws IOException  if expanding the jar file fails.
     */
    @BeforeClass
    static public void expandPmdJar() throws IOException
    {
        cPmdJar = new ExpandedJar("/external-tools/", "pmdJars.jar");
    }


    /**
     * Delete the temporary directory with the PMD jar files.
     */
    @AfterClass
    static public void deleteExpandedJars()
    {
        cPmdJar.deleteExtractedFiles();
    }


    /**
     * Calling {@code runCpd} with a null file collection argument should throw a
     * {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void runCpdThrowsForNullFileCollection() throws IOException
    {
        // Given
        File aReportFile = createReportFileSpec("report.xml");

        // When
        newToolProxy().runCpd(null, aReportFile, "xml", new CpdParameters());
    }


    /**
     * Calling {@code runCpd} with a null report file argument should throw a
     * {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void runCpdThrowsForNullReportFile() throws IOException
    {
        newToolProxy().runCpd(Collections.emptyList(), null, "xml", new CpdParameters());
    }


    /**
     * Calling {@code runCpd} with a null report format argument should throw a
     * {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void runCpdThrowsForNullReportFormat() throws IOException
    {
        // Given
        File aReportFile = createReportFileSpec("report");

        // When
        newToolProxy().runCpd(Collections.emptyList(), aReportFile, null, new CpdParameters());
    }


    /**
     * Calling {@code runCpd} with a null {@code CpdParameters} argument should throw a
     * {@code NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void runCpdThrowsForNullCpdParameters() throws IOException
    {
        // Given
        File aReportFile = createReportFileSpec("report.csv");

        // When
        newToolProxy().runCpd(Collections.emptyList(), aReportFile, "csv", null);
    }


    /**
     * Calling {@code runCpd} with a report file whose parent directory does not exist should throw
     * an {@code IOException}.
     *
     * @throws IOException  always
     */
    @Test(expected = IOException.class)
    public void runCpdThrowsForNonExistingReportFileDirectory() throws IOException
    {
        newToolProxy().runCpd(Collections.emptyList(), new File("/does/not/exist/report"), "text", new CpdParameters());
    }


    /**
     * A duplication should only be reported if the number of duplicated tokens is greater than or
     * equal to the minimum token count.
     *
     * @throws IOException  if the test fails unexpectedly.
     */
    @Test
    public void duplicationIsReportedInAccordanceWithMinimumTokenCount() throws IOException
    {
        // Given
        CpdRunner aCpdRunner = newToolProxy();
        File aReportFile1 = createReportFileSpec("report1.txt");
        File aReportFile2 = createReportFileSpec("report2.txt");
        File aCodeFile = createJavaFile(
          "public class X {",
          "int method1(int p) { return p * 4711; }",
          "int method2(int p) { return p * 4711; }",
          "}"
        );

        // Given
        CpdParameters aParameters = new CpdParameters();
        aParameters.setMinimumTokenCount(11);

        // When
        aCpdRunner.runCpd(Collections.singleton(aCodeFile), aReportFile1, "text", aParameters);

        // Then
        assertTrue(aReportFile1.exists());
        assertEquals(0, aReportFile1.length());

        // Given
        aParameters.setMinimumTokenCount(10);

        // When
        aCpdRunner.runCpd(Collections.singleton(aCodeFile), aReportFile2, "text", aParameters);

        // Then
        assertTrue(aReportFile2.exists());
        assertTrue(aReportFile2.length() > 0);
    }


    /**
     * A duplication with different literals should only be detected if {@code ignoreLiterals} is
     * true.
     *
     * @throws IOException  if the test fails unexpectedly.
     */
    @Test
    public void duplicationOfLiteralsIsReportedOnlyIfIgnoreLiteralsIsTrue() throws IOException
    {
        // Given
        CpdRunner aCpdRunner = newToolProxy();
        File aReportFile1 = createReportFileSpec("report1.txt");
        File aReportFile2 = createReportFileSpec("report2.txt");
        File aCodeFile = createJavaFile(
            "public class X {",
            "double method1(int p) { return p * 4711.0; }",
            "double method2(int p) { return p * 4712.0; }",
            "}"
        );

        // Given
        CpdParameters aParameters = new CpdParameters();
        aParameters.setMinimumTokenCount(10);

        // When
        aCpdRunner.runCpd(Collections.singleton(aCodeFile), aReportFile1, "text", aParameters);

        // Then
        assertTrue(aReportFile1.exists());
        assertEquals(0, aReportFile1.length());

        // Given
        aParameters.setIgnoreLiterals(true);

        // When
        aCpdRunner.runCpd(Collections.singleton(aCodeFile), aReportFile2, "text", aParameters);

        // Then
        assertTrue(aReportFile2.exists());
        assertTrue(aReportFile2.length() > 0);
    }


    /**
     * A duplication with different identifiers should only be detected if {@code ignoreIdentifiers}
     * is true.
     *
     * @throws IOException  if the test fails unexpectedly.
     */
    @Test
    public void duplicationOfIdentifiersIsReportedOnlyIfIgnoreIdentifiersIsTrue() throws IOException
    {
        // Given
        CpdRunner aCpdRunner = newToolProxy();
        File aReportFile1 = createReportFileSpec("report1.text");
        File aReportFile2 = createReportFileSpec("report2.text");
        File aCodeFile = createJavaFile(
            "public class X {",
            "int method1(int p) { return p * 4711; }",
            "int method2(int q) { return q * 4711; }",
            "}"
        );

        // Given
        CpdParameters aParameters = new CpdParameters();
        aParameters.setMinimumTokenCount(10);

        // When
        aCpdRunner.runCpd(Collections.singleton(aCodeFile), aReportFile1, "text", aParameters);

        // Then
        assertTrue(aReportFile1.exists());
        assertEquals(0, aReportFile1.length());

        // Given
        aParameters.setIgnoreIdentifiers(true);

        // When
        aCpdRunner.runCpd(Collections.singleton(aCodeFile), aReportFile2, "text", aParameters);

        // Then
        assertTrue(aReportFile2.exists());
        assertTrue(aReportFile2.length() > 0);
    }


    private File createReportFileSpec(String pFileName)
    {
        File aReportFileSpec = new File(pFileName);
        addTemporaryFile(aReportFileSpec.toPath());
        return aReportFileSpec;
    }


    private File createJavaFile(String... pContents) throws IOException
    {
        // Can't use createTemporaryFile(), since CPD sometimes thinks files in the system temporary
        // directory are symbolic links.
        Path aPath = Files.createFile(Paths.get("Cpd-" + ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) +  ".java"));
        addTemporaryFile(aPath);
        Files.write(aPath, Arrays.asList(pContents));
        return aPath.toFile();
    }
}
