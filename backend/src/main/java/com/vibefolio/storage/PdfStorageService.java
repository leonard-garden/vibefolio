package com.vibefolio.storage;

/**
 * Abstraction for PDF blob storage (Cloudflare R2 via S3-compatible API).
 * Services depend on this interface — never on the concrete R2 implementation.
 */
public interface PdfStorageService {

    /**
     * Upload a PDF and return its public blob URL.
     *
     * @param pdfBytes raw PDF bytes
     * @param filename  destination object key / filename
     * @return full URL of the stored object (R2 public URL)
     */
    String upload(byte[] pdfBytes, String filename);

    /**
     * Delete a stored PDF by its blob URL.
     *
     * @param blobUrl the URL previously returned by {@link #upload}
     */
    void delete(String blobUrl);
}
