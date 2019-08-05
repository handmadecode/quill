<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a SpotBugs XML report into part of an HTML
 * page.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

    <!-- Parameters where the XML file's modification timestamp are passed by the
         caller -->
    <xsl:param name="xml-modified-date"/>
    <xsl:param name="xml-modified-time"/>

  <!-- Key use to get unique class names -->
  <xsl:key name="classname" match="Class" use="@classname"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="BugCollection"/>
  </xsl:template>

  <!-- Template for the BugCollection element, which is the top-level element in
       the SpotBugs report -->
  <xsl:template match="BugCollection">

    <div class="mainsection">

      <!-- Report header -->
      <div class="mainheader">SpotBugs Report</div>

      <!-- Analysis run timestamp table -->
      <xsl:call-template name="output-timestamp-table" />

      <!-- Count the total number of bugs -->
      <xsl:variable name="num-bugs" select="count(BugInstance)"/>

      <!-- Statistics table -->
      <xsl:call-template name="output-statistics-table">
        <xsl:with-param name="version" select="@version"/>
        <xsl:with-param name="num-analyzed-packages" select="FindBugsSummary/@num_packages"/>
        <xsl:with-param name="num-analyzed-classes" select="FindBugsSummary/@total_classes"/>
        <xsl:with-param name="num-problems" select="$num-bugs"/>
      </xsl:call-template>

      <!-- Output bug patterns summary and class details if there were any bugs -->
      <xsl:if test="$num-bugs &gt; 0">

        <!-- Table with summary of the bug patterns found -->
        <xsl:call-template name="output-bug-pattern-table" />

        <!-- Details for each bug instance -->
        <xsl:call-template name="output-classes-table" />

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


  <!-- Output a table with some overall statistics of the SpotBugs run -->
  <xsl:template name="output-statistics-table">
    <xsl:param name="version"/>
    <xsl:param name="num-analyzed-packages"/>
    <xsl:param name="num-analyzed-classes"/>
    <xsl:param name="num-problems"/>
    <table class="mainsectionitem" width="30%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="80%"/><col width="20%"/>
      </colgroup>
      <tr>
        <td class="label">SpotBugs version:</td>
        <td class="data" align="right"><xsl:value-of select="$version"/></td>
      </tr>
      <tr>
        <td class="label">Number of analyzed packages:</td>
        <td class="data" align="right"><xsl:value-of select="$num-analyzed-packages"/></td>
      </tr>
      <tr>
        <td class="label">Number of analyzed classes:</td>
        <td class="data" align="right"><xsl:value-of select="$num-analyzed-classes"/></td>
      </tr>
      <tr>
        <td class="label">Number of problems:</td>
        <td class="data" align="right">
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
    </table>
  </xsl:template>


  <!-- Output a table with a list of all found bug patterns and the number of
       occurrences for each pattern -->
  <xsl:template name="output-bug-pattern-table">
    <div class="level1section">
      <div class="level1header">Bug pattern summary</div>
      <table class="level1sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
        <colgroup>
          <col width="10%"/><col width="8%"/><col width="2%"/><col width="60%"/><col width="20%"/>
        </colgroup>
        <tr>
          <td class="colheader">Pattern</td>
          <td class="colheader" align="right">Count</td>
          <td/>
          <td class="colheader">Description</td>
          <td class="colheader">Category</td>
        </tr>

        <xsl:for-each select="BugPattern">
          <!-- Sort the list on the number of occurrences of the problem -->
          <xsl:sort data-type="number" order="descending"
                    select="count(../BugInstance[@type=current()/@type])"/>
          <tr>
            <!-- Use alternate row characteristics every other row -->
            <xsl:if test="position() mod 2 = 0">
              <xsl:attribute name="class">altrow</xsl:attribute>
            </xsl:if>
            <td class="data"><xsl:value-of select="@abbrev"/></td>
            <td class="data" align="right"><xsl:value-of select="count(../BugInstance[@type=current()/@type])"/></td>
            <td/>
            <td class="data"><xsl:value-of select="ShortDescription"/></td>
            <td class="data"><xsl:value-of select="../BugCategory[@category = current()/@category]/Description"/></td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
  </xsl:template>


  <!-- Output a table with a list of all bug instances for a class -->
  <xsl:template name="output-classes-table">
    <div class="level1section">
      <div class="level1header">Class details</div>
      <!-- The select expression generates a set with the first class node for
           each unique class -->
      <xsl:for-each select="BugInstance/Class[generate-id() = generate-id(key('classname', @classname)[1])]">
        <!-- Sort the list on the number of BugInstances that refer to this
             class -->
        <xsl:sort data-type="number" order="descending"
                  select="count(../../BugInstance/Class[@classname=current()/@classname])"/>
        <div class="level2header"><xsl:value-of select="@classname"/></div>
        <table class="level2sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
          <colgroup>
            <col width="5%"/><col width="6%"/><col width="2%"/>
            <col width="7%"/><col width="80%"/>
          </colgroup>
          <tr>
            <td class="colheader" align="right">Line</td>
            <td class="colheader" align="right">Priority</td>
            <td/>
            <td class="colheader">Pattern</td>
            <td class="colheader">Message</td>
          </tr>

        <!-- Loop over the set of BugInstances that occur in the current class -->
        <xsl:for-each select="../../BugInstance[Class/@classname=current()/@classname]">
          <!-- Sort on line number -->
          <xsl:sort data-type="number" order="ascending" select="SourceLine/@start"/>
          <!-- Get the source line(s), if any -->
          <xsl:variable name="source-lines">
            <xsl:choose>
              <xsl:when test="SourceLine">
                <xsl:for-each select="SourceLine">
                  <xsl:if test="position() &gt; 1">,</xsl:if>
                  <xsl:choose>
                    <xsl:when test="@start = @end">
                      <xsl:value-of select="@start"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:value-of select="@start"/>-<xsl:value-of select="@end"/>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>?</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <tr>
            <!-- Use alternate row characteristics every other row -->
            <xsl:if test="position() mod 2 = 0">
              <xsl:attribute name="class">altrow</xsl:attribute>
            </xsl:if>
            <td class="data" align="right"><xsl:value-of select="$source-lines"/></td>
            <td class="data" align="right">
              <xsl:choose>
                <xsl:when test="@priority &lt;= 1">
                  <span class="error"><xsl:value-of select="@priority"/></span>
                </xsl:when>
                <xsl:otherwise>
                  <span class="warning"><xsl:value-of select="@priority"/></span>
                </xsl:otherwise>
              </xsl:choose>
            </td>
            <td/>
            <td class="data"><xsl:value-of select="@abbrev"/></td>
            <td class="data"><xsl:value-of select="LongMessage"/></td>
          </tr>
        </xsl:for-each>
        </table>
      </xsl:for-each>
    </div>
  </xsl:template>

</xsl:stylesheet>
