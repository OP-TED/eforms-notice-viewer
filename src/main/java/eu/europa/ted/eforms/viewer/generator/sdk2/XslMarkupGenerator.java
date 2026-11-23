package eu.europa.ted.eforms.viewer.generator.sdk2;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europa.ted.eforms.sdk.component.SdkComponent;
import eu.europa.ted.eforms.sdk.component.SdkComponentType;
import eu.europa.ted.efx.interfaces.Argument;
import eu.europa.ted.efx.interfaces.Parameter;
import eu.europa.ted.efx.interfaces.TranslatorContext;
import eu.europa.ted.efx.interfaces.TranslatorOptions;
import eu.europa.ted.efx.model.expressions.scalar.NumericExpression;
import eu.europa.ted.efx.model.expressions.scalar.StringExpression;
import eu.europa.ted.efx.model.templates.Conditional;
import eu.europa.ted.efx.model.templates.FragmentKind;
import eu.europa.ted.efx.model.templates.FragmentOutline;
import eu.europa.ted.efx.model.templates.Markup;
import eu.europa.ted.efx.model.templates.OutlineAddress;
import eu.europa.ted.efx.model.templates.OutlineOffset;
import eu.europa.ted.efx.model.types.EfxDataType;

/**
 * Generates the XSLT of an EFX-2 view template.
 *
 * <p>The toolkit gives each line its nesting level, its heading level and the body of its
 * navigation id in its own section or callable template, and each call what it puts above the
 * fragment it invokes: a nesting level offset, a heading level offset and a navigation id prefix. A
 * template is displayed wherever it is invoked, so the stylesheet carries them from call to call:
 * every fragment receives, in the parameters {@link #NAV_ID_PREFIX},
 * {@link #NESTING_LEVEL_OFFSET} and {@link #HEADING_LEVEL_OFFSET}, what the calls on the way put
 * above it, and passes it on in its calls, extended with what each call puts. Where a line is
 * displayed, its nesting level is the nesting levels received plus its own, and its navigation id
 * is the prefixes received, then its own body, joined by {@link OutlineAddress#SEPARATOR}, then the
 * id of the element of the notice it is displayed for, so that each repetition of the heading has
 * its own id; a navigation entry links to it by the same id (see output_file.ftl). The heading
 * levels received are carried on, but the templates do not write out a heading level yet.
 */
@SdkComponent(versions = {"2"}, componentType = SdkComponentType.MARKUP_GENERATOR)
public class XslMarkupGenerator extends eu.europa.ted.eforms.viewer.generator.sdk1.XslMarkupGenerator {
  private static final Logger logger = LoggerFactory.getLogger(XslMarkupGenerator.class);

  /**
   * The parameter that holds the navigation id prefixes that the calls on the way put above a
   * fragment, each followed by {@link OutlineAddress#SEPARATOR}; empty at the top of a section.
   * Names that start with "__" cannot be declared in EFX, so it cannot clash with a parameter of a
   * template.
   */
  static final String NAV_ID_PREFIX = "__navIdPrefix";

  /**
   * The parameter that holds the sum of the nesting level offsets that the calls on the way put
   * above a fragment; 0 at the top of a section.
   */
  static final String NESTING_LEVEL_OFFSET = "__nestingLevelOffset";

  /**
   * The parameter that holds the sum of the heading level offsets that the calls on the way put
   * above a fragment; 0 at the top of a section.
   */
  static final String HEADING_LEVEL_OFFSET = "__headingLevelOffset";

  public XslMarkupGenerator(TranslatorOptions translatorOptions) {
    super(translatorOptions);
  }

  /**
   * Composes a fragment definition that receives what the calls on the way put above it. A line
   * with a navigation id body gets the navigation id of a heading where it is displayed; its nesting
   * level and its heading level where it is displayed are passed to the template ready to be written
   * out. A fragment that is not displayed itself gets no style, so no section is written for it: a
   * line that invokes a template in each of its alternatives, as the templates take its place, and a
   * template that holds lines, as its lines take the place of the invoking line.
   */
  @Override
  public Markup composeFragmentDefinition(String name, FragmentOutline outline,
      List<Conditional> conditionals, Markup content, Markup children, Set<Parameter> parameters,
      TranslatorContext translatorContext) {
    final Set<Parameter> received = new LinkedHashSet<>(parameters);
    received.add(new Parameter.Impl(NAV_ID_PREFIX,
        this.getEfxDataTypeEquivalent(EfxDataType.String.class)));
    received.add(new Parameter.Impl(NESTING_LEVEL_OFFSET,
        this.getEfxDataTypeEquivalent(EfxDataType.Number.class)));
    received.add(new Parameter.Impl(HEADING_LEVEL_OFFSET,
        this.getEfxDataTypeEquivalent(EfxDataType.Number.class)));
    final String navIdBody = outline.getNavIdBody();
    final String id = navIdBody.isEmpty()
        ? ""
        : "{concat($" + NAV_ID_PREFIX + ", '" + navIdBody + "', '-', generate-id())}";
    return this.composeFragmentDefinition(name, outline.getNumber(), id, outline.getKind(),
        nestingLevelWhereDisplayed(outline), headingLevelWhereDisplayed(outline), conditionals,
        content, children, received);
  }

  /**
   * @return the nesting level of the fragment where it is displayed, ready to be written out: the
   *         nesting level offsets received plus its own nesting level
   */
  private static String nestingLevelWhereDisplayed(final FragmentOutline outline) {
    return "$" + NESTING_LEVEL_OFFSET + " + " + outline.getNestingLevel();
  }

  /**
   * @return the heading level of the fragment where it is displayed, ready to be written out: for a
   *         heading, the heading level offsets received plus its own heading level; for any other
   *         fragment 0, wherever it is displayed
   */
  private static String headingLevelWhereDisplayed(final FragmentOutline outline) {
    return outline.getKind() == FragmentKind.HEADING
        ? "$" + HEADING_LEVEL_OFFSET + " + " + outline.getHeadingLevel()
        : "0";
  }

  /**
   * Renders the call of a fragment, which passes on what the calls on the way put above it,
   * extended with what this call puts.
   */
  @Override
  public Markup renderFragmentInvocation(String name, OutlineOffset offset,
      Set<Argument> arguments, TranslatorContext translatorContext) {
    logger.trace("Rendering fragment invocation with: name={}, offset={}", name, offset);
    final String navIdPrefix = offset.getNavIdPrefix();
    final Set<Argument> passed = new LinkedHashSet<>(arguments);
    passed.add(new Argument.Impl(NAV_ID_PREFIX,
        this.getEfxDataTypeEquivalent(EfxDataType.String.class),
        new StringExpression(navIdPrefix.isEmpty()
            ? "$" + NAV_ID_PREFIX
            : "concat($" + NAV_ID_PREFIX + ", '" + navIdPrefix + OutlineAddress.SEPARATOR + "')")));
    passed.add(offset(NESTING_LEVEL_OFFSET, offset.getNestingLevelOffset()));
    passed.add(offset(HEADING_LEVEL_OFFSET, offset.getHeadingLevelOffset()));
    return this.renderCall(name, passed);
  }

  /**
   * @return the argument that passes on the offset received in the given parameter, extended with
   *         what this call puts
   */
  private Argument offset(final String parameter, final int offset) {
    return new Argument.Impl(parameter, this.getEfxDataTypeEquivalent(EfxDataType.Number.class),
        new NumericExpression(offset == 0
            ? "$" + parameter
            : "$" + parameter + " + " + offset));
  }

  @Override
  public Markup composeOutputFile(final List<Markup> globals, final List<Markup> body,
      final List<Markup> summary, final List<Markup> navigation, final List<Markup> fragments) {
    return super.composeOutputFile(withOutlineStart(globals), body, summary, navigation, fragments);
  }

  /**
   * @return the global declarations, preceded by the values that the calls at the top of a section
   *         pass on: an empty navigation id prefix and offsets 0
   */
  protected static List<Markup> withOutlineStart(final List<Markup> globals) {
    final List<Markup> all = new ArrayList<>();
    all.add(new Markup("<xsl:variable name=\"" + NAV_ID_PREFIX + "\" select=\"''\"/>"));
    all.add(new Markup("<xsl:variable name=\"" + NESTING_LEVEL_OFFSET + "\" select=\"0\"/>"));
    all.add(new Markup("<xsl:variable name=\"" + HEADING_LEVEL_OFFSET + "\" select=\"0\"/>"));
    if (globals != null) {
      all.addAll(globals);
    }
    return all;
  }
}
