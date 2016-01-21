<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming the summary of a JUnitReport XML file into
 * part of an HTML page.
 *                                                        
 * 2007-03-08 /PF    Created.
 * 2014-04-07 /PF    Added XML timestamp parameters + color coded error/warning
 *					 count.
 * 2015-02-12 /PF    Reworked to suite the output from JUnitSummaryReport.
 * 2015-11-25 /PF    Updated to new dashboard look.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- Parameter containing the path to the detailed report -->
  <xsl:param name="detailed-report-path"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="junit-summary"/>
  </xsl:template>


  <!-- Template for the junit-summary element, which is the top-level element in
       a JUnitSummaryReport -->
  <xsl:template match="junit-summary">

    <!-- The JUnit summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Tests</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          JUnit report created
          <xsl:value-of select="@start-date"/>&#160;<xsl:value-of select="@start-time"/>
        </span>
      </div>

      <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:call-template name="output-statistics-table"/>
      </div>

      <!-- Output a link to the detailed html report if it is defined -->
      <xsl:if test="string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">Test details</a>
        </div>
      </xsl:if>

    </div>

  </xsl:template>


  <!-- Output a table with a summary of the test run -->
  <xsl:template name="output-statistics-table">
      <table>
      <!-- Choose the table background color depending on whether there are any
           errors or warnings -->
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="@errors &gt; 0">errorbg</xsl:when>
            <xsl:when test="@failures &gt; 0">errorbg</xsl:when>
            <xsl:when test="@skipped &gt; 0">warningbg</xsl:when>
            <xsl:otherwise>successbg</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <!-- Output the table rows -->
        <tr>
          <td class="summaryvalue"><xsl:value-of select="@testsuites"/></td>
          <td class="summaryvalue"><xsl:value-of select="@tests"/></td>
          <td class="summaryvalue">
            <xsl:call-template name="output-warning-count">
              <xsl:with-param name="warning-count" select="@skipped"/>
            </xsl:call-template>
          </td>
          <td class="summaryvalue">
            <xsl:call-template name="output-error-count">
              <xsl:with-param name="error-count" select="@errors"/>
            </xsl:call-template>
          </td>
          <td class="summaryvalue">
            <xsl:call-template name="output-error-count">
              <xsl:with-param name="error-count" select="@failures"/>
            </xsl:call-template>
          </td>
         </tr>
         <tr>
          <td class="summarylabel">test suites</td>
          <td class="summarylabel">tests</td>
          <td class="summarylabel">skipped</td>
          <td class="summarylabel">errors</td>
          <td class="summarylabel">failures</td>
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
