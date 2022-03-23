package eu.europa.ted.reader;

import java.nio.file.Path;

public interface XmlReaderFactory {
  public XmlReader create(final Path path);
}
