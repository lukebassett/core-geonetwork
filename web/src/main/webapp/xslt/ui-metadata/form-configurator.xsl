<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:gn="http://www.fao.org/geonetwork"
  xmlns:gn-fn-metadata="http://geonetwork-opensource.org/xsl/functions/metadata"
  xmlns:saxon="http://saxon.sf.net/" extension-element-prefixes="saxon"
  exclude-result-prefixes="#all" version="2.0">
  <!-- 
    Build the form from the schema plugin form configuration.
    -->


  <!-- Create a fieldset in the editor with custom
    legend if attribute name is defined or default 
    legend according to the matching element. -->
  <xsl:template mode="form-builer" match="section[@name]|fieldset">
    <xsl:param name="base" as="node()"/>

    <xsl:variable name="sectionName" select="@name"/>

    <xsl:choose>
      <xsl:when test="$sectionName">
        <fieldset>
          <!-- Get translation for labels.
          If labels contains ':', search into labels.xml. -->
          <legend>
            <xsl:value-of
              select="if (contains($sectionName, ':')) 
                then gn-fn-metadata:getLabel($schema, $sectionName, $labels)/label 
                else $strings/*[name() = $sectionName]"
            />
          </legend>
          <xsl:apply-templates mode="form-builer" select="@*|*">
            <xsl:with-param name="base" select="$base"/>
          </xsl:apply-templates>
        </fieldset>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="form-builer" select="@*|*">
          <xsl:with-param name="base" select="$base"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>



  <!-- Element to ignore in that mode -->
  <xsl:template mode="form-builer" match="@name"/>

  <!-- For each field, fieldset and section, check the matching xpath
    is in the current document. In that case dispatch to the schema mode
    or create an XML snippet editor for non matching document based on the
    template element. -->
  <xsl:template mode="form-builer" match="field|fieldset|section[@xpath]">
    <!-- The XML document to edit -->
    <xsl:param name="base" as="node()"/>

    <xsl:if test="@xpath">
      <!-- Match any nodes in the metadata with the XPath -->
      <!--<xsl:variable name="nodes" select="saxon:evaluate(concat('$p1/..', @xpath), $base)"/>-->
      <xsl:variable name="nodes">
        <saxon:call-template name="{concat('evaluate-', $schema)}">
          <xsl:with-param name="base" select="$base"/>
          <xsl:with-param name="in" select="@xpath"/>
        </saxon:call-template>
      </xsl:variable>

      <xsl:variable name="nonExistingChildParent">
        <xsl:if test="@or and @in">
          <saxon:call-template name="{concat('evaluate-', $schema)}">
            <xsl:with-param name="base" select="$base"/>
            <xsl:with-param name="in" select="concat(@in, '[gn:child/@name=''', @or, ''']')"/>
          </saxon:call-template>
        </xsl:if>
      </xsl:variable>

      <!-- Check if this field is controlled by a condition (eg. display that field for 
                service metadata record only).
                If @if expression return false, the field is not displayed. -->

      <xsl:variable name="isDisplayed">
        <xsl:choose>
          <xsl:when test="@if">
            <saxon:call-template name="{concat('evaluate-', $schema)}">
              <xsl:with-param name="base" select="$base"/>
              <xsl:with-param name="in" select="@if"/>
            </saxon:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="true()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <!--
      <xsl:message>Xpath         : <xsl:copy-of select="@xpath"/></xsl:message>
      <xsl:message>Matching nodes: <xsl:copy-of select="$nodes"/></xsl:message>
      <xsl:message>Non existing child path: <xsl:value-of select="concat(@in, '/gn:child[@name = ''', @or, ''']')"/></xsl:message>
      <xsl:message>Non existing child: <xsl:copy-of select="$nonExistingChildParent"/></xsl:message>
      <xsl:message>       display: <xsl:copy-of select="$isDisplayed"/></xsl:message>-->


      <xsl:if test="$isDisplayed">
        <xsl:for-each select="$nodes">
          <saxon:call-template name="{concat('dispatch-', $schema)}">
            <xsl:with-param name="base" select="."/>
          </saxon:call-template>
        </xsl:for-each>

        <!-- For non existing node create a XML snippet to be edited 
        No match in current document. 2 scenario here:
        1) the requested element is a direct child of a node of the document. 
        In that case, a geonet:child element should exist in the document.
        -->

        <xsl:choose>
          <xsl:when test="$nonExistingChildParent/*">
            <xsl:variable name="childName" select="@or"/>

            <xsl:for-each select="$nonExistingChildParent/*/gn:child[@name = $childName]">
              <xsl:call-template name="render-element-to-add">
                <xsl:with-param name="label"
                  select="gn-fn-metadata:getLabel($schema, concat(@prefix, ':', @name), $labels)/label"/>
                <xsl:with-param name="childEditInfo" select="."/>
                <xsl:with-param name="parentEditInfo" select="../gn:element"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:when>
          <xsl:when test="template">
            <!-- 
          2) the requested element is a subchild and is not described in the
            metadocument. This mode will probably take precedence over the others
            if defined in a view.
            -->
            <xsl:message>!<xsl:copy-of select="$nodes"/></xsl:message>
            <xsl:if test="normalize-space($nodes) = '' and template">

              <!--<xsl:message>!<xsl:copy-of select="template/snippet"/></xsl:message>-->
              
              <xsl:variable name="id" select="generate-id()"/>
              <div class="form-group">
                <label title="{@xpath}" class="col-lg-2">
                  <xsl:value-of select="@name"/>
                </label>
                <div class="col-lg-8">
                  <xsl:for-each select="template/values/key">
                    <label>
                      <xsl:value-of select="@label"/>
                    </label>
                    <input class="form-control" type="{@use}" value="" id="{$id}_{@label}"/>
                  </xsl:for-each>
                  <textarea class="form-control" name="_X_TODO" data-gn-template-field="{$id}" data-keys="{string-join(template/values/key/@label, ',')}">
                    <xsl:copy-of select="template/snippet"/>
                  </textarea>
                </div>
              </div>
            </xsl:if>
          </xsl:when>
        </xsl:choose>

      </xsl:if>
    </xsl:if>


  </xsl:template>
</xsl:stylesheet>
