<#--
	FreeMarkerTemplate.FRAGMENT_DEFINITION
	Used by MarkupGenerator.renderFragmentDefinition

	Renders a fragment definition as an xsl:template.
	In EFX Template terminology, a fragment is a piece of markup that can be reused.
	Fragments are defined once and can be invoked as many times as needed.
	As a concept, a fragments is the EFX equivalent of an xsl:template in XSLT. 

    Parameters:
    - name: 		Fragment identifier. 
					The fragment will be invoked with this name when called.
	- parameters: 	A list of parameters that will be passed to the fragment when called.
					Each parameter is an object with a parameter.name and a parameter.type property.
    - number: 		Outline number. 
					Display if present.
	- id: 			The id of the section, ready to be written out, or empty for a section without one.
	- conditionals: A list of conditionals, each of which is an object with a conditional.condition and a conditional.content property.
					If conditional.condition is true then conditional.content should be displayed. 
    - content: 		The content of this fragment.
					If the conditionals parameter is not empty, then this is the content to be used when no condition is et (under "otherwise").
					If no conditionals are passed, then this is the content to display for this fragment.
	- children: 	The children of this fragment  are other fragments that need to be rendered nested under this one.
	- style: 		How the template line is displayed: "heading" or "normal".
					Absent for a fragment that is not displayed itself (a line that invokes a template, or a template that holds lines): no section is written for it.
	- nestingLevel: 	The nesting level of the line where it is displayed: 1 at the top of its section, one more for each line it is nested in.
	- headingLevel: 	The heading level of the line where it is displayed: 1 for a heading with no heading above it, one more for each heading it is nested in. 0 for a line that is not a heading.
-->

<xsl:template name="${name}">
	<#if parameters??>
		<#list parameters as parameter>
			<xsl:param name="${parameter.name}" />
		</#list>
	</#if>
	<#if style?has_content><section title="${name}" class="${style} nesting-{${nestingLevel}} heading-{${headingLevel}}" <#if id?has_content>id="${id}"</#if>></#if>
		<#if number?has_content && children?has_content>
		  <xsl:text>${number}&#160;</xsl:text>
		</#if>
        <#if conditionals?? && conditionals?size gt 0>
            <xsl:choose>
                <#list conditionals as item>
                    <xsl:when test="${item.condition}">
                        ${item.content}
                    </xsl:when>
                </#list>
                <xsl:otherwise>
                    ${content}
                </xsl:otherwise>
            </xsl:choose>
		<#else>
			${content}
        </#if>
		<#if children?has_content>
			${children}
		</#if>
	<#if style?has_content></section></#if>
</xsl:template>