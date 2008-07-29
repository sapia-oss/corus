<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sapia="http://www.sapia-oss.org/2003/XSL/Transform">
                
<!-- ========================================= PAGE ========================================= -->
                
  <xsl:template match="/sapia:page">
    <html>
    <head>

      <script Language="JavaScript">
      <xsl:text>&lt;!--
      function popup(url, name, width, height)
      {
      settings=
      "toolbar=no,location=no,directories=no,"+
      "status=no,menubar=no,scrollbars=yes,"+
      "resizable=yes,width="+width+",height="+height;
      
      MyNewWindow=window.open(url,name,settings);
      }
      //--&gt;</xsl:text>
      </script>     
    
    <title><xsl:value-of select="@title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    <link rel="stylesheet" href="css/sapia.css" type="text/css"/>
    </head>
    <body bgcolor="#F5EEB8" text="#000000">    
    <xsl:apply-templates select="sapia:refreshMenu"/>
    <table width="100%" border="0" cellspacing="0">
      <tr>
        <td width="600">
        <xsl:apply-templates select="sapia:sect1"/>
        </td>
      </tr>
    </table>
    </body>
    </html>    
  </xsl:template>        

  <xsl:template match="sapia:sect1">
    <table width="600" border="0" class="titleTable" bgcolor="#FFFFFF" bordercolor="#FFFFFF">
      <tr> 
        <td class="titleTable" align="left" valign="top">
          <xsl:apply-templates select="sapia:section" />
          <br/>
          <xsl:call-template name="displayAnchor"/>
          <h1><xsl:value-of select="@title"/></h1>
        </td>
      </tr>
    </table>
    <table width="600" border="1" class="textTable" align="left" valign="top" bordercolor="#FFFFFF" cellpadding="20">
      <tr> 
        <td class="textTable"> 
          <xsl:apply-templates select="sapia:sect-desc"/>
          <xsl:if test="toc">
          <ul>
          <xsl:call-template name="toc2"/>
          </ul>
          </xsl:if>
          <xsl:apply-templates select="sapia:sect2"/>
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template match="sapia:sect-desc">
    <xsl:apply-templates/>
  </xsl:template>        
  
  <xsl:template match="sapia:sect2">
    <xsl:call-template name="displayAnchor"/>
    <h2><xsl:value-of select="@title"/></h2>
    <xsl:apply-templates/>    
  </xsl:template>

  <xsl:template match="sapia:sect3">
    <xsl:call-template name="displayAnchor"/>
    <h3><xsl:value-of select="@title"/></h3>
    <xsl:apply-templates/>    
  </xsl:template>

  <xsl:template match="sapia:sect4">
    <xsl:call-template name="displayAnchor"/>
    <h4><xsl:value-of select="@title"/></h4>
    <xsl:apply-templates/>    
  </xsl:template>
  
  <xsl:template match="sapia:sect5">
    <xsl:call-template name="displayAnchor"/>
    <h5><xsl:value-of select="@title"/></h5>
    <xsl:apply-templates/>    
  </xsl:template>

  <xsl:template match="sapia:sect6">
    <xsl:call-template name="displayAnchor" /> 
    <h6><xsl:value-of select="@title"/></h6>
    <xsl:apply-templates/>    
  </xsl:template>
  
  <xsl:template name="displayAnchor">
    <xsl:choose>
    <xsl:when test="@alias">
      <a><xsl:attribute name="name"><xsl:value-of select="@alias"/></xsl:attribute></a>
    </xsl:when>
    <xsl:otherwise>
      <a><xsl:attribute name="name"><xsl:value-of select="@title"/></xsl:attribute></a>    
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
<!-- =====================================      TOC     ====================================== -->

<xsl:template name="toc2">
  <xsl:for-each select="sapia:sect2">
    <li><xsl:call-template name="displayLink"/>
    <ul>
      <xsl:for-each select="sapia:sect3">
        <xsl:call-template name="toc3"/>
      </xsl:for-each>
    </ul>
    </li>
  </xsl:for-each> 
</xsl:template>

<xsl:template name="toc3">
  <li><xsl:call-template name="displayLink"/>
    <ul>
      <xsl:for-each select="sapia:sect4">    
         <xsl:call-template name="toc4"/>
      </xsl:for-each>      
    </ul>
    </li>
</xsl:template>

<xsl:template name="toc4">
  <li><xsl:call-template name="displayLink"/>
    <ul>
      <xsl:for-each select="sapia:sect5">    
         <xsl:call-template name="toc5"/>
      </xsl:for-each>      
    </ul>
    </li>
</xsl:template>

<xsl:template name="toc5">
  <li><xsl:call-template name="displayLink"/></li>
</xsl:template>

<xsl:template name="displayLink">
 <font size="1">
  <xsl:choose>
  <xsl:when test="@alias">
    <a><xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="@alias"/></xsl:attribute><xsl:value-of select="@title"/></a>
  </xsl:when>
  <xsl:otherwise>
    <a><xsl:attribute name="href"><xsl:text>#</xsl:text><xsl:value-of select="@title"/></xsl:attribute><xsl:value-of select="@title"/></a>    
  </xsl:otherwise>
  </xsl:choose>
  </font>
</xsl:template>

  
<!-- =====================================      SECTION PATH     ====================================== -->  
  
  <xsl:template match="sapia:section">
    <xsl:for-each select="sapia:path">
      <xsl:choose>
      <xsl:when test="@href">
        <a><xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
          <xsl:attribute name="target"><xsl:text>main</xsl:text></xsl:attribute>        
          <xsl:text>/</xsl:text><xsl:value-of select="@name"/></a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>/</xsl:text><xsl:value-of select="@name"/>
      </xsl:otherwise>
      </xsl:choose>      
    </xsl:for-each>
  </xsl:template>          
  
<!-- ======================================     REFRESH MENU    ====================================== -->

  <xsl:template match="sapia:refreshMenu">
    <xsl:attribute name="onLoad">
    <xsl:text>javascript:top.sidebar.location.href='</xsl:text>
    <xsl:value-of select="@page"/>
    <xsl:text>'</xsl:text>
    </xsl:attribute>
  
  </xsl:template>

<!-- ======================================        ANCHOR      ====================================== -->

  <xsl:template match="a">
    <a>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
      <xsl:when test="@href">
        <b><font color="#0066CC"><xsl:value-of select="."/></font></b>      
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>      
      </xsl:otherwise>
      </xsl:choose>
    </a>
  </xsl:template>
  
<!-- ======================================        POP UP      ====================================== -->

  <xsl:template match="sapia:popup">
    <a href="#">
    <xsl:attribute name="onClick">
    <xsl:text>popup('</xsl:text>
    <xsl:value-of select="@href"/><xsl:text>','</xsl:text>
    <xsl:value-of select="@title"/><xsl:text>',</xsl:text>
    <xsl:value-of select="@width"/><xsl:text>,</xsl:text>
    <xsl:value-of select="@height"/><xsl:text>); return false</xsl:text>    
    </xsl:attribute><b><font color="#0066CC"><xsl:value-of select="."/></font></b>
    </a>
  </xsl:template>  
  
<!-- =========================================      NOTE     ========================================= -->

   <xsl:template match="sapia:note">    
     <table width="100%" border="0" bgcolor="#FFFFCC" cellpadding="20">
       <tr><td><b><font size="2">
       <xsl:apply-templates/>       
       </font></b></td></tr>
     </table>    
   </xsl:template>
   
<!-- =========================================      NOTE     ========================================= -->

   <xsl:template match="sapia:command">    
       <font size="1" face="courier, courier new">
       <xsl:apply-templates/>       
       </font>
   </xsl:template>   

<!-- =========================================     CLASS     ========================================= -->

   <xsl:template match="sapia:class">    
       <b><font size="1" face="courier, courier new">
       <xsl:apply-templates/>       
       </font></b>
   </xsl:template>   

<!-- =========================================      CODE     ========================================= -->

   <xsl:template match="sapia:code">    
     <table width="100%" border="0" bgcolor="#FFFFCC" cellpadding="20">
       <tr><td><font size="3"><pre>
<xsl:apply-templates/>  
       </pre></font></td></tr>
     </table> 
   </xsl:template>

<!-- =========================================     TABLE     ========================================= -->

   <xsl:template match="sapia:table">    
     <table class="sapiaTable" cellspacing="5">
        <xsl:apply-templates select="@*"/>
        <xsl:apply-templates/>
     </table>    
   </xsl:template>
   
   <xsl:template match="sapia:th">
      <th bgcolor="#FFFFCC">
        <xsl:apply-templates select="@*"/>      
        <xsl:apply-templates/>
      </th>
   </xsl:template>   

<!-- ========================================= VERTICAL MENU ========================================= -->

  <xsl:template match="/sapia:vmenu">
    <html>
    <head>
      <title>Sidebar</title>
      <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
      <link rel="stylesheet" href="css/sapia.css" type="text/css"/>
    </head>
    <body bgcolor="#F5EEB8" text="#000000">
    <table width="150" border="0" cellpadding="5">
    <xsl:apply-templates select="sapia:vsection"/>
      <tr>
        <td height="100" valign="bottom" align="left">
          <xsl:apply-templates select="sapia:sflogo"/>
        </td>
      </tr>
    </table>
    </body>
    </html>
  </xsl:template>
  
  <xsl:template match="sapia:vsection">
    <tr>
      <xsl:choose>
      <xsl:when test="@href">
        <td class="menuTable" bgcolor="#FFFFCC"><a>      
        <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
        <xsl:attribute name="target"><xsl:text>main</xsl:text></xsl:attribute>        
        <xsl:value-of select="@name"/>
        </a></td>
      </xsl:when>
      <xsl:otherwise>
        <td class="menuTable" bgcolor="#FFFFCC">
          <b><xsl:value-of select="@name"/></b>
        </td>
      </xsl:otherwise>
      </xsl:choose>      
    </tr>   
    <xsl:for-each select="sapia:vitem">
      <tr>    
      <td class="menuTable">    
        <a><xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
          <xsl:attribute name="target"><xsl:text>main</xsl:text></xsl:attribute>      
          <font size="1"><xsl:value-of select="@name"/></font>
        </a>
      </td>
     </tr>
    </xsl:for-each>
  </xsl:template>
  
 <!-- --> 
 
  <xsl:template match="sapia:sflogo">
<a href="http://sourceforge.net"><img src="http://sourceforge.net/sflogo.php?group_id=68099&amp;type=3" width="125" height="37" border="0" alt="SourceForge.net Logo" /></a>
  
  </xsl:template>
  
 <!-- ========================================= LICENSE ========================================= -->  

  <xsl:template match="sapia:license">
<p>
This license is based on the Apache Software License, Version 1.1
</p>
<p>
 Copyright (c) 2002, 2003 Sapia Open Source Software.  All rights
 reserved.
</p>
<p>
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
</p>
<ul>
 <li>Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.</li>
 
 <li>Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the
     distribution.</li>
 
 <li>The end-user documentation included with the redistribution,
     if any, must include the following acknowledgment:
     "This product includes software developed by 
      Sapia Open Source Software Organization (http://www.sapia-oss.org/)."
      Alternately, this acknowledgment may appear in the software itself,
      if and wherever such third-party acknowledgments normally appear.</li> 
 
 <li>The names "Sapia", "Sapia Open Source Software" and "Sapia OSS" must
     not be used to endorse or promote products derived from this
     software without prior written permission. For written
     permission, please contact info@sapia-oss.org.</li>
 
 <li>Products derived from this software may not be called "Sapia",
     nor may "Sapia" appear in their name, without prior written
     permission of Sapia OSS.</li>
 
</ul>
<p>
 THIS SOFTWARE IS PROVIDED ''AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE SAPIA OSS COMMUNITY OR
 ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.
</p>
<p>
 This software consists of voluntary contributions made by many
 individuals on behalf of the Sapia Open Source Software Organization.  
 For more information on Sapia OSS, please see
 <a href="http://www.sapia-oss.org/">http://www.sapia-oss.org/</a>
</p>  
  </xsl:template>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
       <xsl:apply-templates select="@*"/>
       <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>  