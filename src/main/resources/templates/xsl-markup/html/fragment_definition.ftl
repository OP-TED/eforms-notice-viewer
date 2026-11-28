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
	- conditionals: A list of conditionals, each of which is an object with a conditional.condition, a
	                conditional.caption and a conditional.content property. If conditional.condition is true, then
	                conditional.caption and conditional.content are displayed.
	- caption:      The caption of the content: what the template of the line displays before the colon that separates
	                it from the content, or empty when the template has no caption (always for a heading, and in EFX-1).
	                The colon itself is not passed: the viewer writes its own separator.
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
	- its nesting level is the offset plus its own, in the class nesting-N;
	- a heading's heading level is the offset plus its own, in the class heading-N; a line that is not a heading has no
	  heading level, so it gets no such class.
-->

<#-- What the line displays: its number, when lines are nested in it, then its caption and its content. -->
<#macro lineText>
	<#if number?has_content && children?has_content>
		<span class="number">${number}&#160;</span>
	</#if>
	<#if conditionals?? && conditionals?size gt 0>
		<xsl:choose>
			<#list conditionals as item>
				<xsl:when test="${item.condition}">
					<@captionedContent caption=item.caption content=item.content/>
				</xsl:when>
			</#list>
			<xsl:otherwise>
				<@captionedContent caption=caption content=content/>
			</xsl:otherwise>
		</xsl:choose>
	<#else>
		<@captionedContent caption=caption content=content/>
	</#if>
</#macro>

<#-- A content with its caption, when it has one: the caption, the viewer's own separator and the content, each in a
     span of its own, together in a span of class "captioned", so that a style sheet can show them side by side or
     one below the other. Like the number, the separator ends with a no-break space. A content without a caption is
     written as it is. -->
<#macro captionedContent caption content>
	<#if caption?has_content>
		<span class="captioned">
			<span class="caption">${caption}</span>
			<span class="separator">:&#160;</span>
			<span class="content">${content}</span>
		</span>
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
			<#assign headingClass = " heading-{$__headingLevelOffset + ${headingLevel?c}}">
		<#else>
			<#assign headingClass = "">
		</#if>
		<#if navIdBody?has_content>
			<#assign sectionClass = " section-{translate(concat($__navIdPrefix, '${navIdBody}'), '_', '-')}">
		<#elseif number?has_content>
			<#assign sectionClass = " section-${number?replace('.', '-')}">
		<#else>
			<#assign sectionClass = "">
		</#if>
		<section title="${name}"
			class="${style} nesting-{$__nestingLevelOffset + ${nestingLevel?c}}${headingClass}${sectionClass}"
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
