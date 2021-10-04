<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="PHONE_RECORDS">
  <html><head><title>Phone Listing</title></head>
  <body><h1>Phone Call Records</h1>
  <table border="1">
    <th>Item</th>
    <th>Source Number</th>
    <th>Destination Number</th>
    <th>Date (MM/DD/YY)</th>

  <xsl:apply-templates/>

  </table>
  </body></html>
</xsl:template>

<xsl:template match="CALL">
  <tr>
  <td><xsl:number/></td>
  <td><xsl:value-of select="FROM"/></td>
  <td><xsl:value-of select="DESTINATION"/></td>
  <td><xsl:value-of select="DATE"/></td>
  </tr>
</xsl:template>

</xsl:stylesheet>
