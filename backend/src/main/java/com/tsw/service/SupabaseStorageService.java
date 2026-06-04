package com.tsw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class SupabaseStorageService {

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final RestClient restClient = RestClient.create();

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String serviceKey,
            @Value("${supabase.bucket:products}") String bucket) {
        this.supabaseUrl = supabaseUrl;
        this.serviceKey = serviceKey;
        this.bucket = bucket;
    }

    /**
     * Uploads a file to Supabase Storage and returns its public URL.
     * The bucket must be set to Public in the Supabase dashboard.
     */
    public String upload(MultipartFile file, String objectPath) throws IOException {
        String contentType = file.getContentType() != null
                ? file.getContentType()
                : "application/octet-stream";
        try {
            restClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(file.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new IOException("Supabase upload failed for " + objectPath + ": " + e.getMessage(), e);
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    /**
     * Deletes a file from Supabase Storage given its public URL.
     * Silently ignores failures so that a missing file never breaks a delete request.
     */
    public void delete(String publicUrl) {
        if (publicUrl == null) return;

        String prefix = supabaseUrl + "/storage/v1/object/public/" + bucket + "/";
        if (!publicUrl.startsWith(prefix)) return;

        String objectPath = publicUrl.substring(prefix.length());

        try {
            restClient.method(HttpMethod.DELETE)
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("prefixes", List.of(objectPath)))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            System.err.println("Could not delete from Supabase: " + objectPath + " - " + e.getMessage());
        }
    }
}
