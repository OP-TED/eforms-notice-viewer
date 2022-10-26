package eu.europa.ted.eforms.sdk.entity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import com.helger.genericode.Genericode10CodeListMarshaller;
import com.helger.genericode.v10.CodeListDocument;
import com.helger.genericode.v10.Identification;
import com.helger.genericode.v10.LongName;
import com.helger.genericode.v10.SimpleCodeList;
import eu.europa.ted.eforms.sdk.helpers.GenericodeTools;

public class SdkCodelistRepository extends HashMap<String, SdkCodelist> {
  private static final long serialVersionUID = 1L;

  private transient Path codelistsPath;
  private String sdkVersion;

  @SuppressWarnings("unused")
  private SdkCodelistRepository() {
    throw new UnsupportedOperationException();
  }

  public SdkCodelistRepository(String sdkVersion, Path codelistsPath) {
    this.sdkVersion = sdkVersion;
    this.codelistsPath = codelistsPath;
  }

  /**
   * Builds EFX list from the passed codelist reference. This will lazily compute and cache the
   * result for reuse as the operation can be costly on some large lists.
   *
   * @param codelistId A reference to an SDK codelist.
   * @return The EFX string representation of the list of all the codes of the referenced codelist.
   */
  @Override
  public final SdkCodelist get(final Object codelistId) {
    if (codelistId == null) {
      throw new RuntimeException("CodelistId is null.");
    }
    if (StringUtils.isBlank((String) codelistId)) {
      throw new RuntimeException("CodelistId is blank.");
    }
    return computeIfAbsent((String) codelistId, key -> {
      try {
        return loadSdkCodelist(sdkVersion, key, codelistsPath);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @Override
  public SdkCodelist getOrDefault(final Object codelistId, final SdkCodelist defaultValue) {
    return computeIfAbsent((String) codelistId, key -> {
      try {
        return loadSdkCodelist(sdkVersion, key, codelistsPath);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private static SdkCodelist loadSdkCodelist(final String sdkVersion, final String codeListId,
      final Path codelistsPath) throws InstantiationException {
    // Find the SDK codelist .gc file that corresponds to the passed reference.
    // Stream the data from that file.
    final Genericode10CodeListMarshaller marshaller = GenericodeTools.getMarshaller();
    final Map<String, String> codelistIdToFilename;
    try {
      codelistIdToFilename = buildMapCodelistIdToFilename(codelistsPath, marshaller);
    } catch (IOException e1) {
      throw new RuntimeException(e1);
    }
    final String filename = codelistIdToFilename.get(codeListId);
    assert filename != null : "filename is null";
    try (InputStream is = Files.newInputStream(codelistsPath.resolve(Path.of(filename)))) {
      final CodeListDocument cl = marshaller.read(is);
      final SimpleCodeList scl = cl.getSimpleCodeList();

      // Version tag of the genericode (gc) file.
      final String codelistVersion = cl.getIdentification().getVersion();
      final Optional<String> parentCodelistIdOpt = extractParentId(cl.getIdentification());

      // Get all the code values in a list.
      // We assume there are no duplicate code values in the referenced
      // codelists.
      final List<String> codes = scl.getRow().stream().map(row -> {
        return row.getValue().stream()
            .filter(v -> GenericodeTools.KEY_CODE.equals(GenericodeTools.extractColRefId(v)))
            .findFirst()//
            .orElseThrow(RuntimeException::new)//
            .getSimpleValue()//
            .getValue().strip();
      }).collect(Collectors.toList());

      return SdkEntityFactory.getSdkCodelist(sdkVersion, codeListId, codelistVersion, codes,
          parentCodelistIdOpt);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get eForms parent id.
   */
  public static final Optional<String> extractParentId(final Identification ident) {
    return extractLongnameWithIdentifier(ident, "eFormsParentId");
  }

  /**
   * @return The extracted value as an stripped string if present, optional empty otherwise.
   */
  public static Optional<String> extractLongnameWithIdentifier(final Identification identity,
      final String identifierStr) {
    final Optional<LongName> valueOpt = identity.getLongName().stream()
        .filter(item -> Objects.equals(item.getIdentifier(), identifierStr))//
        .findFirst();
    if (valueOpt.isPresent()) {
      final String parentId = valueOpt.get().getValue();
      return StringUtils.isBlank(parentId) ? Optional.empty() : Optional.of(parentId.strip());
    }
    return Optional.empty();
  }

  private static Map<String, String> buildMapCodelistIdToFilename(final Path pathFolder,
      final Genericode10CodeListMarshaller marshaller) throws IOException {
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

  @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
      value = "RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE", justification = "False positive.")
  private static Set<Path> getFilePathsAsSet(final Path pathFolder, final int depth,
      final String extension) throws IOException {
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
