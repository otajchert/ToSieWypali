package com.tsw.storage;

public record FileUpload(
        String originalFilename,
        String contentType,
        byte[] content
) {
}
