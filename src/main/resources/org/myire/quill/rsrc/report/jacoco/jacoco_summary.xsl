<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a Jacoco XML report into part of an HTML page.
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


  <!-- Template for the report element, which is the top-level element in the Jacoco report -->
  <xsl:template match="report">

  <!-- Count the total number of packages and files -->
  <xsl:variable name="num-packages" select="count(package)"/>
  <xsl:variable name="num-types" select="count(package/class)"/>

    <!-- The Jacoco summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Coverage</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          JaCoCo report created
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


  <!-- Output a table with aggregated coverage statistics- -->
  <xsl:template name="output-summary-table">
    <xsl:param name="num-packages"/>
    <xsl:param name="num-types"/>
      <table class="neutralbg">
        <tr>
          <td class="summaryvalue"><xsl:value-of select="$num-packages"/></td>
          <td class="summaryvalue"><xsl:value-of select="$num-types"/></td>
          <xsl:apply-templates select="counter[@type='CLASS']"/>
          <xsl:apply-templates select="counter[@type='METHOD']"/>
          <xsl:apply-templates select="counter[@type='INSTRUCTION']"/>
          <xsl:apply-templates select="counter[@type='BRANCH']"/>
        </tr>
        <tr>
          <td class="summarylabel">packages</td>
          <td class="summarylabel">types</td>
          <td class="summarylabel">class<br/>coverage</td>
          <td class="summarylabel">method<br/>coverage</td>
          <td class="summarylabel">instruction<br/>coverage</td>
          <td class="summarylabel">branch<br/>coverage</td>
        </tr>
      </table>
  </xsl:template>

  <xsl:template match="counter">
    <xsl:variable name="total" select="number(@covered) + number(@missed)"/>
    <xsl:variable name="percentage" select="round(1000 * number(@covered) div $total) div 10"/>
    <td class="summaryvalue"><xsl:value-of select="$percentage"/>%</td>
  </xsl:template>

</xsl:stylesheet>
