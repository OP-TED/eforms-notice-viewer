package eu.europa.ted.reader;

import java.io.IOException;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

public class XmlReaderFactoryImpl implements XmlReaderFactory {

  @Override
  public XmlReader create(final Path path) {
    try {
      return new XmlReaderDomImpl(path);
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
  }

}
