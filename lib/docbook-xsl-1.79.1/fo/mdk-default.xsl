<?xml version="1.0" encoding="UTF-8"?>
<!-- Version 1.0
	 by: Miyako Wilson (Georgia Tech)
		- created by including some of mgss.xsl into docbook.xsl
		- handle mms-link-view and mms-cf
		- html a, br, ...
		 
	 mgss.xsl - Version 3.0
     Updated by: Charles E Galey (313B) 4/22/13, 
     Original OpsRev version by: Doris T Lamb (393A)-->

<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format" 
    version="1.0">
    <xsl:import href="docbook.xsl"/>
   
    <xsl:param name="toc.section.depth" select="8"/>
    <xsl:param name="section.label.includes.component.label" select="1"/>
    <xsl:param name="section.autolabel" select="1"/>
    <xsl:param name="body.start.indent" select="1"/>
    
<!-- make pdf links blue and underline -->
    <xsl:attribute-set name="xref.properties">
        <xsl:attribute name="color">blue</xsl:attribute>
        <xsl:attribute name="text-decoration">underline</xsl:attribute>
    </xsl:attribute-set>
    
    
    
    <!-- chapter and appendix name customization -->
    <xsl:param name="local.l10n.xml" select="document('')"/>
    <l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0"> 
        <l:l10n language="en">
            <l:context name="title-numbered">
                <l:template name="chapter" text="%n. %t"/> 
                <l:template name="appendix" text="Appendix %n. %t"/> 
            </l:context>
        </l:l10n> 
    </l:i18n>
     
    <xsl:template name="calsTable">
        <xsl:variable name="keep.together">
            <xsl:call-template name="pi.dbfo_keep-together"/>
        </xsl:variable>
        <xsl:for-each select="tgroup">
            <fo:table xsl:use-attribute-sets="table.table.properties">
                <xsl:if test="$keep.together != ''">
                    <xsl:attribute name="keep-together.within-column">
                        <xsl:value-of select="$keep.together"/>
                    </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="table.frame"/>
                <xsl:if test="following-sibling::tgroup">
                    <xsl:attribute name="border-bottom-width">0pt</xsl:attribute>
                    <xsl:attribute name="border-bottom-style">none</xsl:attribute>
                    <xsl:attribute name="padding-bottom">0pt</xsl:attribute>
                    <xsl:attribute name="margin-bottom">0pt</xsl:attribute>
                    <xsl:attribute name="space-after">0pt</xsl:attribute>
                    <xsl:attribute name="space-after.minimum">0pt</xsl:attribute>
                    <xsl:attribute name="space-after.optimum">0pt</xsl:attribute>
                    <xsl:attribute name="space-after.maximum">0pt</xsl:attribute>
                </xsl:if>
                <xsl:if test="preceding-sibling::tgroup">
                    <xsl:attribute name="border-top-width">0pt</xsl:attribute>
                    <xsl:attribute name="border-top-style">none</xsl:attribute>
                    <xsl:attribute name="padding-top">0pt</xsl:attribute>
                    <xsl:attribute name="margin-top">0pt</xsl:attribute>
                    <xsl:attribute name="space-before">0pt</xsl:attribute>
                    <xsl:attribute name="space-before.minimum">0pt</xsl:attribute>
                    <xsl:attribute name="space-before.optimum">0pt</xsl:attribute>
                    <xsl:attribute name="space-before.maximum">0pt</xsl:attribute>
                </xsl:if>
                <xsl:apply-templates select="."/>
            </fo:table>
            <xsl:for-each select="mediaobject|graphic">
                <xsl:apply-templates select="."/>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:apply-templates select="caption"/>
    </xsl:template>
    
    
        
	<xsl:template match="mms-view-link">
	   <xsl:variable name="dmeId" select="@data-mms-element-id" />
	       <xsl:choose>
	           <xsl:when test="$dmeId">
	               <xsl:choose>
		              <xsl:when test="//section[@id=$dmeId]">
       		            <fo:block>
       		                <fo:inline keep-with-next.within-line="always">
       		                    <fo:basic-link internal-destination="{$dmeId}" xsl:use-attribute-sets="xref.properties">  
       		                        <xsl:value-of select="normalize-space(substring-before(substring-after(.,':'), '.'))"/> 
       		                    </fo:basic-link>
       		                </fo:inline>
       		            </fo:block>
  		             </xsl:when>    
     		         <xsl:otherwise>
     		            <fo:block color ="red">
     		                <fo:inline keep-with-next.within-line="always">
     		                    <xsl:value-of select="normalize-space(substring-before(substring-after(.,':'), '.'))"/> 
     		                    [error: An internal link not found]
     		                </fo:inline>
     		            </fo:block>
     		          </xsl:otherwise>
    	           </xsl:choose>
	           </xsl:when>
	           <xsl:otherwise>
	               <fo:block color ="red">
	                   <fo:inline keep-with-next.within-line="always">
	                       <xsl:value-of select="normalize-space(substring-before(substring-after(.,':'), '.'))"/> 
	                       [error: An internal link not defined]
	                   </fo:inline>
	               </fo:block>
	           </xsl:otherwise>
		  </xsl:choose>
	</xsl:template>
    
    
 <!--[cf:Create Viewpoint Methods.vlink]-->
<!-- copy from block para -->
<xsl:template match="mms-cf">
	<!--<xsl:message> ==================<xsl:value-of select="."/>
	<xsl:value-of select="@mms-cf-type"/>
	<xsl:value-of select="@mms-element-id"/>
	<xsl:value-of select=
   "substring-before(substring-after(.,':'), '-.')"/>
	</xsl:message> -->
	<xsl:value-of select="substring-before(substring-after(.,':'), '-.')"/>
	<!--
	<xsl:variable name="keep.together">
		<xsl:call-template name="pi.dbfo_keep-together"/>
	</xsl:variable>
  <fo:block xsl:use-attribute-sets="para.properties">
    <xsl:if test="$keep.together != ''">
      <xsl:attribute name="keep-together.within-column"><xsl:value-of
                      select="$keep.together"/></xsl:attribute>
					  
    </xsl:if>
    <xsl:call-template name="anchor"/>
    <xsl:apply-templates/> 
  </fo:block> -->
  
 </xsl:template>
    <xsl:template match="a">
        <xsl:variable name="href" select="@href" />
        <xsl:if test="$href != ''">
            <fo:basic-link external-destination="url({$href})" xsl:use-attribute-sets="xref.properties">  
                <xsl:value-of select="."/> 
            </fo:basic-link>
        </xsl:if>    
    </xsl:template>    
    
    <xsl:template match="p|span">
        <xsl:apply-templates/>
        <!-- <xsl:value-of select="."/> -->
    </xsl:template>     
    <xsl:template match="br">
        <xsl:text>&#x20;&#x20;</xsl:text>
        <xsl:text>&#xA;</xsl:text>
    </xsl:template>         
        <!-- unordered lists -->
        
    <xsl:template match="ol|ul">
        <fo:list-block margin-top="5pt">
            <xsl:apply-templates/>
        </fo:list-block>
    </xsl:template>
    
    <xsl:template match="ul/li">
        <fo:list-item margin-top="10pt">
            <fo:list-item-label end-indent="label-end()">
                <fo:block>&#x02022;</fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block> <xsl:value-of select="."/><xtext>?ulli????</xtext></fo:block> 
            </fo:list-item-body>
        </fo:list-item>
    </xsl:template>
    
    <xsl:template match="ol/li">
        <fo:list-item margin-top="10pt">
            <fo:list-item-label end-indent="label-end()">
                <fo:block><xsl:number count="li" level="single" format="1."/></fo:block>
            </fo:list-item-label>
            <fo:list-item-body start-indent="body-start()">
                <fo:block><xsl:value-of select="."/><xtext>????olli?</xtext></fo:block> 
            </fo:list-item-body>
        </fo:list-item>
    </xsl:template>    
    
        <!-- ordered lists -->
        
    

<!--
    <xsl:template match="xhtml:*" mode="xhtml_to_plaintext">
        <xsl:choose>
            <xsl:when test="self::text()">
                <xsl:value-of select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates mode="xhtml_to_plaintext"/>
                <xsl:variable name="html_element_type">
                    <xsl:call-template name="get_html_element_type">
                        <xsl:with-param name="e" select="local-name()"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:if test="$html_element_type = 'block'">
                    <xsl:text>
</xsl:text>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>  
    
    <xsl:template name="get_html_element_type">
        <xsl:param name="e"/>
        <xsl:choose>     
            <xsl:when test="$e='a' or $e='b' or $e='del' or $e='em' or $e='i' or $e='ins' or $e='mark' or $e='span' or $e='strike' or $e='strong' or $e='sub' or $e='sup' or $e='u'">
                <xsl:text>inline</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>block</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
-->
    
    
</xsl:stylesheet>