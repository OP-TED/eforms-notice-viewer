package eu.europa.ted.eforms.viewer.helpers;

import java.util.function.Supplier;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.efx.model.Markup;

/**
 *Class for managing the caching of objects.
 *
 */
public class CacheHelper {
  private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

  private CacheHelper() {}

  public static void cacheXslMarkup(String key, Markup markup) {
    if (markup != null) {
      logger.debug("Caching XSL markup with key[{}]", key);

      final CacheAccess<String, String> cache =
          JCS.getInstance(NoticeViewerConstants.NV_CACHE_REGION);

      cache.put(key, markup.script);
    }
  }

  public static <T> T get(final Supplier<T> valueGenerator, final String cacheRegion,
      final String[] keyParts) {
    return get(valueGenerator, cacheRegion, computeKey(keyParts));
  }

  public static <T> T get(final Supplier<T> valueGenerator, final String cacheRegion,
      final String key) {

    logger.debug("Getting value from cache region [{}] for key: {}", cacheRegion, key);

    final CacheAccess<String, T> cache = JCS.getInstance(cacheRegion);

    T result = cache.get(key);

    if (result != null) {
      logger.debug("Found object in cache for key [{}]", key);
      logger.trace("Found object value: {}", result);

      return result;
    }

    result = valueGenerator.get();
    cache.put(key, result);

    return result;
  }

  public static <T> void put(final String cacheRegion, final T value, final String[] keyParts) {
    put(cacheRegion, value, computeKey(keyParts));
  }

  public static <T> void put(final String cacheRegion, final T value, final String key) {
    logger.debug("Adding ojbect into cache region [{}] under key [{}]", cacheRegion, key);
    logger.trace("Object value: {}", value);

    JCS.getInstance(cacheRegion).put(key, value);
  }

  public static String computeKey(String... strings) {
    Validate.notEmpty(strings, "The array of strings cannot be empty");
    String key = DigestUtils.md5Hex((StringUtils.join(strings, "###")));

    logger.trace("Computed key for [{}]: {}", strings, key);

    return key;
  }

  public static void clearCacheRegion(String cacheRegion) {
    logger.debug("Clearing cache region [{}]", cacheRegion);

    JCS.getInstance(cacheRegion).clear();
  }
}
