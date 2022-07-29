package eu.europa.ted.eforms.sdk.selector.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdkUnpacker {
  private static final Logger logger = LoggerFactory.getLogger(SdkUnpacker.class);

  private SdkUnpacker() {}

  public static void unpack(File archive, Path targetDir) throws IOException {
    if (archive == null) {
      logger.warn("Undefined archive. Nothing to do!");
      return;
    }

    Validate.isTrue(Files.isRegularFile(archive.toPath()),
        MessageFormat.format("[{0}] is not a file.", archive));

    logger.info("Unpacking file [{}] onto [{}]", archive, targetDir.toAbsolutePath());

    try (ZipFile file = new ZipFile(archive)) {
      Files.createDirectories(targetDir);

      file.stream().filter((ZipEntry entry) -> entry.getName().startsWith("eforms-sdk/"))
          .forEach((ZipEntry entry) -> {
            Path targetEntryPath = Path.of(targetDir.toString(),
                RegExUtils.removeFirst(entry.getName(), "eforms-sdk/"));

            try {
              if (entry.isDirectory()) {
                logger.trace("Creating directory [{}]", targetEntryPath);
                Files.createDirectories(targetEntryPath);
              } else {
                try (InputStream fileInput = file.getInputStream(entry);
                    FileOutputStream fileOutput = new FileOutputStream(targetEntryPath.toFile());) {
                  IOUtils.copy(fileInput, fileOutput);
                }

                logger.trace("Written file [{}]", targetEntryPath);
              }
            } catch (IOException e) {
              throw new RuntimeException(MessageFormat.format(
                  "Failed to extract files from archive [{0}]. Reason was: {1}", archive, e));
            }
          });
    }

    logger.info("Successfully unpacked artifact file [{}] onto [{}]", archive,
        targetDir.toAbsolutePath());
  }
}
