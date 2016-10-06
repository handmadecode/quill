<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a JavaNCSS XML report into part of an HTML
 * page.
 *                                                        
 * 2008-04-07 /PF    Created.
 * 2009-01-07 /PF    Using div's and style classes more consistently.
 * 2014-04-08 /PF    Added XML timestamp parameters.
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
    <xsl:apply-templates select="javancss"/>
  </xsl:template>


  <!-- Template for the javancss element, which is the top-level element in
       the JavaNCSS report -->
  <xsl:template match="javancss">

    <div class="mainsection">

      <!-- Report header -->
      <div class="mainheader">JavaNCSS Report</div>

      <!-- Analysis run timestamp table -->
      <xsl:call-template name="output-timestamp-table" />

      <!-- Statistics for all classes -->
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
                    <xsl:value-of select="$xml-modified-date"/>
                    <xsl:text>&#32;</xsl:text>
                    <xsl:value-of select="$xml-modified-time"/>
                </td>
            </tr>
        </table>
    </xsl:template>


  <!-- Output a table with statistics for each analyzed package -->
  <xsl:template name="output-packages-table">
    <div class="level1section">
      <div class="level1header">Package statistics</div>
      <table class="level1sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
        <colgroup>
          <col width="23%"/><col width="11%"/><col width="11%"/>
          <col width="11%"/><col width="11%"/><col width="11%"/>
          <col width="11%"/><col width="11%"/>
        </colgroup>
        <tr>
          <td class="colheader">Name</td>
          <td class="colheader" align="right">Classes</td>
          <td class="colheader" align="right">Methods</td>
          <td class="colheader" align="right">NCSS</td>
          <td class="colheader" align="right">JavaDocs</td>
          <td class="colheader" align="right">JavaDoc lines</td>
          <td class="colheader" align="right">Single line comments</td>
          <td class="colheader" align="right">Block comment lines</td>
        </tr>
 
        <xsl:for-each select="packages/package">
          <xsl:sort data-type="number" order="descending" select="classes"/>
          <tr>
            <!-- Use alternate row characteristics every other row -->
            <xsl:if test="position() mod 2 = 0">
              <xsl:attribute name="class">altrow</xsl:attribute>
            </xsl:if>
            <td class="data"><xsl:value-of select="name"/></td>
            <td class="data" align="right"><xsl:value-of select="classes"/></td>
            <td class="data" align="right"><xsl:value-of select="functions"/></td>
            <td class="data" align="right"><xsl:value-of select="ncss"/></td>
            <td class="data" align="right"><xsl:value-of select="javadocs"/></td>
            <td class="data" align="right"><xsl:value-of select="javadoc_lines"/></td>
            <td class="data" align="right"><xsl:value-of select="single_comment_lines"/></td>
            <td class="data" align="right"><xsl:value-of select="multi_comment_lines"/></td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
  </xsl:template>


  <!-- Output a table with statistics for all analyzed classes -->
  <xsl:template name="output-total-table">
    <table class="mainsectionitem" width="60%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="30%"/><col width="5%"/><col width="30%"/><col width="30%"/><col width="5%"/>
      </colgroup>
      <tr>
        <td class="label">Packages:</td>
        <td class="data" align="right"><xsl:value-of select="count(packages/package)"/></td>
        <td/>
        <td class="label">JavaDocs:</td>
        <td class="data" align="right"><xsl:value-of select="packages/total/javadocs"/></td>
      </tr>
      <tr>
        <td class="label">Types:</td>
        <td class="data" align="right"><xsl:value-of select="packages/total/classes + sum(objects/object/classes)"/></td>
        <td/>
        <td class="label">JavaDoc lines:</td>
        <td class="data" align="right"><xsl:value-of select="packages/total/javadoc_lines"/></td>
      </tr>
      <tr>
        <td class="label">Methods:</td>
        <td class="data" align="right"><xsl:value-of select="count(functions/function)"/></td>
        <td/>
        <td class="label">Single line comments:</td>
        <td class="data" align="right"><xsl:value-of select="packages/total/single_comment_lines"/></td>
      </tr>
      <tr>
        <td class="label">NCSS:</td>
        <td class="data" align="right"><xsl:value-of select="packages/total/ncss"/></td>
        <td/>
        <td class="label">Block comment lines:</td>
        <td class="data" align="right"><xsl:value-of select="packages/total/multi_comment_lines"/></td>
      </tr>
    </table>
  </xsl:template>


  <!-- Template to output all matched text verbatim -->
  <xsl:template match="text()"/>

</xsl:stylesheet>
