<?xml version="1.0"?>
<!--
 *******************************************************************************
 *
 * XSL style sheet for transforming a Checkstyle XML report into part of an HTML
 * page.
 *
 * 2007-03-05 /PF    Created.
 * 2008-04-07 /PF    Moved generate-id() construct into output-violations-table
 *					 template as work-around for JDK 1.5 xsl bug.
 *                   Fixed calculation of number of files with violations.
 *                   Fixed mixup of file/count in output-violations-table.
 * 2009-01-07 /PF    Using div's and style classes more consistently.
 * 2014-04-07 /PF    Added XML timestamp parameters + color coded error/warning
 *					 count.
 *
 *******************************************************************************
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output method="html"/>

  <!-- This part of the path to the source files should not be used when
       converting the file path to qualified class names -->
  <xsl:param name="source-file-base" select="'src/java/'"/>

  <!-- Key use to get unique file names -->
  <xsl:key name="filename" match="file" use="@name"/>

  <!-- Key use to get unique violating file names -->
  <xsl:key name="violatingfilename" match="error" use="../@name"/>

  <!-- Key use to get unique sources, i.e. names of a violated check -->
  <xsl:key name="source" match="error" use="@source"/>

  <!-- Parameters where the XML file's modification timestamp are passed by the
       caller -->
  <xsl:param name="xml-modified-date"/>
  <xsl:param name="xml-modified-time"/>


  <!-- Main template for the document root -->
  <xsl:template match="/">
    <xsl:apply-templates select="checkstyle"/>
  </xsl:template>

  <!-- Template for the checkstyle element, which is the top-level element in
       the Checkstyle report -->
  <xsl:template match="checkstyle">

    <div class="mainsection">

      <!-- Report header -->
      <div class="mainheader">Checkstyle Report</div>

      <!-- Analysis run timestamp table -->
      <xsl:call-template name="output-timestamp-table" />

      <!-- Statistics table -->
      <xsl:call-template name="output-statistics-table">
        <xsl:with-param name="version" select="@version"/>
        <!-- Checkstyle may loop through the fileset multiple times depending on
             which checks are run, and a file may thus occur more than once in
             the output -->
        <xsl:with-param name="num-analyzed-files"
                        select="count(file[generate-id() = generate-id(key('filename', @name)[1])])"/>
        <xsl:with-param name="num-files-with-violations"
                        select="count(file/error[generate-id() = generate-id(key('violatingfilename', ../@name)[1])])"/>
        <xsl:with-param name="num-errors" select="count(file/error[@severity='error'])"/>
        <xsl:with-param name="num-warnings" select="count(file/error[@severity='warning'])"/>
      </xsl:call-template>

      <!-- Output violation summary and file details if there were any
           violations -->
      <xsl:if test="file[error]">
        <!-- Table with summary of the checks that were violated -->
        <xsl:call-template name="output-violations-table"/>
        <!-- Details for each file with violations -->
        <div class="level1section">
          <div class="level1header">File details</div>
          <xsl:for-each select="file[error]">
            <xsl:sort data-type="number" order="descending" select="count(error)"/>
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


  <!-- Output a table with some overall statistics of the checkstyle run -->
  <xsl:template name="output-statistics-table">
    <xsl:param name="version"/>
    <xsl:param name="num-analyzed-files"/>
    <xsl:param name="num-files-with-violations"/>
    <xsl:param name="num-errors"/>
    <xsl:param name="num-warnings"/>
    <table class="mainsectionitem" width="30%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="80%"/><col width="20%"/>
      </colgroup>
      <tr>
        <td class="label">Checkstyle version:</td>
        <td class="data" align="right"><xsl:value-of select="$version"/></td>
      </tr>
      <tr>
        <td class="label">Number of analyzed files:</td>
        <td class="data" align="right"><xsl:value-of select="$num-analyzed-files"/></td>
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
        <td class="label">Number of errors:</td>
        <td class="data" align="right">
          <xsl:choose>
            <xsl:when test="$num-errors &gt; 0">
              <span class="error"><xsl:value-of select="$num-errors"/></span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$num-errors"/>
            </xsl:otherwise>
        </xsl:choose>
        </td>
      </tr>
      <tr>
        <td class="label">Number of warnings:</td>
        <td class="data" align="right">
          <xsl:choose>
            <xsl:when test="$num-warnings &gt; 0">
              <span class="warning"><xsl:value-of select="$num-warnings"/></span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$num-warnings"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
    </table>
  </xsl:template>


  <!-- Output a table with a list of all violated checks and the number of
       occurrences for each check -->
  <xsl:template name="output-violations-table">
    <div class="level1section">
      <div class="level1header">Violated checks summary</div>
      <table class="level1sectionitem" width="60%" cellpadding="2" cellspacing="0" border="0">
        <colgroup>
          <col width="70%"/><col width="15%"/><col width="15%"/>
        </colgroup>
        <tr>
          <td class="colheader">Check</td>
          <td class="colheader" align="right">Files</td>
          <td class="colheader" align="right">Count</td>
        </tr>

        <!-- The select expression generates a set with the first error node for
             each unique error source -->
        <xsl:for-each select="file/error[generate-id() = generate-id(key('source', @source)[1])]">
          <!-- Sort the list on the number of occurrences of the violation's source -->
          <xsl:sort data-type="number" order="descending"
                    select="count(../../file/error[@source=current()/@source])"/>
          <!-- Get the set of violations that have the current source -->
          <xsl:variable name="error-set" select="../../file/error[@source=current()/@source]"/>
          <tr>
            <!-- Use alternate row characteristics every other row -->
            <xsl:if test="position() mod 2 = 0">
              <xsl:attribute name="class">altrow</xsl:attribute>
            </xsl:if>
            <td class="data">
              <xsl:call-template name="substring-after-last-dot">
                <xsl:with-param name="str" select="@source"/>
              </xsl:call-template>
            </td>
            <td class="data" align="right"><xsl:value-of select="count($error-set/..)"/></td>
            <td class="data" align="right"><xsl:value-of select="count($error-set)"/></td>
          </tr>
        </xsl:for-each>
      </table>
    </div>
  </xsl:template>


  <!-- Output a table with a list of all violated checks for a specific file -->
  <xsl:template name="output-file-violations">
    <xsl:param name="file"/>
    <div class="level2header">
      <xsl:choose>
        <xsl:when test="contains(@name,$source-file-base)">
          <xsl:value-of select="substring-after(@name,$source-file-base)"/>
        </xsl:when>
       <xsl:otherwise><xsl:value-of select="@name"/></xsl:otherwise>
      </xsl:choose>
      : <xsl:value-of select="count(error[@severity='error'])"/> error(s),
      <xsl:value-of select="count(error[@severity='warning'])"/> warning(s)
    </div>
    <table class="level2sectionitem" width="100%" cellpadding="2" cellspacing="0" border="0">
      <colgroup>
        <col width="5%"/><col width="5%"/><col width="2%"/>
        <col width="10%"/><col width="25%"/><col width="53%"/>
      </colgroup>
      <tr>
        <td class="colheader" align="right">Line</td>
        <td class="colheader" align="right">Column</td>
        <td/>
        <td class="colheader">Severity</td>
        <td class="colheader">Violated check</td>
        <td class="colheader">Message</td>
      </tr>
    <xsl:for-each select="error">
      <tr>
        <!-- Use alternate row characteristics every other row -->
        <xsl:if test="position() mod 2 = 0">
          <xsl:attribute name="class">altrow</xsl:attribute>
        </xsl:if>
        <td class="data" align="right"><xsl:value-of select="@line"/></td>
        <td class="data" align="right"><xsl:value-of select="@column"/></td>
        <td/>
        <td class="data">
          <span class="{@severity}"><xsl:value-of select="@severity"/></span>
        </td>
        <td class="data">
          <xsl:call-template name="substring-after-last-dot">
            <xsl:with-param name="str" select="@source"/>
          </xsl:call-template>
        </td>
        <td class="data"><xsl:value-of select="@message"/></td>
      </tr>
    </xsl:for-each>
    </table>
  </xsl:template>

  <!-- Extract everything in a string after the last '.' -->
  <xsl:template name="substring-after-last-dot">
    <xsl:param name="str"/>
    <xsl:choose>
      <xsl:when test="contains($str, '.')">
        <xsl:call-template name="substring-after-last-dot">
          <xsl:with-param name="str" select="substring-after($str, '.')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


</xsl:stylesheet>
