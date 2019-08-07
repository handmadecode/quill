<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a Scent XML report into part of an HTML page.
 *
 * 2016-10-26 /PF    Created.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="scent-report"/>
  </xsl:template>


  <!-- Template for the scent-report element, which is the top-level element in the Scent report -->
  <xsl:template match="scent-report">

    <div class="mainsection">

      <!-- Report header -->
      <div class="mainheader">Scent Report</div>

      <!-- Analysis run timestamp table -->
      <xsl:call-template name="output-timestamp-table" />

      <!-- Statistics for all packages -->
      <xsl:call-template name="output-total-table" />

      <!-- Statistics per package -->
      <xsl:call-template name="output-packages-table" />

    </div>

  </xsl:template>


    <!-- Output a table with the analysis run timestamp-->
    <xsl:template name="output-timestamp-table">
        <table class="mainsectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
            <tr>
                <td class="data">
                    Analysis run on<xsl:text>&#32;</xsl:text>
                    <xsl:value-of select="@date"/>
                    <xsl:text>&#32;</xsl:text>
                    <xsl:value-of select="@time"/>
                    <br/>Scent version: <xsl:value-of select="@version"/>
                </td>
            </tr>
        </table>
    </xsl:template>


  <!-- Output a table with statistics for all analyzed packages -->
  <xsl:template name="output-total-table">
    <table class="mainsectionitem" width="60%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="30%"/><col width="5%"/><col width="30%"/><col width="30%"/><col width="5%"/>
      </colgroup>
      <tr>
        <td class="label">Packages:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@packages"/>
        </xsl:call-template>
        <td/>
        <td class="label">JavaDocs:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@javadocs"/>
        </xsl:call-template>
      </tr>
      <tr>
        <td class="label">Compilation Units:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@compilation-units"/>
        </xsl:call-template>
        <td/>
        <td class="label">JavaDoc lines:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@javadoc-lines"/>
        </xsl:call-template>
      </tr>
      <tr>
        <td class="label">Types:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@types"/>
        </xsl:call-template>
        <td/>
        <td class="label">Single line comments:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@line-comments"/>
        </xsl:call-template>
      </tr>
      <tr>
        <td class="label">Methods:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@methods"/>
        </xsl:call-template>
        <td/>
        <td class="label">Block comments:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@block-comments"/>
        </xsl:call-template>
      </tr>
      <tr>
        <td class="label">Statements:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@statements"/>
        </xsl:call-template>
        <td/>
        <td class="label">Block comment lines:</td>
        <xsl:call-template name="output-value-cell">
          <xsl:with-param name="value" select="summary/@block-comments-lines"/>
        </xsl:call-template>
      </tr>
    </table>
  </xsl:template>


  <!-- Output a table with statistics for each analyzed package -->
  <xsl:template name="output-packages-table">
    <div class="level1section">
      <div class="level1header">Package statistics</div>
      <table class="level1sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
        <colgroup>
          <col width="19%"/>
          <col width="9%"/><col width="9%"/><col width="9%"/>
          <col width="9%"/><col width="9%"/><col width="9%"/>
          <col width="9%"/><col width="9%"/><col width="9%"/>
        </colgroup>
        <tr>
          <td class="colheader">Name</td>
          <td class="colheader" align="right">Types</td>
          <td class="colheader" align="right">Methods</td>
          <td class="colheader" align="right">Fields</td>
          <td class="colheader" align="right">Statements</td>
          <td class="colheader" align="right">JavaDocs</td>
          <td class="colheader" align="right">JavaDoc<br/>lines</td>
          <td class="colheader" align="right">Line<br/>comments</td>
          <td class="colheader" align="right">Block<br/>comments</td>
          <td class="colheader" align="right">Block<br/>comment lines</td>
        </tr>

        <xsl:for-each select="packages/package">
          <xsl:sort data-type="number" order="descending" select="summary/@types"/>
          <tr>
            <!-- Use alternate row characteristics every other row -->
            <xsl:if test="position() mod 2 = 0">
              <xsl:attribute name="class">altrow</xsl:attribute>
            </xsl:if>
            <td class="data"><xsl:value-of select="@name"/></td>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@types"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@methods"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@fields"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@statements"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@javadocs"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@javadoc-lines"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@line-comments"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@block-comments"/>
            </xsl:call-template>
            <xsl:call-template name="output-value-cell">
              <xsl:with-param name="value" select="summary/@block-comments-lines"/>
            </xsl:call-template>
          </tr>
        </xsl:for-each>
      </table>
    </div>
  </xsl:template>


  <!-- Output a table cell with a value if it exists, otherwise a marker value for non-presence -->
  <xsl:template name="output-value-cell">
    <xsl:param name="value"/>
    <td class="data" align="right">
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
