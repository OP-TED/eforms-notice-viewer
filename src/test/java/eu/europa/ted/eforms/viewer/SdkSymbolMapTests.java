package eu.europa.ted.eforms.viewer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class SdkSymbolMapTests {

  private final String testSdkVersion = "latest";

  private SdkSymbolMap getDummyInstance() {
    return SdkSymbolMap.getInstance(testSdkVersion);
  }

  @Test
  void testGetCodelistCodesNonTailored() {
    final SdkSymbolMap symbols = getDummyInstance();
    final String expected =
        "('all-rev-tic', 'cost-comp', 'exc-right', 'other', 'publ-ser-obl', 'soc-stand')";
    final String codelistReference = "contract-detail";
    final String efxList = symbols.expandCodelist(codelistReference); // Has no parent.
    assertEquals(expected, efxList);
  }

  @Test
  void testGetCodelistCodesTailored() {
    final SdkSymbolMap symbols = getDummyInstance();
    final String codelistReference = "eu-official-language";
    final String expected =
        "('BUL', 'CES', 'DAN', 'DEU', 'ELL', 'ENG', 'EST', 'FIN', 'FRA', 'GLE', 'HRV', 'HUN', 'ITA', 'LAV', 'LIT', 'MLT', 'NLD', 'POL', 'POR', 'RON', 'SLK', 'SLV', 'SPA', 'SWE')";
    final String efxList = symbols.expandCodelist(codelistReference);
    assertEquals(expected, efxList);
  }

  @Test
  public void testSymbolsContext() {
    final SdkSymbolMap symbols = getDummyInstance();
    final String fieldId = "BT-01(c)-Procedure";

    final String contextPathOfField = symbols.contextPathOfField(fieldId);
    assertEquals("/*/cac:TenderingTerms", contextPathOfField);

    final String relativePathOfField = symbols.relativeXpathOfField(fieldId, contextPathOfField);
    assertEquals("cac:ProcurementLegislationDocumentReference/cbc:ID[not(text()='CrossBorderLaw')]",
        relativePathOfField);

    // TODO What about cases like this:
    // BT-71-Lot
    // /*/cac:ProcurementProjectLot[cbc:ID/@schemeName='Lot']/cac:TenderingTerms/cac:TendererQualificationRequest[not(cbc:CompanyLegalFormCode)][not(cac:SpecificTendererRequirement/cbc:TendererRequirementTypeCode[@listName='missing-info-submission'])]/cac:SpecificTendererRequirement/cbc:TendererRequirementTypeCode[@listName='reserved-procurement']
  }

  // @Test
  // public void testSymbolsFieldParentNode() {
  // final SdkSymbols symbols = getDummyInstance();
  // symbols.useNewContextualizer(testNewContextualizer);
  // final String fieldId = "BT-01(c)-Procedure";
  // final String parentNodeId = symbols.parentNodeOfField(fieldId);
  // assertEquals("ND-1", parentNodeId);
  // }

  // @Test
  // public void testSymbolsFieldXpath() {
  // final SdkSymbols symbols = getDummyInstance();
  // symbols.useNewContextualizer(testNewContextualizer);
  // final String fieldId = "BT-01(c)-Procedure";
  // final String xpath = symbols.absoluteXpathOfField(fieldId);
  // assertEquals(
  // "/*/cac:TenderingTerms/cac:ProcurementLegislationDocumentReference/cbc:ID[not(text()='CrossBorderLaw')]",
  // xpath);
  // }

  // @Test
  // public void testSymbolsNodeXpath() {
  // final SdkSymbols symbols = getDummyInstance();
  // symbols.useNewContextualizer(testNewContextualizer);
  // final String nodeId = "ND-609";
  // final String xpath = symbols.absoluteXpathOfNode(nodeId);
  // assertEquals("/*/cac:AdditionalDocumentReference", xpath);
  // }

}
