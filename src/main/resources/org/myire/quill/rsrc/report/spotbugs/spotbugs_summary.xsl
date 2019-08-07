<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming the summary of a SpotBugs XML report into
 * part of an HTML page.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- Key use to get unique class names -->
  <xsl:key name="classname" match="Class" use="@classname"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>

  <!-- Parameter containing the path to the detailed report -->
  <xsl:param name="detailed-report-path"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="BugCollection"/>
  </xsl:template>


  <!-- Template for the BugCollection element, which is the top-level element in
       the SpotBugs report -->
  <xsl:template match="BugCollection">

    <!-- Count the total number of bugs -->
    <xsl:variable name="num-bugs" select="count(BugInstance)"/>

    <!-- The SpotBugs summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">SpotBugs</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          SpotBugs <xsl:value-of select="@version"/> report created
          <xsl:value-of select="$xml-modified-date"/>&#160;<xsl:value-of select="$xml-modified-time"/>
        </span>
      </div>

      <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:call-template name="output-statistics-table">
          <xsl:with-param name="num-analyzed-packages" select="FindBugsSummary/@num_packages"/>
          <xsl:with-param name="num-analyzed-classes" select="FindBugsSummary/@total_classes"/>
          <xsl:with-param name="num-problems" select="$num-bugs"/>
        </xsl:call-template>
      </div>

      <!-- Output a link to the detailed html report if it is defined and there are errors or
           warnings-->
      <xsl:if test="$num-bugs &gt; 0 and string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">SpotBugs details</a>
        </div>
	  </xsl:if>

    </div>

  </xsl:template>


  <!-- Output a table with a summary of the SpotBugs run -->
  <xsl:template name="output-statistics-table">
    <xsl:param name="num-analyzed-packages"/>
    <xsl:param name="num-analyzed-classes"/>
    <xsl:param name="num-problems"/>
      <table>
      <!-- Choose the table background color depending on whether there are any
           errors or warnings -->
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$num-problems &gt; 0">errorbg</xsl:when>
            <xsl:otherwise>successbg</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <!-- Output the table rows -->
        <tr>
          <td class="summaryvalue"><xsl:value-of select="$num-analyzed-packages"/></td>
          <td class="summaryvalue"><xsl:value-of select="$num-analyzed-classes"/></td>
          <td class="summaryvalue">
            <xsl:choose>
              <xsl:when test="$num-problems &gt; 0">
                <span class="error"><xsl:value-of select="$num-problems"/></span>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$num-problems"/>
              </xsl:otherwise>
            </xsl:choose>
          </td>
         </tr>
         <tr>
          <td class="summarylabel">packages</td>
          <td class="summarylabel">classes</td>
          <td class="summarylabel">problems</td>
        </tr>
      </table>
  </xsl:template>

</xsl:stylesheet>
