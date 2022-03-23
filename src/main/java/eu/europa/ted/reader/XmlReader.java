package eu.europa.ted.reader;

import java.util.List;
import javax.xml.xpath.XPathExpressionException;

public interface XmlReader {

  /**
   * @return The String that is the result of evaluating the expression and converting the result to
   *         a String.
   */
  public List<String> valuesOf(String xpathExpression) throws XPathExpressionException;
}
