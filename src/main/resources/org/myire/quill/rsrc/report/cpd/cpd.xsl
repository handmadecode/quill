<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a CPD XML report into part of an HTML page.
 *
 * 2010-05-20 /PF    Created.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>

  <!-- This part of the path to the source files should not be used when
       converting the file path to qualified class names -->
  <xsl:param name="source-file-base" select="'src/java/'"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="pmd-cpd"/>
  </xsl:template>


  <!-- Template for the pmd-cpd element, which is the top-level element in the
       CPD report -->
  <xsl:template match="pmd-cpd">

    <div class="mainsection">

      <!-- Report header -->
      <div class="mainheader">CPD Report</div>

      <!-- Analysis run timestamp table -->
      <xsl:call-template name="output-timestamp-table" />

      <!-- Statistics table -->
      <xsl:call-template name="output-statistics-table">
        <xsl:with-param name="num-duplications" select="count(duplication)"/>
        <xsl:with-param name="num-lines" select="sum(duplication/@lines)"/>
      </xsl:call-template>

      <!-- Output details about each found duplication -->
      <xsl:if test="duplication">
        <div class="level1section">
          <div class="level1header">Duplications</div>
          <xsl:for-each select="duplication">
            <xsl:sort data-type="number" order="descending" select="count(@lines)"/>
            <xsl:call-template name="output-duplication">
              <xsl:with-param name="duplication" select="."/>
            </xsl:call-template>
          </xsl:for-each>
        </div>
      </xsl:if>

    </div>
  </xsl:template>


  <!-- Output a table with the analysis run timestamp-->
  <xsl:template name="output-timestamp-table">
      <table class="mainsectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
          <tr>
              <td class="data">
                  Analysis run on<xsl:text>&#32;</xsl:text>
                  <xsl:value-of select="$xml-modified-date"/>
                  <xsl:text>&#32;</xsl:text>
                  <xsl:value-of select="$xml-modified-time"/>
              </td>
          </tr>
      </table>
  </xsl:template>


  <!-- Output a table with some overall statistics of the CPD run -->
  <xsl:template name="output-statistics-table">
    <xsl:param name="num-duplications"/>
    <xsl:param name="num-lines"/>
    <table class="mainsectionitem" width="30%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="80%"/><col width="20%"/>
      </colgroup>
      <tr>
        <td class="label">Number of duplications:</td>
        <td class="data" align="right"><xsl:value-of select="$num-duplications"/></td>
      </tr>
      <tr>
        <td class="label">Total number of duplicated lines:</td>
        <td class="data" align="right"><xsl:value-of select="$num-lines"/></td>
      </tr>
    </table>
  </xsl:template>


  <!-- Output a table with the files in a duplication -->
  <xsl:template name="output-duplication">
    <xsl:param name="duplication"/>
    <div class="level2header">
      Duplication of <xsl:value-of select="@lines"/> lines /
      <xsl:value-of select="@tokens"/> tokens
    </div>
    <table class="level2sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="1%"/><col width="30%"/><col width="2%"/><col width="7%"/><col width="60%"/>
      </colgroup>
      <tr>
        <td/>
        <td class="colheader">File</td>
        <td/>
        <td class="colheader" align="right">Line</td>
        <td/>
      </tr>
    <xsl:for-each select="file">
      <tr>
        <!-- Use alternate row characteristics every other row -->
        <xsl:if test="position() mod 2 = 0">
          <xsl:attribute name="class">altrow</xsl:attribute>
        </xsl:if>
        <td/>
        <td class="data">
          <!-- File path relative to the source base -->
          <xsl:choose>
            <xsl:when test="contains(@path,$source-file-base)">
              <xsl:value-of select="substring-after(@path,$source-file-base)"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="@path"/></xsl:otherwise>
          </xsl:choose>
        </td>
        <td/>
        <td class="data" align="right">
          <!-- Line number -->
          <xsl:value-of select="@line"/>
        </td>
        <td/>
      </tr>
    </xsl:for-each>
    </table>
  </xsl:template>

</xsl:stylesheet>
