<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming an Cobertura XML report into part of an HTML
 * page.
 *
 * 2009-01-05 /PF    Created from emma.xsl.
 * 2014-04-07 /PF    Created from the full report XSL.
 * 2015-11-25 /PF    Updated to new dashboard look.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>

  <!-- Parameter containing the path to the detailed report -->
  <xsl:param name="detailed-report-path"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="coverage"/>
  </xsl:template>


  <!-- Template for the coverage element, which is the top-level element in
       the Cobertura report -->
  <xsl:template match="coverage">

  <!-- Count the total number of packages and files -->
  <xsl:variable name="num-packages" select="count(packages/package)"/>
  <xsl:variable name="num-types" select="count(packages/package/classes/class)"/>

    <!-- The Cobertura summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Coverage</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          Cobertura <xsl:value-of select="@version"/> report created
          <xsl:value-of select="$xml-modified-date"/>&#160;<xsl:value-of select="$xml-modified-time"/>
        </span>
      </div>

      <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:call-template name="output-summary-table">
          <xsl:with-param name="num-packages" select="$num-packages"/>
          <xsl:with-param name="num-types" select="$num-types"/>
        </xsl:call-template>
      </div>

      <!-- Output a link to the detailed html report if it is defined -->
      <xsl:if test="string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">Coverage details</a>
        </div>
      </xsl:if>

    </div>

  </xsl:template>


  <!-- Output a table with statistics for all analyzed classes -->
  <xsl:template name="output-summary-table">
    <xsl:param name="num-packages"/>
    <xsl:param name="num-types"/>
      <table class="neutralbg">
        <tr>
          <td class="summaryvalue"><xsl:value-of select="$num-packages"/></td>
          <td class="summaryvalue"><xsl:value-of select="$num-types"/></td>
          <td class="summaryvalue"><xsl:value-of select="round(1000 * number(@line-rate)) div 10"/>%</td>
          <td class="summaryvalue"><xsl:value-of select="round(1000 * number(@branch-rate)) div 10"/>%</td>
          <td class="summaryvalue"><xsl:value-of select="format-number(number(@complexity), '#.000')"/></td>
        </tr>
        <tr>
          <td class="summarylabel">packages</td>
          <td class="summarylabel">types</td>
          <td class="summarylabel">line<br/>coverage</td>
          <td class="summarylabel">branch<br/>coverage</td>
          <td class="summarylabel">average<br/>complexity</td>
        </tr>
      </table>
  </xsl:template>

</xsl:stylesheet>
