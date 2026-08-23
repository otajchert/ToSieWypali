package com.tsw.service;

import com.tsw.exception.StorageOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SupabaseStorageService.class);

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final RestClient restClient = RestClient.create();

    public SupabaseStorageService(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String serviceKey,
            @Value("${supabase.bucket:products}") String bucket) {
        String baseUrl = supabaseUrl.trim().replaceAll("/+$", "");
        this.supabaseUrl = baseUrl.replaceFirst("/rest/v1$", "");
        this.serviceKey = serviceKey;
        this.bucket = bucket;
    }

    public String upload(MultipartFile file, String objectPath) {
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
        } catch (RestClientException | IOException exception) {
            throw new StorageOperationException(exception);
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

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
        } catch (RestClientException exception) {
            LOGGER.warn("Could not delete Supabase object {}", objectPath, exception);
        }
    }
}
