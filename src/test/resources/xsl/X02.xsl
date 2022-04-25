<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions"
  xmlns:efext="http://data.europa.eu/p27/eforms-ubl-extensions/1"
  xmlns:efac="http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1"
  xmlns:efbc="http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1"
  xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
  xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2"
  xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
  <xsl:output method="html" encoding="UTF-8" indent="yes" />
  <xsl:variable name="labels" select="fn:document('labels.xml')" />
  <xsl:template match="/">
    <html>
      <head>
        <style>
          section { padding: 6px 6px 6px 36px; }
          .text { font-size: 12pt; color: black;}
          .label {
          font-size: 12pt; color: green; }
          .dynamic-label { font-size: 12pt; color: blue; }
          .value {
          font-size: 12pt; color: red; }
        </style>
      </head>
      <body>
        <xsl:for-each select="/*">
          <xsl:call-template name="block01" />
        </xsl:for-each>

        <xsl:for-each select="/*">
          <xsl:call-template name="block02" />
        </xsl:for-each>

        <xsl:for-each select="/*">
          <xsl:call-template name="block03" />
        </xsl:for-each>

        <xsl:for-each select="/*">
          <xsl:call-template name="block04" />
        </xsl:for-each>

      </body>
    </html>
  </xsl:template>
  <xsl:template name='block01'>
    <section title="block01">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|procedure']/text(), ' Label not found (decoration|name|procedure)')[1]" />
      </span>
      <xsl:for-each select="/*">
        <xsl:call-template name="block0101" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block0101'>
    <section title="block0101">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|procedure']/text(), ' Label not found (decoration|name|procedure)')[1]" />
      </span>
      <xsl:for-each select="/*">
        <xsl:call-template name="block010101" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block010101'>
    <section title="block010101">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|general-information']/text(), ' Label not found (decoration|name|general-information)')[1]" />
      </span>
      <xsl:for-each select="/*">
        <xsl:call-template name="block01010101" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:TenderingTerms/cac:ProcurementLegislationDocumentReference/cbc:ID[not(text()='CrossBorderLaw')]">
        <xsl:call-template name="block01010102" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:TenderingTerms/cac:ProcurementLegislationDocumentReference[not(cbc:ID/text()='CrossBorderLaw')]/cbc:DocumentDescription">
        <xsl:call-template name="block01010103" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block01010101'>
    <section title="block01010101">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|legal-basis']/text(), ' Label not found (decoration|name|legal-basis)')[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block01010102'>
    <section title="block01010102">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='field|name|BT-01(c)-Procedure']/text(), ' Label not found (field|name|BT-01(c)-Procedure)')[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block01010103'>
    <section title="block01010103">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-01']/text(), ' Label not found (business_term|name|BT-01)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02'>
    <section title="block02">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|organisations']/text(), ' Label not found (decoration|name|organisations)')[1]" />
      </span>
      <xsl:for-each select="/*">
        <xsl:call-template name="block0201" />
      </xsl:for-each>

      <xsl:for-each select="/*">
        <xsl:call-template name="block0202" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block0201'>
    <section title="block0201">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|organisations']/text(), ' Label not found (decoration|name|organisations)')[1]" />
      </span>
      <xsl:for-each select="/*">
        <xsl:call-template name="block020101" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block020101'>
    <section title="block020101">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-500']/text(), ' Label not found (business_term|name|BT-500)')[1]" />
      </span>
      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity[not(cbc:CompanyID/@schemeName = 'EU')]/cbc:RegistrationName">
        <xsl:call-template name="block02010101" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity/cbc:CompanyID[@schemeName = 'EU']">
        <xsl:call-template name="block02010102" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity/cbc:CompanyID[not(@schemeName = 'EU')]">
        <xsl:call-template name="block02010103" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:PostalAddress/cbc:StreetName">
        <xsl:call-template name="block02010104" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:PostalAddress/cbc:CityName">
        <xsl:call-template name="block02010105" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:PostalAddress/cbc:PostalZone">
        <xsl:call-template name="block02010106" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PostalAddress/cbc:CountrySubentityCode">
        <xsl:call-template name="block02010107" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PostalAddress/cac:Country/cbc:IdentificationCode">
        <xsl:call-template name="block02010108" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:Name">
        <xsl:call-template name="block02010109" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:ElectronicMail">
        <xsl:call-template name="block02010110" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:Telephone">
        <xsl:call-template name="block02010111" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:Telefax">
        <xsl:call-template name="block02010112" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cbc:WebsiteURI">
        <xsl:call-template name="block02010113" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block02010101'>
    <section title="block02010101">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-500']/text(), ' Label not found (business_term|name|BT-500)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010102'>
    <section title="block02010102">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-501']/text(), ' Label not found (business_term|name|BT-501)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010103'>
    <section title="block02010103">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-501']/text(), ' Label not found (business_term|name|BT-501)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010104'>
    <section title="block02010104">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-510']/text(), ' Label not found (business_term|name|BT-510)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
      <span class="text">
        <xsl:text> </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="../cbc:AdditionalStreetName/normalize-space(text())" />
      </span>
      <span class="text">
        <xsl:text> </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="../cac:AddressLine/cbc:Line/normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010105'>
    <section title="block02010105">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-513']/text(), ' Label not found (business_term|name|BT-513)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010106'>
    <section title="block02010106">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-512']/text(), ' Label not found (business_term|name|BT-512)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010107'>
    <section title="block02010107">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-507']/text(), ' Label not found (business_term|name|BT-507)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label1" select="concat('code|value|nuts.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label1]/text(), concat('Label not found (', $label1, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010108'>
    <section title="block02010108">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-514']/text(), ' Label not found (business_term|name|BT-514)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label2" select="concat('code|value|country.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label2]/text(), concat('Label not found (', $label2, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010109'>
    <section title="block02010109">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-502']/text(), ' Label not found (business_term|name|BT-502)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010110'>
    <section title="block02010110">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-506']/text(), ' Label not found (business_term|name|BT-506)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010111'>
    <section title="block02010111">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-503']/text(), ' Label not found (business_term|name|BT-503)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010112'>
    <section title="block02010112">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-739']/text(), ' Label not found (business_term|name|BT-739)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block02010113'>
    <section title="block02010113">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-505']/text(), ' Label not found (business_term|name|BT-505)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block0202'>
    <section title="block0202">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|organisation-roles']/text(), ' Label not found (decoration|name|organisation-roles)')[1]" />
      </span>
      <xsl:for-each
        select="/*/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:Organizations/efac:Organization/efac:Company/cac:PartyName/cbc:Name[cac:PartyIdentification/cbc:ID/normalize-space(text())=../../../../../../../cac:ContractingParty/cac:Party/cac:PartyIdentification/cbc:ID/normalize-space(text())]">
        <xsl:call-template name="block020201" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block020201'>
    <section title="block020201">
      <span class="text">
        <xsl:text>TODO fix something in that row</xsl:text>
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block03'>
    <section title="block03">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|object']/text(), ' Label not found (decoration|name|object)')[1]" />
      </span>
      <xsl:for-each select="/*">
        <xsl:call-template name="block0301" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity[not(cbc:CompanyID/@schemeName = 'EU')]/cbc:RegistrationName">
        <xsl:call-template name="block0302" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessCapability/cbc:CapabilityTypeCode">
        <xsl:call-template name="block0303" />
      </xsl:for-each>

      <xsl:for-each select="/*">
        <xsl:call-template name="block0304" />
      </xsl:for-each>

      <xsl:for-each select="/*">
        <xsl:call-template name="block0305" />
      </xsl:for-each>

      <xsl:for-each select="/*/cbc:Note">
        <xsl:call-template name="block0306" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block0301'>
    <section title="block0301">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|legal-basis']/text(), ' Label not found (decoration|name|legal-basis)')[1]" />
      </span>
      <xsl:for-each select="/*/cbc:RegulatoryDomain">
        <xsl:call-template name="block030101" />
      </xsl:for-each>

      <xsl:for-each select="/*/efac:NoticePurpose/cbc:PurposeCode">
        <xsl:call-template name="block030102" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block030101'>
    <section title="block030101">
      <xsl:variable name="label3" select="concat('code|value|legal-basis.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label3]/text(), concat('Label not found (', $label3, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030102'>
    <section title="block030102">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-100']/text(), ' Label not found (business_term|name|OPP-100)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label4" select="concat('code|value|notice-purpose.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label4]/text(), concat('Label not found (', $label4, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block0302'>
    <section title="block0302">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-500-company']/text(), ' Label not found (business_term|name|BT-500-company)')[1]" />
      </span>
      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity[not(cbc:CompanyID/@schemeName = 'EU')]/cbc:RegistrationName">
        <xsl:call-template name="block030201" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity/cbc:CompanyID[not(@schemeName = 'EU')]">
        <xsl:call-template name="block030202" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:PostalAddress/cbc:StreetName">
        <xsl:call-template name="block030203" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:PostalAddress/cbc:CityName">
        <xsl:call-template name="block030204" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:PostalAddress/cbc:PostalZone">
        <xsl:call-template name="block030205" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PostalAddress/cbc:CountrySubentityCode">
        <xsl:call-template name="block030206" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PostalAddress/cac:Country/cbc:IdentificationCode">
        <xsl:call-template name="block030207" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:Name">
        <xsl:call-template name="block030208" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:ElectronicMail">
        <xsl:call-template name="block030209" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:Telephone">
        <xsl:call-template name="block030210" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cac:Contact/cbc:Telefax">
        <xsl:call-template name="block030211" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:BusinessParty/cbc:WebsiteURI">
        <xsl:call-template name="block030212" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block030201'>
    <section title="block030201">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-500']/text(), ' Label not found (business_term|name|BT-500)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030202'>
    <section title="block030202">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-501']/text(), ' Label not found (business_term|name|BT-501)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030203'>
    <section title="block030203">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-510']/text(), ' Label not found (business_term|name|BT-510)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
      <span class="text">
        <xsl:text> </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="../cbc:AdditionalStreetName/normalize-space(text())" />
      </span>
      <span class="text">
        <xsl:text> </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="../cac:AddressLine/cbc:Line/normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030204'>
    <section title="block030204">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-513']/text(), ' Label not found (business_term|name|BT-513)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030205'>
    <section title="block030205">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-512']/text(), ' Label not found (business_term|name|BT-512)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030206'>
    <section title="block030206">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-507']/text(), ' Label not found (business_term|name|BT-507)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label5" select="concat('code|value|nuts.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label5]/text(), concat('Label not found (', $label5, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030207'>
    <section title="block030207">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-514']/text(), ' Label not found (business_term|name|BT-514)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label6" select="concat('code|value|country.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label6]/text(), concat('Label not found (', $label6, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030208'>
    <section title="block030208">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-502']/text(), ' Label not found (business_term|name|BT-502)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030209'>
    <section title="block030209">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-506']/text(), ' Label not found (business_term|name|BT-506)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030210'>
    <section title="block030210">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-503']/text(), ' Label not found (business_term|name|BT-503)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030211'>
    <section title="block030211">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-739']/text(), ' Label not found (business_term|name|BT-739)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030212'>
    <section title="block030212">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-505']/text(), ' Label not found (business_term|name|BT-505)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block0303'>
    <section title="block0303">
      <xsl:variable name="label7" select="concat('code|value|main-activity.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label7]/text(), concat('Label not found (', $label7, ')'))[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label8" select="concat('code|value|main-activity.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label8]/text(), concat('Label not found (', $label8, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block0304'>
    <section title="block0304">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|place-registration']/text(), ' Label not found (decoration|name|place-registration)')[1]" />
      </span>
      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cac:CorporateRegistrationScheme/cac:JurisdictionRegionAddress/cbc:CityName">
        <xsl:call-template name="block030401" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cac:CorporateRegistrationScheme/cac:JurisdictionRegionAddress/cbc:PostalZone">
        <xsl:call-template name="block030402" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cac:CorporateRegistrationScheme/cac:JurisdictionRegionAddress/cac:Country/cbc:IdentificationCode">
        <xsl:call-template name="block030403" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity/cbc:CompanyID[@schemeName = 'EU']">
        <xsl:call-template name="block030404" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cbc:RegistrationDate">
        <xsl:call-template name="block030405" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block030401'>
    <section title="block030401">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-110']/text(), ' Label not found (business_term|name|OPP-110)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030402'>
    <section title="block030402">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-111']/text(), ' Label not found (business_term|name|OPP-111)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030403'>
    <section title="block030403">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-112']/text(), ' Label not found (business_term|name|OPP-112)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030404'>
    <section title="block030404">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-501']/text(), ' Label not found (business_term|name|BT-501)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030405'>
    <section title="block030405">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-113']/text(), ' Label not found (business_term|name|OPP-113)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block0305'>
    <section title="block0305">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|publication-gazette']/text(), ' Label not found (decoration|name|publication-gazette)')[1]" />
      </span>
      <xsl:for-each select="/*/cac:AdditionalDocumentReference/cbc:DocumentDescription">
        <xsl:call-template name="block030501" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:AdditionalDocumentReference/cbc:ReferencedDocumentInternalAddress">
        <xsl:call-template name="block030502" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/cac:AdditionalDocumentReference/cac:Attachment/cac:ExternalReference/cbc:URI">
        <xsl:call-template name="block030503" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:AdditionalDocumentReference/cbc:IssueDate">
        <xsl:call-template name="block030504" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block030501'>
    <section title="block030501">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-120']/text(), ' Label not found (business_term|name|OPP-120)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030502'>
    <section title="block030502">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-121']/text(), ' Label not found (business_term|name|OPP-121)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030503'>
    <section title="block030503">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-122']/text(), ' Label not found (business_term|name|OPP-122)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block030504'>
    <section title="block030504">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-123']/text(), ' Label not found (business_term|name|OPP-123)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block0306'>
    <section title="block0306">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-130']/text(), ' Label not found (business_term|name|OPP-130)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block04'>
    <section title="block04">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|notice-information']/text(), ' Label not found (decoration|name|notice-information)')[1]" />
      </span>
      <xsl:for-each select="/*">
        <xsl:call-template name="block0401" />
      </xsl:for-each>

      <xsl:for-each select="/*">
        <xsl:call-template name="block0402" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block0401'>
    <section title="block0401">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|notice-information']/text(), ' Label not found (decoration|name|notice-information)')[1]" />
      </span>
      <xsl:for-each select="/*/cbc:ID[@schemeName='notice-id']">
        <xsl:call-template name="block040101" />
      </xsl:for-each>

      <xsl:for-each select="/*/cbc:NoticeTypeCode/@listName">
        <xsl:call-template name="block040102" />
      </xsl:for-each>

      <xsl:for-each select="/*/cbc:NoticeTypeCode">
        <xsl:call-template name="block040103" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:NoticeSubType/cbc:SubTypeCode">
        <xsl:call-template name="block040104" />
      </xsl:for-each>

      <xsl:for-each select="/*/cac:SenderParty/cac:Contact/cbc:ElectronicMail">
        <xsl:call-template name="block040105" />
      </xsl:for-each>

      <xsl:for-each select="/*/cbc:IssueDate">
        <xsl:call-template name="block040106" />
      </xsl:for-each>

      <xsl:for-each select="/*/cbc:NoticeLanguageCode">
        <xsl:call-template name="block040107" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block040101'>
    <section title="block040101">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-757']/text(), ' Label not found (business_term|name|BT-757)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="../cbc:VersionID/normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040102'>
    <section title="block040102">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-03']/text(), ' Label not found (business_term|name|BT-03)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label9" select="concat('code|value|form-type.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label9]/text(), concat('Label not found (', $label9, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040103'>
    <section title="block040103">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-02']/text(), ' Label not found (business_term|name|BT-02)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <xsl:variable name="label10" select="concat('code|value|notice-type.', .)" />
      <span class="dynamic-label">
        <xsl:value-of
          select="($labels/properties/entry[./@key=$label10]/text(), concat('Label not found (', $label10, ')'))[1]" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040104'>
    <section title="block040104">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-070']/text(), ' Label not found (business_term|name|OPP-070)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040105'>
    <section title="block040105">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-131']/text(), ' Label not found (business_term|name|OPP-131)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040106'>
    <section title="block040106">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-05']/text(), ' Label not found (business_term|name|BT-05)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
      <span class="text">
        <xsl:text> </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="../cbc:IssueTime/normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040107'>
    <section title="block040107">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|BT-702']/text(), ' Label not found (business_term|name|BT-702)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
      <span class="text">
        <xsl:text> </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of
          select="../cac:AdditionalNoticeLanguage/cbc:ID/normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block0402'>
    <section title="block0402">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='decoration|name|publication-information']/text(), ' Label not found (decoration|name|publication-information)')[1]" />
      </span>
      <xsl:for-each
        select="/*/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:Publication/efbc:NoticePublicationID[@schemeName='ojs-notice-id']">
        <xsl:call-template name="block040201" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:Publication/efbc:GazetteID[@schemeName='ojs-id']">
        <xsl:call-template name="block040202" />
      </xsl:for-each>

      <xsl:for-each
        select="/*/ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:Publication/efbc:PublicationDate">
        <xsl:call-template name="block040203" />
      </xsl:for-each>

    </section>
  </xsl:template>

  <xsl:template name='block040201'>
    <section title="block040201">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-010']/text(), ' Label not found (business_term|name|OPP-010)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040202'>
    <section title="block040202">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-011']/text(), ' Label not found (business_term|name|OPP-011)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

  <xsl:template name='block040203'>
    <section title="block040203">
      <span class="label">
        <xsl:value-of
          select="($labels/properties/entry[./@key='business_term|name|OPP-012']/text(), ' Label not found (business_term|name|OPP-012)')[1]" />
      </span>
      <span class="text">
        <xsl:text>: </xsl:text>
      </span>
      <span class="value">
        <xsl:value-of select="./normalize-space(text())" />
      </span>
    </section>
  </xsl:template>

</xsl:stylesheet>