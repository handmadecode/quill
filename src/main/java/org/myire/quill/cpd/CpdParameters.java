/*
 * Copyright 2019 Peter Franzen. All rights reserved.
 *
 * Licensed under the Apache License v2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
package org.myire.quill.cpd;

/**
 * The parameters that can be passed to A {@code CpdRunner} to control the CPD analysis.
 */
public class CpdParameters
{
    private String fLanguage;
    private String fEncoding;

    private int fMinimumTokenCount = 100;

    private boolean fIgnoreLiterals;
    private boolean fIgnoreIdentifiers;
    private boolean fIgnoreAnnotations;
    private boolean fIgnoreUsings;

    private boolean fSkipDuplicateFiles;
    private boolean fSkipLexicalErrors;

    private boolean fSkipBlocks;
    private String fSkipBlocksPattern;


    /**
     * Get the language of the source files to analyze, e.g. &quot;cpp&quot;, &quot;java&quot;,
     * &quot;php&quot;, &quot;ruby&quot;, or &quot;ecmascript&quot;. See
     * <a href="http://pmd.sourceforge.net">the CPD documentation</a> for the list of languages
     * supported by the different versions of CPD. The default is &quot;java&quot;.
     */
    public String getLanguage()
    {
        return fLanguage;
    }


    public void setLanguage(String pLanguage)
    {
        fLanguage = pLanguage;
    }


    /**
     * Get the encoding used by CPD to read the source files and to produce the report. The
     * platform's default encoding will be used if this parameter isn't specified.
     */
    public String getEncoding()
    {
        return fEncoding;
    }


    public void setEncoding(String pEncoding)
    {
        fEncoding = pEncoding;
    }


    /**
     * The minimum duplicate size to be reported. The default is 100.
     */
    public int getMinimumTokenCount()
    {
        return fMinimumTokenCount;
    }


    public void setMinimumTokenCount(int pMinimumTokenCount)
    {
        fMinimumTokenCount = pMinimumTokenCount;
    }


    /**
     * If true, CPD ignores literal value differences when evaluating a duplicate block. This means
     * that {@code foo=42;} and {@code foo=43;} will be seen as equivalent. Default is false.
     */
    public boolean isIgnoreLiterals()
    {
        return fIgnoreLiterals;
    }


    public void setIgnoreLiterals(boolean pIgnoreLiterals)
    {
        fIgnoreLiterals = pIgnoreLiterals;
    }


    /**
     * If true, differences in identifiers (like variable names or methods names) will be ignored in
     * the same way as literals in {@link #isIgnoreLiterals()}. Default is false.
     */
    public boolean isIgnoreIdentifiers()
    {
        return fIgnoreIdentifiers;
    }


    public void setIgnoreIdentifiers(boolean pIgnoreIdentifiers)
    {
        fIgnoreIdentifiers = pIgnoreIdentifiers;
    }


    /**
     * If true, annotations will be ignored. This property can be useful when analyzing code based
     * on certain frameworks where annotations become very repetitive. Default is false.
     */
    public boolean isIgnoreAnnotations()
    {
        return fIgnoreAnnotations;
    }


    public void setIgnoreAnnotations(boolean pIgnoreAnnotations)
    {
        fIgnoreAnnotations = pIgnoreAnnotations;
    }


    /**
     * If true, {@code using} directives in C# will be ignored when comparing text. Default is
     * false.
     */
    public boolean isIgnoreUsings()
    {
        return fIgnoreUsings;
    }


    public void setIgnoreUsings(boolean pIgnoreUsings)
    {
        fIgnoreUsings = pIgnoreUsings;
    }


    /**
     * If true, CPD will ignore multiple copies of files with the same name and length. Default is
     * false.
     */
    public boolean isSkipDuplicateFiles()
    {
        return fSkipDuplicateFiles;
    }


    public void setSkipDuplicateFiles(boolean pSkipDuplicateFiles)
    {
        fSkipDuplicateFiles = pSkipDuplicateFiles;
    }


    /**
     * If true, CPD will skip files which can't be tokenized due to invalid characters instead of
     * aborting the analysis. Default is false.
     */
    public boolean isSkipLexicalErrors()
    {
        return fSkipLexicalErrors;
    }


    public void setSkipLexicalErrors(boolean pSkipLexicalErrors)
    {
        fSkipLexicalErrors = pSkipLexicalErrors;
    }


    /**
     * If true, skipping of blocks is enabled with the patterns specified in the
     * {@code skipBlocksPattern} property. Default is false.
     */
    public boolean isSkipBlocks()
    {
        return fSkipBlocks;
    }


    public void setSkipBlocks(boolean pSkipBlocks)
    {
        fSkipBlocks = pSkipBlocks;
    }


    /**
     * Specifies the pattern to find the blocks to skip when {@code skipBlocks} is true. The string
     * value contains two parts, separated by a vertical line ('|'). The first part is the start
     * pattern, the second part is the end pattern. The default value is &quot;#if 0|#endif&quot;.
     */
    public String getSkipBlocksPattern()
    {
        return fSkipBlocksPattern;
    }


    public void setSkipBlocksPattern(String pSkipBlocksPattern)
    {
        fSkipBlocksPattern = pSkipBlocksPattern;
    }
}
