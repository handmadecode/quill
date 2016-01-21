<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming the summary of a Checkstyle XML report into
 * part of an HTML page.
 *
 * 2014-03-14 /PF    Created from the full report XSL.
 * 2015-11-25 /PF    Updated to new dashboard look.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- Key use to get unique file names -->
  <xsl:key name="filename" match="file" use="@name"/>

  <!-- Key use to get unique violating file names -->
  <xsl:key name="violatingfilename" match="error" use="../@name"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>

  <!-- Parameter containing the path to the detailed report -->
  <xsl:param name="detailed-report-path"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="checkstyle"/>
  </xsl:template>


  <!-- Template for the checkstyle element, which is the top-level element in
       the Checkstyle report -->
  <xsl:template match="checkstyle">

  <!-- Count the total number of errors and warnings -->
  <xsl:variable name="num-errors" select="count(file/error[@severity='error'])"/>
  <xsl:variable name="num-warnings" select="count(file/error[@severity='warning'])"/>

    <!-- The Checkstyle summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Checkstyle</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          Checkstyle <xsl:value-of select="@version"/> report created
          <xsl:value-of select="$xml-modified-date"/>&#160;<xsl:value-of select="$xml-modified-time"/>
        </span>
      </div>

        <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:call-template name="output-summary-table">
          <!-- Checkstyle may loop through the fileset multiple times depending on
               which checks are run, and a file may thus occur more than once in
               the output -->
          <xsl:with-param name="num-analyzed-files"
                          select="count(file[generate-id() = generate-id(key('filename', @name)[1])])"/>
          <xsl:with-param name="num-files-with-violations"
                          select="count(file/error[generate-id() = generate-id(key('violatingfilename', ../@name)[1])])"/>
          <xsl:with-param name="num-errors" select="$num-errors"/>
          <xsl:with-param name="num-warnings" select="$num-warnings"/>
        </xsl:call-template>
      </div>

      <!-- Output a link to the detailed html report if it is defined and there are errors or
           warnings-->
      <xsl:if test="$num-errors + $num-warnings &gt; 0 and string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">Checkstyle details</a>
        </div>
	  </xsl:if>

    </div>

  </xsl:template>


  <!-- Output a table with a summary of the checkstyle run -->
  <xsl:template name="output-summary-table">
    <xsl:param name="num-analyzed-files"/>
    <xsl:param name="num-files-with-violations"/>
    <xsl:param name="num-errors"/>
    <xsl:param name="num-warnings"/>
      <table>
      <!-- Choose the table background color depending on whether there are any
           errors or warnings -->
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$num-errors &gt; 0">errorbg</xsl:when>
            <xsl:when test="$num-warnings &gt; 0">warningbg</xsl:when>
            <xsl:otherwise>successbg</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <!-- Output the table rows -->
        <tr>
          <td class="summaryvalue"><xsl:value-of select="$num-analyzed-files"/></td>
          <td class="summaryvalue">
            <xsl:call-template name="output-error-count">
              <xsl:with-param name="error-count" select="$num-files-with-violations"/>
            </xsl:call-template>
          </td>
          <td class="summaryvalue">
            <xsl:call-template name="output-error-count">
              <xsl:with-param name="error-count" select="$num-errors"/>
            </xsl:call-template>
          </td>
          <td class="summaryvalue">
            <xsl:call-template name="output-warning-count">
              <xsl:with-param name="warning-count" select="$num-warnings"/>
            </xsl:call-template>
          </td>
        </tr>
        <tr>
          <td class="summarylabel">files</td>
          <td class="summarylabel">files with<br/>violations</td>
          <td class="summarylabel">errors</td>
          <td class="summarylabel">warnings</td>
        </tr>
      </table>
  </xsl:template>


  <!-- Output a numeric value with the error class if it is greater than 0 -->
  <xsl:template name="output-error-count">
    <xsl:param name="error-count"/>
    <xsl:choose>
      <xsl:when test="$error-count &gt; 0">
        <span class="error"><xsl:value-of select="$error-count"/></span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$error-count"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Output a numeric value with the warning class if it is greater than 0 -->
  <xsl:template name="output-warning-count">
    <xsl:param name="warning-count"/>
    <xsl:choose>
      <xsl:when test="$warning-count &gt; 0">
        <span class="warning"><xsl:value-of select="$warning-count"/></span>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$warning-count"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
