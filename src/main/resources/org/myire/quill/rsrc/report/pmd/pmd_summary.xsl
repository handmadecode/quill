<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming the summary of a PMD XML report into part of
 * an HTML page.
 *
 *******************************************************************************
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pmd="http://pmd.sourceforge.net/report/2.0.0">
  <xsl:output method="html"/>

  <!-- Key use to get unique rule names -->
  <xsl:key name="rule" match="pmd:violation" use="@rule"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>

  <!-- Parameter containing the path to the detailed report -->
  <xsl:param name="detailed-report-path"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="pmd:pmd"/>
  </xsl:template>


  <!-- Template for the pmd element, which is the top-level element in the PMD
       report -->
  <xsl:template match="pmd:pmd">

    <!-- Count the number of files with violations -->
    <xsl:variable name="num-files-with-violations" select="count(pmd:file)"/>

    <!-- The PMD summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">PMD</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          PMD <xsl:value-of select="@version"/> report created
          <xsl:value-of select="$xml-modified-date"/>&#160;<xsl:value-of select="$xml-modified-time"/>
        </span>
      </div>

      <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:call-template name="output-summary-table">
          <xsl:with-param name="num-files-with-violations" select="$num-files-with-violations"/>
          <xsl:with-param name="num-violations" select="count(pmd:file/pmd:violation)"/>
        </xsl:call-template>
      </div>

      <!-- Output a link to the detailed html report if is defined and there are errors or
           warnings-->
      <xsl:if test="$num-files-with-violations &gt; 0 and string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">PMD details</a>
        </div>
	  </xsl:if>

    </div>
  </xsl:template>


  <!-- Output a table with a summary of the PMD run -->
  <xsl:template name="output-summary-table">
    <xsl:param name="num-files-with-violations"/>
    <xsl:param name="num-violations"/>
      <table>
      <!-- Choose the table background color depending on whether there are any
           errors or warnings -->
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$num-violations &gt; 0">errorbg</xsl:when>
            <xsl:otherwise>successbg</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <!-- Output the table rows -->
        <tr>
          <td class="summaryvalue">
            <xsl:call-template name="output-error-count">
              <xsl:with-param name="error-count" select="$num-files-with-violations"/>
            </xsl:call-template>
          </td>
          <td class="summaryvalue">
            <xsl:call-template name="output-error-count">
              <xsl:with-param name="error-count" select="$num-violations"/>
            </xsl:call-template>
          </td>
         </tr>
         <tr>
          <td class="summarylabel">files with violations</td>
          <td class="summarylabel">violations</td>
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

</xsl:stylesheet>
