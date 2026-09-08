package com.tsw.infrastructure.storage;

import com.tsw.exception.StorageOperationException;
import com.tsw.storage.FileStorage;
import com.tsw.storage.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class SupabaseStorageAdapter implements FileStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupabaseStorageAdapter.class);

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final RestClient restClient;

    public SupabaseStorageAdapter(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.key}") String serviceKey,
            @Value("${supabase.bucket:products}") String bucket,
            RestClient.Builder restClientBuilder) {
        String baseUrl = supabaseUrl.trim().replaceAll("/+$", "");
        this.supabaseUrl = baseUrl.replaceFirst("/rest/v1$", "");
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String upload(FileUpload file, String objectPath) {
        String contentType = file.contentType() != null
                ? file.contentType()
                : "application/octet-stream";
        try {
            restClient.post()
                    .uri(supabaseUrl + "/storage/v1/object/" + bucket + "/" + objectPath)
                    .header("Authorization", "Bearer " + serviceKey)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(file.content())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new StorageOperationException(exception);
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + objectPath;
    }

    @Override
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
