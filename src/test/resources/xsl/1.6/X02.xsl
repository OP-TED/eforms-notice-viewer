<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2" xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2" xmlns:efext="http://data.europa.eu/p27/eforms-ubl-extensions/1" xmlns:efac="http://data.europa.eu/p27/eforms-ubl-extension-aggregate-components/1" xmlns:efbc="http://data.europa.eu/p27/eforms-ubl-extension-basic-components/1" xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2" version="2.0">  
    <xsl:output method="html" encoding="UTF-8" indent="yes"/>  
    <xsl:param name="language"/>  
    <xsl:variable name="labels" select="(fn:document(concat('business-term_' , $language, '.xml')), fn:document(concat('field_' , $language, '.xml')), fn:document(concat('code_' , $language, '.xml')), fn:document(concat('auxiliary_' , $language, '.xml')))"/>  
    <xsl:template match="/"> 
        <html> 
            <head> 
                <style>section { padding: 6px 6px 6px 36px; } .text { font-size: 12pt; color: black; } .label { font-size: 12pt; color: green; } .dynamic-label { font-size: 12pt; color: blue; } .value { font-size: 12pt; color: red; }</style> 
            </head>  
            <body> 
                <xsl:for-each select="/*"> 
                    <xsl:call-template name="block01"/> 
                </xsl:for-each>  
                <xsl:for-each select="/*"> 
                    <xsl:call-template name="block02"/> 
                </xsl:for-each> 
            </body> 
        </html> 
    </xsl:template>  
    <xsl:template name="block01"> 
        <section title="block01"> 
            <xsl:text>9 </xsl:text>  
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'object')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'object'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="."> 
                <xsl:call-template name="block0101"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:BusinessParty/cac:PartyLegalEntity[not(cbc:CompanyID/@schemeName = 'EU')]/cbc:RegistrationName"> 
                <xsl:call-template name="block0102"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:BusinessCapability/cbc:CapabilityTypeCode"> 
                <xsl:call-template name="block0103"/> 
            </xsl:for-each>  
            <xsl:for-each select="."> 
                <xsl:call-template name="block0104"/> 
            </xsl:for-each>  
            <xsl:for-each select="."> 
                <xsl:call-template name="block0105"/> 
            </xsl:for-each>  
            <xsl:for-each select="cbc:Note"> 
                <xsl:call-template name="block0106"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0101"> 
        <section title="block0101"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'legal-basis')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'legal-basis'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="cbc:RegulatoryDomain"> 
                <xsl:call-template name="block010101"/> 
            </xsl:for-each>  
            <xsl:for-each select="efac:NoticePurpose/cbc:PurposeCode"> 
                <xsl:call-template name="block010102"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010101"> 
        <section title="block010101"> 
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in . return concat('code', '|', 'name', '|', 'legal-basis', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010102"> 
        <section title="block010102"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-100')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-100'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in . return concat('code', '|', 'name', '|', 'notice-purpose', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0102"> 
        <section title="block0102"> 
            <xsl:text>9.1 </xsl:text>  
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'company')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'company'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="."> 
                <xsl:call-template name="block010201"/> 
            </xsl:for-each>  
            <xsl:for-each select="../cbc:CompanyID[not(@schemeName = 'EU')]"> 
                <xsl:call-template name="block010202"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:PostalAddress/cbc:StreetName"> 
                <xsl:call-template name="block010203"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:PostalAddress/cbc:CityName"> 
                <xsl:call-template name="block010204"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:PostalAddress/cbc:PostalZone"> 
                <xsl:call-template name="block010205"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:PostalAddress/cbc:CountrySubentityCode"> 
                <xsl:call-template name="block010206"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:PostalAddress/cac:Country/cbc:IdentificationCode"> 
                <xsl:call-template name="block010207"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:Contact/cbc:Name"> 
                <xsl:call-template name="block010208"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:Contact/cbc:ElectronicMail"> 
                <xsl:call-template name="block010209"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:Contact/cbc:Telephone"> 
                <xsl:call-template name="block010210"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cac:Contact/cbc:Telefax"> 
                <xsl:call-template name="block010211"/> 
            </xsl:for-each>  
            <xsl:for-each select="../../cbc:WebsiteURI"> 
                <xsl:call-template name="block010212"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010201"> 
        <section title="block010201"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-500')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-500'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010202"> 
        <section title="block010202"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-501')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-501'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010203"> 
        <section title="block010203"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-510')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-510'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span>
            <span class="text">
                <xsl:text> </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="../cbc:AdditionalStreetName/normalize-space(text())"/>
            </span>
            <span class="text">
                <xsl:text> </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="../cac:AddressLine/cbc:Line/normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010204"> 
        <section title="block010204"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-513')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-513'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010205"> 
        <section title="block010205"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-512')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-512'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010206"> 
        <section title="block010206"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-507')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-507'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in . return concat('code', '|', 'name', '|', 'nuts', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010207"> 
        <section title="block010207"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-514')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-514'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in . return concat('code', '|', 'name', '|', 'country', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010208"> 
        <section title="block010208"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-502')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-502'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010209"> 
        <section title="block010209"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-506')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-506'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010210"> 
        <section title="block010210"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-503')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-503'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010211"> 
        <section title="block010211"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-739')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-739'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010212"> 
        <section title="block010212"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-505')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-505'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0103"> 
        <section title="block0103"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-105-Business')]/text(), concat('{', concat('field', '|', 'name', '|', 'OPP-105-Business'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in . return concat('code', '|', 'name', '|', 'main-activity', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0104"> 
        <section title="block0104"> 
            <xsl:text>9.3 </xsl:text>  
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'place-registration')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'place-registration'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cac:CorporateRegistrationScheme/cac:JurisdictionRegionAddress/cbc:CityName"> 
                <xsl:call-template name="block010401"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cac:CorporateRegistrationScheme/cac:JurisdictionRegionAddress/cbc:PostalZone"> 
                <xsl:call-template name="block010402"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cac:CorporateRegistrationScheme/cac:JurisdictionRegionAddress/cac:Country/cbc:IdentificationCode"> 
                <xsl:call-template name="block010403"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cbc:CompanyID[@schemeName = 'EU']"> 
                <xsl:call-template name="block010404"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:BusinessParty/cac:PartyLegalEntity[cbc:CompanyID/@schemeName = 'EU']/cbc:RegistrationDate"> 
                <xsl:call-template name="block010405"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010401"> 
        <section title="block010401"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-110')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-110'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010402"> 
        <section title="block010402"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-111')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-111'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010403"> 
        <section title="block010403"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-112')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-112'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010404"> 
        <section title="block010404"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-501')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-501'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010405"> 
        <section title="block010405"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-113')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-113'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./xs:date(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0105"> 
        <section title="block0105"> 
            <xsl:text>9.4 </xsl:text>  
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'publication-gazette')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'publication-gazette'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="cac:AdditionalDocumentReference/cbc:DocumentDescription"> 
                <xsl:call-template name="block010501"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:AdditionalDocumentReference/cbc:ReferencedDocumentInternalAddress"> 
                <xsl:call-template name="block010502"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:AdditionalDocumentReference/cac:Attachment/cac:ExternalReference/cbc:URI"> 
                <xsl:call-template name="block010503"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:AdditionalDocumentReference/cbc:IssueDate"> 
                <xsl:call-template name="block010504"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010501"> 
        <section title="block010501"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-120')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-120'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010502"> 
        <section title="block010502"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-121')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-121'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010503"> 
        <section title="block010503"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-122')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-122'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block010504"> 
        <section title="block010504"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-123')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-123'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./xs:date(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0106"> 
        <section title="block0106"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-130')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-130'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block02"> 
        <section title="block02"> 
            <xsl:text>11 </xsl:text>  
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'notice-information')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'notice-information'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="."> 
                <xsl:call-template name="block0201"/> 
            </xsl:for-each>  
            <xsl:for-each select="."> 
                <xsl:call-template name="block0202"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0201"> 
        <section title="block0201"> 
            <xsl:text>11.1 </xsl:text>  
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'notice-information')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'notice-information'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="cbc:ID[@schemeName='notice-id']"> 
                <xsl:call-template name="block020101"/> 
            </xsl:for-each>  
            <xsl:for-each select="cbc:NoticeTypeCode/@listName"> 
                <xsl:call-template name="block020102"/> 
            </xsl:for-each>  
            <xsl:for-each select="cbc:NoticeTypeCode"> 
                <xsl:call-template name="block020103"/> 
            </xsl:for-each>  
            <xsl:for-each select="ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:NoticeSubType/cbc:SubTypeCode"> 
                <xsl:call-template name="block020104"/> 
            </xsl:for-each>  
            <xsl:for-each select="cac:SenderParty/cac:Contact/cbc:ElectronicMail"> 
                <xsl:call-template name="block020105"/> 
            </xsl:for-each>  
            <xsl:for-each select="cbc:IssueDate"> 
                <xsl:call-template name="block020106"/> 
            </xsl:for-each>  
            <xsl:for-each select="cbc:NoticeLanguageCode"> 
                <xsl:call-template name="block020107"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020101"> 
        <section title="block020101"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-701')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-701'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>:  </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span>
            <span class="text">
                <xsl:text> </xsl:text>
            </span>
            <span class="text">
                <xsl:text>- </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="../cbc:VersionID/normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020102"> 
        <section title="block020102"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-03')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-03'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in ../@listName return concat('code', '|', 'name', '|', 'form-type', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020103"> 
        <section title="block020103"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-02')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-02'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in . return concat('code', '|', 'name', '|', 'notice-type', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020104"> 
        <section title="block020104"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-070')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-070'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020105"> 
        <section title="block020105"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-131')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-131'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020106"> 
        <section title="block020106"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-05')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-05'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./xs:date(text())"/>
            </span>
            <span class="text">
                <xsl:text> </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="../cbc:IssueTime/xs:time(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020107"> 
        <section title="block020107"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'BT-702')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'BT-702'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in . return concat('code', '|', 'name', '|', 'language', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span>
            <span class="text">
                <xsl:text> </xsl:text>
            </span>
            <span class="dynamic-label"> 
                <xsl:variable name="labels" as="xs:string*"> 
                    <xsl:for-each select="for $item in ../cac:AdditionalNoticeLanguage/cbc:ID return concat('code', '|', 'name', '|', 'language', '.', $item)"> 
                        <xsl:variable name="label" select="."/>  
                        <xsl:value-of select="($label, concat('{', $label, '}'))[1]"/> 
                    </xsl:for-each> 
                </xsl:variable>  
                <xsl:value-of select="string-join($label, ', ')"/> 
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block0202"> 
        <section title="block0202"> 
            <xsl:text>11.2 </xsl:text>  
            <span class="label">
                <xsl:value-of select="($label, '|', 'text', '|', 'publication-information')]/text(), concat('{', concat('auxiliary', '|', 'text', '|', 'publication-information'), '}'))[1]"/>
            </span>  
            <xsl:for-each select="ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:Publication/efbc:NoticePublicationID[@schemeName='ojs-notice-id']"> 
                <xsl:call-template name="block020201"/> 
            </xsl:for-each>  
            <xsl:for-each select="ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:Publication/efbc:GazetteID[@schemeName='ojs-id']"> 
                <xsl:call-template name="block020202"/> 
            </xsl:for-each>  
            <xsl:for-each select="ext:UBLExtensions/ext:UBLExtension/ext:ExtensionContent/efext:EformsExtension/efac:Publication/efbc:PublicationDate"> 
                <xsl:call-template name="block020203"/> 
            </xsl:for-each> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020201"> 
        <section title="block020201"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-010')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-010'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020202"> 
        <section title="block020202"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-011')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-011'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./normalize-space(text())"/>
            </span> 
        </section> 
    </xsl:template>  
    <xsl:template name="block020203"> 
        <section title="block020203"> 
            <span class="label">
                <xsl:value-of select="($label, '|', 'name', '|', 'OPP-012')]/text(), concat('{', concat('business-term', '|', 'name', '|', 'OPP-012'), '}'))[1]"/>
            </span>
            <span class="text">
                <xsl:text>: </xsl:text>
            </span>
            <span class="value">
                <xsl:value-of select="./xs:date(text())"/>
            </span> 
        </section> 
    </xsl:template> 
</xsl:stylesheet>