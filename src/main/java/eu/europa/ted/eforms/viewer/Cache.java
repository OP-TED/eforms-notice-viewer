package eu.europa.ted.eforms.viewer;

import java.util.function.Supplier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.efx.model.Markup;

/**
 *Class for managing the caching of objects.
 *
 */
public class Cache {
  private static final Logger logger = LoggerFactory.getLogger(Cache.class);

  private static CacheAccess<String, String> nvCache =
      JCS.getInstance(NoticeViewerConstants.NV_CACHE_REGION);

  private Cache() {}

  public static void cacheXslMarkup(String key, Markup markup) {
    if (markup != null) {
      logger.debug("Caching XSL markup with key[{}]", key);

      nvCache.put(key, markup.script);
    }
  }

  public static String getString(final Supplier<String> stringGenerator, final String... keyParts) {
    String cacheKey = computeKey(keyParts);
    String result = nvCache.get(cacheKey);

    if (StringUtils.isNotBlank(result)) {
      logger.trace("Found string in cache for key [{}]", cacheKey);

      return result;
    }

    result = stringGenerator.get();
    nvCache.put(cacheKey, result);

    return result;
  }

  public static String computeKey(String... strings) {
    Validate.notEmpty(strings, "The array of strings cannot be empty");
    String key = DigestUtils.md5Hex((StringUtils.join(strings, "###")));

    logger.trace("Computed key for [{}]: {}", strings, key);

    return key;
  }

  public static void clear() {
    logger.debug("Clearing cache");

    nvCache.clear();
  }
}
