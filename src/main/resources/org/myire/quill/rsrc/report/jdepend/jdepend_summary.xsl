<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming the summary of a JDepend XML report into
 * part of an HTML page.
 *
 * 2015-03-07 /PF    Created from the full report XSL.
 * 2015-11-25 /PF    Updated to new dashboard look.
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
    <xsl:apply-templates select="JDepend"/>
  </xsl:template>


  <!-- Template for the JDepend element, which is the top-level element in the JDepend report -->
  <xsl:template match="JDepend">

    <!-- The JDepend summary section -->
    <div class="summarysection">

      <!-- Report header -->
      <div class="summaryheader">Dependencies</div>

      <!-- Intro text -->
      <div class="summarysectionitem">
        <span class="summaryintro">
          JDepend report created
          <xsl:value-of select="$xml-modified-date"/>&#160;<xsl:value-of select="$xml-modified-time"/>
        </span>
      </div>

        <!-- Summary table -->
      <div class="summarysectionitem">
        <xsl:call-template name="output-summary-table" />
      </div>

      <!-- Output a link to the detailed html report -->
      <xsl:if test="string-length($detailed-report-path) &gt; 0">
        <div class="summarysectionitem">
          <a class="data" href="{$detailed-report-path}" target="_blank">JDepend details</a>
        </div>
      </xsl:if>

    </div>

  </xsl:template>


  <!-- Output a table with a summary of the JDepend run -->
  <xsl:template name="output-summary-table">
      <xsl:variable name="num-cycles" select="count(Cycles/Package)"/>
      <table>
        <!-- Choose the table background color depending on whether there are any cycles or not -->
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$num-cycles&gt; 0">warningbg</xsl:when>
            <xsl:otherwise>successbg</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <tr>
          <td class="summaryvalue"><xsl:value-of select="count(Packages/Package/Stats)"/></td>
          <td class="summaryvalue"><xsl:call-template name="max-classes"/></td>
          <td class="summaryvalue"><xsl:call-template name="max-afferent"/></td>
          <td class="summaryvalue"><xsl:call-template name="max-efferent"/></td>
          <td class="summaryvalue"><xsl:call-template name="max-distance"/></td>
          <td class="summaryvalue">
            <xsl:choose>
                <xsl:when test="$num-cycles &gt; 0">
                    <span class="warning"><xsl:value-of select="$num-cycles"/></span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$num-cycles"/>
                </xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
        <tr>
          <td class="summarylabel">packages</td>
          <td class="summarylabel">max classes</td>
          <td class="summarylabel">max afferent couplings</td>
          <td class="summarylabel">max efferent couplings</td>
          <td class="summarylabel">max distance</td>
          <td class="summarylabel">cycles</td>
        </tr>
      </table>
  </xsl:template>


    <xsl:template name="max-classes">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="TotalClasses"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="TotalClasses"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="max-afferent">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="Ca"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="Ca"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="max-efferent">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="Ce"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="Ce"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="max-distance">
        <xsl:for-each select="Packages/Package/Stats">
            <xsl:sort data-type="number" order="descending" select="D"/>
            <xsl:if test="position()=1">
                <xsl:value-of select="D"/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
