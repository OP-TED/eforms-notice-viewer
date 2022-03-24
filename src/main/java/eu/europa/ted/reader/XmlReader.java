package eu.europa.ted.reader;

import java.util.List;
import javax.xml.xpath.XPathExpressionException;

public interface XmlReader {

  public List<String> valuesOf(String xpathExpression) throws XPathExpressionException;

  public List<String> valuesOf(String relativeXpath, String contextXpath)
      throws XPathExpressionException;
}
