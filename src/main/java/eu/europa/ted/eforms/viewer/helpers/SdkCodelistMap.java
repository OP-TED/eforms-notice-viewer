package eu.europa.ted.eforms.viewer.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.helger.genericode.Genericode10CodeListMarshaller;
import com.helger.genericode.v10.CodeListDocument;
import com.helger.genericode.v10.Identification;
import com.helger.genericode.v10.SimpleCodeList;

import eu.europa.ted.efx.model.SdkCodelist;

public class SdkCodelistMap extends HashMap<String, SdkCodelist> {
  private static final long serialVersionUID = 1L;

  /**
   * Currently unused, this will be supported in future versions.
   */
  private final String sdkVersion;

  public SdkCodelistMap(final String sdkVersion) {
    this.sdkVersion = sdkVersion;
  }

  /**
   * Builds EFX list from the passed codelist reference. This will lazily
   * compute and cache the result for reuse as the operation can be costly on
   * some large lists.
   *
   * @param codelistId
   *          A reference to an SDK codelist.
   * @return The EFX string representation of the list of all the codes of the
   *         referenced codelist.
   */
  public final SdkCodelist get(final String codelistId) {
    if (StringUtils.isBlank(codelistId)) {
      throw new RuntimeException("CodelistId is blank.");
    }
    return this.computeIfAbsent(codelistId, key -> loadSdkCodelist(key, sdkVersion));
  }

  @Override
  public SdkCodelist get(final Object codelistId) {
    return this.computeIfAbsent((String) codelistId, key -> loadSdkCodelist(key, sdkVersion));
  }

  @Override
  public SdkCodelist getOrDefault(final Object codelistId, final SdkCodelist defaultValue) {
    return this.computeIfAbsent((String) codelistId, key -> loadSdkCodelist(key, sdkVersion));
  }

  private static SdkCodelist loadSdkCodelist(final String codeListId, String sdkVersion) {
    // Find the SDK codelist .gc file that corresponds to the passed reference.
    // Stream the data from that file.
    final Genericode10CodeListMarshaller marshaller = GenericodeTools.getMarshaller();
    final Map<String, String> codelistIdToFilename;
    try {
      codelistIdToFilename = buildMapCodelistIdToFilename(SdkResourcesLoader.getInstance().getResourceAsPath(SdkConstants.ResourceType.CODELISTS, sdkVersion), marshaller);
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
    final String filename = codelistIdToFilename.get(codeListId);
    assert filename != null : "filename is null";
    try (
        InputStream is = SdkResourcesLoader.getInstance().getResourceAsStream(SdkConstants.ResourceType.CODELISTS, sdkVersion, filename)) {
      final CodeListDocument cl = marshaller.read(is);
      final SimpleCodeList scl = cl.getSimpleCodeList();
      final String codelistVersion = cl.getIdentification().getVersion(); // Version
                                                                          // tag
                                                                          // of
                                                                          // .gc
      // Get all the code values in a list.
      // We assume there are no duplicate code values in the referenced
      // codelists.
      final List<String> codes = scl.getRow().stream().map(row -> {
        final String codeVal = row.getValue().stream().filter(v -> GenericodeTools.KEY_CODE.equals(GenericodeTools.extractColRefId(v))).findFirst()//
            .orElseThrow(RuntimeException::new)//
            .getSimpleValue()//
            .getValue().strip();
        return codeVal;
      }).collect(Collectors.toList());
      return new SdkCodelist(codeListId, codelistVersion, codes);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Map<String, String> buildMapCodelistIdToFilename(final Path pathFolder, final Genericode10CodeListMarshaller marshaller) throws IOException {
    final int depth = 1; // Flat folder, not recursive for now.
    return getFilePathsAsSet(pathFolder, depth, GenericodeTools.EXTENSION_DOT_GC)//
        // .parallelStream() // Overkill and also messes with logs order.
        .stream().map(path -> {
          final CodeListDocument cl = marshaller.read(path);
          final Identification identification = cl.getIdentification();
          // We use the longName as a ID, PK in the the DB.
          // But for the filenames we do not always follow this convention.
          // So we need to map.
          final String longNameStr = identification.getLongNameAtIndex(0).getValue();
          final String fileNameStr = path.getFileName().toString();
          return Map.entry(longNameStr, fileNameStr);
        }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
  }

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "False positive.")
  private static Set<Path> getFilePathsAsSet(final Path pathFolder, final int depth, final String extension) throws IOException {
    if (!pathFolder.toFile().isDirectory()) {
      throw new RuntimeException(String.format("Expecting folder but got: %s", pathFolder));
    }
    try (Stream<Path> stream = Files.walk(pathFolder, depth)) {
      return stream.filter(pathFile -> !Files.isDirectory(pathFile)//
          && pathFile.toString().endsWith(extension))//
          .collect(Collectors.toSet());
    }
  }
}
