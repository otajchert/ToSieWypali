package com.tsw.storage;

public interface FileStorage {

    String upload(FileUpload file, String objectPath);

    void delete(String publicUrl);
}
