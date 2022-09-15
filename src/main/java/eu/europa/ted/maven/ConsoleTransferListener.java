package eu.europa.ted.maven;

import static java.util.Objects.requireNonNull;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simplistic transfer listener that logs uploads/downloads to the console.
 */
public class ConsoleTransferListener extends AbstractTransferListener {
  private static final Logger logger = LoggerFactory.getLogger(ConsoleTransferListener.class);

  private final Map<TransferResource, Long> downloads = new ConcurrentHashMap<>();

  private int lastLength;

  public ConsoleTransferListener() {
  }

  @Override
  public void transferInitiated(TransferEvent event) {
    requireNonNull(event, "event cannot be null");
    String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";

    logger.debug("{}: {}{}", message, event.getResource().getRepositoryUrl(), event.getResource().getResourceName());
  }

  @Override
  public void transferProgressed(TransferEvent event) {
    requireNonNull(event, "event cannot be null");
    TransferResource resource = event.getResource();
    downloads.put(resource, event.getTransferredBytes());

    StringBuilder buffer = new StringBuilder(64);

    for (Map.Entry<TransferResource, Long> entry : downloads.entrySet()) {
      long total = entry.getKey().getContentLength();
      long complete = entry.getValue();

      buffer.append(getStatus(complete, total)).append("  ");
    }

    int pad = lastLength - buffer.length();
    lastLength = buffer.length();
    pad(buffer, pad);
    buffer.append('\r');

    logger.debug(buffer.toString());
  }

  private String getStatus(long complete, long total) {
    if (total >= 1024) {
      return toKB(complete) + "/" + toKB(total) + " KB ";
    } else if (total >= 0) {
      return complete + "/" + total + " B ";
    } else if (complete >= 1024) {
      return toKB(complete) + " KB ";
    } else {
      return complete + " B ";
    }
  }

  private void pad(StringBuilder buffer, int spaces) {
    String block = "                                        ";
    while (spaces > 0) {
      int n = Math.min(spaces, block.length());
      buffer.append(block, 0, n);
      spaces -= n;
    }
  }

  @Override
  public void transferSucceeded(TransferEvent event) {
    requireNonNull(event, "event cannot be null");
    transferCompleted(event);

    TransferResource resource = event.getResource();
    long contentLength = event.getTransferredBytes();
    if (contentLength >= 0) {
      String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded");
      String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

      String throughput = "";
      long duration = System.currentTimeMillis() - resource.getTransferStartTime();
      if (duration > 0) {
        long bytes = contentLength - resource.getResumeOffset();
        DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
        double kbPerSec = (bytes / 1024.0) / (duration / 1000.0);
        throughput = " at " + format.format(kbPerSec) + " KB/sec";
      }

      logger.debug("{}: {}{} ({}{})", type, resource.getRepositoryUrl(), resource.getResourceName(), len, throughput);
    }
  }

  @Override
  public void transferFailed(TransferEvent event) {
    requireNonNull(event, "event cannot be null");
    transferCompleted(event);

    if (!(event.getException() instanceof MetadataNotFoundException)) {
      logger.debug("Transfer failed.", event.getException());
    }
  }

  private void transferCompleted(TransferEvent event) {
    requireNonNull(event, "event cannot be null");
    downloads.remove(event.getResource());

    StringBuilder buffer = new StringBuilder(64);
    pad(buffer, lastLength);
    buffer.append('\r');
    logger.debug(buffer.toString());
  }

  @Override
  public void transferCorrupted(TransferEvent event) {
    requireNonNull(event, "event cannot be null");
    logger.debug("Transfer corrupted.", event.getException());
  }

  protected long toKB(long bytes) {
    return (bytes + 1023) / 1024;
  }

}
