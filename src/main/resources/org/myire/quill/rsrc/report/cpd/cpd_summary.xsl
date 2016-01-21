<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a CPD XML report into part of an HTML page.
 *
 * 2010-04-10 /PF    Created from the full version.
 * 2015-11-25 /PF    Updated to new dashboard look.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- This part of the path to the source files should not be used when
       converting the file path to qualified class names -->
  <xsl:param name="source-file-base" select="'src/java/'"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>

  <!-- Parameter containing the path to the detailed report -->
  <xsl:param name="detailed-report-path"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="pmd-cpd"/>
  </xsl:template>


  <!-- Template for the pmd-cpd element, which is the top-level element in the
       CPD report -->
  <xsl:template match="pmd-cpd">

    <!-- Count the total number of duplications -->
    <xsl:variable name="num-duplications" select="count(duplication)"/>

    <!-- The CPD summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Duplications</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          CPD report created
          <xsl:value-of select="$xml-modified-date"/>&#160;<xsl:value-of select="$xml-modified-time"/>
        </span>
      </div>

        <!-- Summary table -->
      <div class="summarysectionitem">
       <xsl:call-template name="output-summary-table">
          <xsl:with-param name="num-duplications" select="$num-duplications"/>
          <xsl:with-param name="num-lines" select="sum(duplication/@lines)"/>
        </xsl:call-template>
      </div>

      <!-- Output a link to the detailed html report if it is defined and there are errors or
           warnings -->
      <xsl:if test="$num-duplications &gt; 0 and string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">CPD details</a>
        </div>
	  </xsl:if>

    </div>
  </xsl:template>


  <!-- Output a table with a summary of the CPD analysis -->
  <xsl:template name="output-summary-table">
    <xsl:param name="num-duplications"/>
    <xsl:param name="num-lines"/>
      <table>
      <!-- Choose the table background color depending on whether there are any
           errors or warnings -->
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$num-duplications &gt; 0">warningbg</xsl:when>
            <xsl:otherwise>successbg</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <!-- Output the table rows -->
        <tr>
          <td class="summaryvalue"><xsl:value-of select="$num-duplications"/></td>
          <td class="summaryvalue"><xsl:value-of select="$num-lines"/></td>
        </tr>
        <tr>
          <td class="summarylabel">duplications</td>
          <td class="summarylabel">duplicated lines</td>
        </tr>
      </table>
  </xsl:template>

</xsl:stylesheet>
