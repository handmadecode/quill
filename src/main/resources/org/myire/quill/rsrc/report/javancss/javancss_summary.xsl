<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a JavaNCSS XML report into part of an HTML
 * page.
 *                                                        
 * 2014-04-08 /PF    Created from the full report XSL.
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
    <xsl:apply-templates select="javancss"/>
  </xsl:template>


  <!-- Template for the javancss element, which is the top-level element in
       the JavaNCSS report -->
  <xsl:template match="javancss">

    <!-- The JavaNCSS summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Metrics</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          JavaNCSS report created
          <xsl:value-of select="$xml-modified-date"/>&#160;<xsl:value-of select="$xml-modified-time"/>
        </span>
      </div>

        <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:call-template name="output-total-table" />
      </div>

      <!-- Output a link to the detailed html report if it is defined -->
      <xsl:if test="string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">JavaNCSS details</a>
        </div>
      </xsl:if>

    </div>

  </xsl:template>


  <!-- Output a table with statistics for all analyzed classes -->
  <xsl:template name="output-total-table">
      <table class="neutralbg">
        <tr>
          <td class="summaryvalue"><xsl:value-of select="count(packages/package)"/></td>
          <td class="summaryvalue"><xsl:value-of select="packages/total/classes"/></td>
          <td class="summaryvalue"><xsl:value-of select="packages/total/functions"/></td>
          <td class="summaryvalue"><xsl:value-of select="packages/total/ncss"/></td>
        </tr>
        <tr>
          <td class="summarylabel">packages</td>
          <td class="summarylabel">classes</td>
          <td class="summarylabel">methods</td>
          <td class="summarylabel">NCSS</td>
        </tr>
        <tr>
          <td class="summaryvalue"><xsl:value-of select="packages/total/javadocs"/></td>
          <td class="summaryvalue"><xsl:value-of select="packages/total/javadoc_lines"/></td>
          <td class="summaryvalue"><xsl:value-of select="packages/total/single_comment_lines"/></td>
          <td class="summaryvalue"><xsl:value-of select="packages/total/multi_comment_lines"/></td>
        </tr>
        <tr>
          <td class="summarylabel">JavaDocs</td>
          <td class="summarylabel">JavaDoc lines</td>
          <td class="summarylabel">single line<br/>comments</td>
          <td class="summarylabel">block comment<br/>lines</td>
        </tr>
      </table>
  </xsl:template>


  <!-- Template to output all matched text verbatim -->
  <xsl:template match="text()"/>

</xsl:stylesheet>
