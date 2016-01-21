<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a PMD XML report into part of an HTML page.
 *
 * 2007-03-05 /PF    Created.
 * 2008-04-07 /PF    Moved generate-id() construct into output-rules-table
 *					 template as work-around for JDK 1.5 xsl bug.
 * 2008-04-18 /PF    Support for PMD 4.x beginline/endline + handling of
 *					 absent method names.
 * 2009-01-07 /PF    Using div's and style classes more consistently.
 * 2014-04-10 /PF    Added XML timestamp parameters + color coded error/warning
 *					 count.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- This part of the path to the source files should not be used when
       converting the file path to qualified class names -->
  <xsl:param name="source-file-base" select="'src/java/'"/>

  <!-- Key use to get unique rule names -->
  <xsl:key name="rule" match="violation" use="@rule"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="pmd"/>
  </xsl:template>

  <!-- Template for the pmd element, which is the top-level element in the PMD
       report -->
  <xsl:template match="pmd">

    <div class="mainsection">

      <!-- Report header -->
      <div class="mainheader">PMD Report</div>

        <!-- Analysis run timestamp table -->
      <xsl:call-template name="output-timestamp-table" />

      <!-- Statistics table -->
      <xsl:call-template name="output-statistics-table">
        <xsl:with-param name="version" select="@version"/>
        <xsl:with-param name="num-files-with-violations" select="count(file)"/>
        <xsl:with-param name="num-violations" select="count(file/violation)"/>
      </xsl:call-template>

      <!-- Output violation summary and file details if there were any
           violations -->
      <xsl:if test="file[violation]">
        <!-- Table with summary of the rules that were violated -->
        <xsl:call-template name="output-rules-table"/>
        <!-- Details for each file with violations -->
        <div class="level1section">
          <div class="level1header">File details</div>
          <xsl:for-each select="file">
            <xsl:sort data-type="number" order="descending" select="count(violation)"/>
            <xsl:call-template name="output-file-violations">
              <xsl:with-param name="file" select="."/>
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


  <!-- Output a table with some overall statistics of the PMD run -->
  <xsl:template name="output-statistics-table">
    <xsl:param name="version"/>
    <xsl:param name="num-files-with-violations"/>
    <xsl:param name="num-violations"/>
    <table class="mainsectionitem" width="30%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="80%"/><col width="20%"/>
      </colgroup>
      <tr>
        <td class="label">PMD version:</td>
        <td class="data" align="right"><xsl:value-of select="$version"/></td>
      </tr>
      <tr>
        <td class="label">Number of files with violations:</td>
        <td class="data" align="right">
          <xsl:choose>
            <xsl:when test="$num-files-with-violations &gt; 0">
              <span class="error"><xsl:value-of select="$num-files-with-violations"/></span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$num-files-with-violations"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
      <tr>
        <td class="label">Number of violations:</td>
        <td class="data" align="right">
          <xsl:choose>
            <xsl:when test="$num-violations &gt; 0">
              <span class="error"><xsl:value-of select="$num-violations"/></span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$num-violations"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
    </table>
  </xsl:template>


  <!-- Output a table with a list of all violated rules and the number of
       violations for each rule -->
  <xsl:template name="output-rules-table">
    <div class="level1section">
      <div class="level1header">Violated rules summary</div>
      <table class="level1sectionitem" width="60%" cellpadding="2" cellspacing="0" border="0">
        <colgroup>
            <col width="70%"/><col width="15%"/><col width="15%"/>
        </colgroup>
        <tr>
          <td class="colheader">Rule</td>
          <td class="colheader" align="right">Files</td>
          <td class="colheader" align="right">Count</td>
        </tr>
 
        <!-- The select expression generates a set with the first error node for
             each unique error source -->
        <xsl:for-each select="file/violation[generate-id() = generate-id(key('rule', @rule)[1])]">
          <!-- Sort the list on the number of occurrences of the rule -->
          <xsl:sort data-type="number" order="descending"
                    select="count(../../file/violation[@rule=current()/@rule])"/>
          <!-- Get the set of violations that have the current rule -->
          <xsl:variable name="violation-set" select="../../file/violation[@rule=current()/@rule]"/>
          <tr>
            <!-- Use alternate row characteristics every other row -->
            <xsl:if test="position() mod 2 = 0">
              <xsl:attribute name="class">altrow</xsl:attribute>
            </xsl:if>
            <!-- Output the name of the rule as a link to the PMD external info URL -->
            <td class="data">
              <a target="_blank">
               <xsl:attribute name="href"><xsl:value-of select="@externalInfoUrl"/></xsl:attribute>
               <xsl:value-of select="@rule"/>
              </a>
            </td>
            <td class="data" align="right"><xsl:value-of select="count($violation-set/..)"/></td>
            <td class="data" align="right"><xsl:value-of select="count($violation-set)"/></td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
  </xsl:template>


  <!-- Output a table with a list of all violated rules for a specific file -->
  <xsl:template name="output-file-violations">
    <xsl:param name="file"/>
    <div class="level2header">
      <xsl:choose>
        <xsl:when test="contains(@name,$source-file-base)">
          <xsl:value-of select="substring-after(@name,$source-file-base)"/>
        </xsl:when>
       <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
      </xsl:choose>
    : <xsl:value-of select="count(violation)"/> violation(s)
    </div>
    <table class="level2sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="15%"/><col width="7%"/><col width="7%"/><col width="2%"/>
        <col width="25%"/><col width="44%"/>
      </colgroup>
      <tr>
        <td class="colheader">Method</td>
        <td class="colheader" align="right">Line</td>
        <td class="colheader" align="right">Priority</td>
        <td/>
        <td class="colheader">Rule</td>
        <td class="colheader">Message</td>
      </tr>
    <xsl:for-each select="violation">
      <tr>
        <!-- Use alternate row characteristics every other row -->
        <xsl:if test="position() mod 2 = 0">
          <xsl:attribute name="class">altrow</xsl:attribute>
        </xsl:if>
        <td class="data">
          <xsl:choose>
            <xsl:when test="string-length(@method) &gt; 0">
              <xsl:value-of select="@method"/>()
            </xsl:when>
	        <xsl:otherwise>-</xsl:otherwise>
          </xsl:choose>
        </td>
        <td class="data" align="right">
          <xsl:choose>
            <xsl:when test="string-length(@line) &gt; 0">
              <xsl:value-of select="@line"/>
            </xsl:when>
            <xsl:when test="string-length(@beginline) &gt; 0">
              <xsl:value-of select="@beginline"/>
              <xsl:if test="@beginline != @endline">-<xsl:value-of select="@endline"/></xsl:if>
            </xsl:when>
	        <xsl:otherwise>-</xsl:otherwise>
          </xsl:choose>
        </td>
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
        <!-- Output the name of the rule as a link to the PMD external info URL -->
        <td class="data">
          <a target="_blank">
           <xsl:attribute name="href"><xsl:value-of select="@externalInfoUrl"/></xsl:attribute>
           <xsl:value-of select="@rule"/>
          </a>
        </td>
        <td class="data"><xsl:value-of select="text()"/></td>
      </tr>
    </xsl:for-each>
    </table>
  </xsl:template>

</xsl:stylesheet>
