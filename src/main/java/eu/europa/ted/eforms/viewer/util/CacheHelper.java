package eu.europa.ted.eforms.viewer.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Supplier;
import org.apache.commons.jcs3.JCS;
import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ted.eforms.viewer.NoticeViewerConstants;
import eu.europa.ted.efx.model.Markup;

/**
 * Utility class with methods for managing the caching of objects.
 */
public class CacheHelper {
  private static final Logger logger = LoggerFactory.getLogger(CacheHelper.class);

  private CacheHelper() {}

  /**
   * Caches {@link Markup} instances
   *
   * @param key The cache key
   * @param markup The {@link Markup} instance to cache
   */
  public static void cacheXslMarkup(String key, Markup markup) {
    if (markup != null) {
      logger.debug("Caching XSL markup with key[{}]", key);

      final CacheAccess<String, String> cache =
          JCS.getInstance(NoticeViewerConstants.NV_CACHE_REGION);

      cache.put(key, markup.script);
    }
  }

  /**
   * Retrieves an object from a cache region.
   *
   * @param <T> The type of the retrieved object
   * @param valueGenerator The function which computes the object's value if it is not found in the
   *        cache
   * @param cacheRegion The cache region to look into
   * @param keyParts An array containing the cache key's parts
   * @return An object of the expected type
   */
  public static <T> T get(final Supplier<T> valueGenerator, final String cacheRegion,
      final String[] keyParts) {
    return get(valueGenerator, cacheRegion, computeKey(keyParts));
  }

  /**
   * Retrieves an object from a cache region.
   *
   * @param <T> The type of the retrieved object
   * @param valueGenerator The function which computes the object's value if it is not found in the
   *        cache
   * @param cacheRegion The cache region to look into
   * @param key The string to be used as the cache key
   * @return An object of the expected type
   */
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

    logger.debug("Object not found in cache for key [{}]. Generating a new one...", key);

    result = valueGenerator.get();
    cache.put(key, result);

    return result;
  }

  /**
   * Caches an object.
   *
   * @param <T> The type of the object to cache
   * @param cacheRegion The cache region where the object will be inserted
   * @param value The value of the object to cache
   * @param keyParts An array containing the cache key's parts
   */
  public static <T> void put(final String cacheRegion, final T value, final String[] keyParts) {
    put(cacheRegion, value, computeKey(keyParts));
  }

  /**
   * Caches an object.
   *
   * @param <T> The type of the object to cache
   * @param cacheRegion The cache region where the object will be inserted
   * @param value The value of the object to cache
   * @param key The string to be used as the cache key
   */
  public static <T> void put(final String cacheRegion, final T value, final String key) {
    logger.debug("Adding object into cache region [{}] under key [{}]", cacheRegion, key);
    logger.trace("Object value: {}", value);

    JCS.getInstance(cacheRegion).put(key, value);
  }

  /**
   * Computes a cache key hash from an array of strings
   *
   * @param strings The strings to generate the hash from
   * @return A string representing a cache key
   */
  public static String computeKey(String... strings) {
    Validate.notEmpty(strings, "The array of strings cannot be empty");

    try {
      String key = new String(
          MessageDigest.getInstance("SHA-512")
              .digest((StringUtils.join(strings, "###").getBytes())));

      logger.trace("Computed key for [{}]: {}", strings, key);

      return key;
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Failed to compute cache key", e);
    }
  }

  /**
   * Clears all contents of a cache region
   *
   * @param cacheRegion The name of the target cache region
   */
  public static void clearCacheRegion(String cacheRegion) {
    logger.debug("Clearing cache region [{}]", cacheRegion);

    JCS.getInstance(cacheRegion).clear();
  }
}
