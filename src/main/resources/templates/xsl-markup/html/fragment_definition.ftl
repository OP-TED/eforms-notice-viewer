<#--
	FreeMarkerTemplate.FRAGMENT_DEFINITION
	Used by MarkupGenerator.composeFragmentDefinition

	Renders a fragment definition as an xsl:template.
	In EFX Template terminology, a fragment is a piece of markup that can be reused.
	Fragments are defined once and can be invoked as many times as needed.
	As a concept, a fragment is the EFX equivalent of an xsl:template in XSLT.

	Parameters:
	- name:         Fragment identifier.
	                The fragment will be invoked with this name when called.
	- parameters:   A list of parameters that will be passed to the fragment when called.
	                Each parameter is an object with a parameter.name and a parameter.type property.
	- number:       Outline number.
	                Displayed if present, with the space after it, in a span of class "number", so that it can be styled
	                apart from the text of the line (or hidden).
	- navIdBody:    The navigation id of the line in its own section or template, as 5_1 or notice-info, or empty for a
	                line without one.
	- conditionals: A list of conditionals, each of which is an object with a conditional.condition and a
	                conditional.content property. If conditional.condition is true, then conditional.content is displayed.
	- content:      The content of this fragment.
	                If the conditionals parameter is not empty, this is the content displayed when no condition is met
	                (under "otherwise"). Otherwise, this is the content to display for this fragment.
	- children:     The children of this fragment: other fragments, rendered nested under this one.
	- style:        How the template line is displayed: "heading" or "normal".
	                Empty for a fragment that is not displayed itself (a line that invokes a template, or a template that
	                holds lines): no section is written for it.
	                A displayed line is a section; its own text, the number and the content, is in a span of class
	                "line-text", before the lines nested in it.
	- nestingLevel: The nesting level of the line in its own section or template: 1 at the top, one more for each line
	                it is nested in.
	- headingLevel: The heading level of the line in its own section or template: 1 for a heading with no heading above
	                it, one more for each heading it is nested in; 0 for a line that is not a heading.

	Every fragment also receives what the calls on the way put above it (see fragment_invocation.ftl, and output_file.ftl
	for their values at the top of a section): __navIdPrefix, the navigation id prefixes, each followed by the separator
	"_"; __nestingLevelOffset and __headingLevelOffset, the offsets. Names that start with "__" cannot be declared in EFX,
	so they cannot clash with a parameter of a template. Where the line is displayed:
	- its id is the prefixes, its navigation id body, "-" and the id of the element of the notice it is displayed for, so
	  that each repetition of a heading has its own id; a navigation entry links to it by the same id (see
	  section_roles.ftl);
	- its section class is "section-" and its navigation id with "-" instead of "_", as section-5-1 for 5.1, or, for a
	  line without a navigation id body, "section-" and its outline number with "-" instead of "."; a style sheet can
	  style a whole section of the notice by it, wherever its lines are displayed;
	- its nesting level and its heading level are the offsets plus its own (a line that is not a heading stays at 0).
-->

<#-- What the line displays: its number, when lines are nested in it, then its content. -->
<#macro lineText>
	<#if number?has_content && children?has_content>
		<span class="number">${number}&#160;</span>
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
</#macro>

<xsl:template name="${name}">
	<#if parameters??>
		<#list parameters as parameter>
			<xsl:param name="${parameter.name}" />
		</#list>
	</#if>
	<xsl:param name="__navIdPrefix" />
	<xsl:param name="__nestingLevelOffset" />
	<xsl:param name="__headingLevelOffset" />
	<#if style?has_content>
		<#if headingLevel gt 0>
			<#assign heading = "{$__headingLevelOffset + ${headingLevel?c}}">
		<#else>
			<#assign heading = "0">
		</#if>
		<#if navIdBody?has_content>
			<#assign sectionClass = " section-{translate(concat($__navIdPrefix, '${navIdBody}'), '_', '-')}">
		<#elseif number?has_content>
			<#assign sectionClass = " section-${number?replace('.', '-')}">
		<#else>
			<#assign sectionClass = "">
		</#if>
		<section title="${name}"
			class="${style} nesting-{$__nestingLevelOffset + ${nestingLevel?c}} heading-${heading}${sectionClass}"
			<#if navIdBody?has_content>id="{concat($__navIdPrefix, '${navIdBody}', '-', generate-id())}"</#if>>
			<span class="line-text">
				<@lineText/>
			</span>
			${children}
		</section>
	<#else>
		<@lineText/>
		${children}
	</#if>
</xsl:template>
