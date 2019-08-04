<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a Scent XML report into part of an HTML page.
 *
 * 2016-10-25 /PF    Created.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- Parameter containing the path to the detailed report -->
  <xsl:param name="detailed-report-path"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="scent-report"/>
  </xsl:template>


  <!-- Template for the scent-report element, which is the top-level element in
       the Scent report -->
  <xsl:template match="scent-report">

    <!-- The Scent summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Metrics</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          Scent <xsl:value-of select="@version"/> report created
          <xsl:value-of select="@date"/>&#160;<xsl:value-of select="@time"/>
        </span>
      </div>

        <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:apply-templates select="summary"/>
      </div>

      <!-- Output a link to the detailed html report if it is defined -->
      <xsl:if test="string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">Scent details</a>
        </div>
      </xsl:if>

    </div>

  </xsl:template>


  <!-- Output a table with statistics for all analyzed classes -->
  <xsl:template match="summary">
      <table class="neutralbg">
        <tr>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@packages"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@compilation-units"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@types"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@methods"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@fields"/>
          </xsl:call-template>
        </tr>
        <tr>
          <td class="summarylabel">packages</td>
          <td class="summarylabel">files</td>
          <td class="summarylabel">types</td>
          <td class="summarylabel">methods</td>
          <td class="summarylabel">fields</td>
        </tr>
        <tr>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@statements"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@javadocs"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@javadoc-lines"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@line-comments"/>
          </xsl:call-template>
          <xsl:call-template name="output-value-cell">
            <xsl:with-param name="value" select="@block-comments-lines"/>
          </xsl:call-template>
        </tr>
        <tr>
          <td class="summarylabel">statements</td>
          <td class="summarylabel">JavaDocs</td>
          <td class="summarylabel">JavaDoc<br/>lines</td>
          <td class="summarylabel">line<br/>comments</td>
          <td class="summarylabel">block comment<br/>lines</td>
        </tr>
      </table>
  </xsl:template>

  <!-- Output a table cell with a value if it exists, otherwise a marker value for non-presence -->
  <xsl:template name="output-value-cell">
    <xsl:param name="value"/>
    <td class="summaryvalue">
      <xsl:choose>
        <xsl:when test="$value">
          <xsl:value-of select="$value" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>0</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
    </td>
  </xsl:template>

</xsl:stylesheet>
