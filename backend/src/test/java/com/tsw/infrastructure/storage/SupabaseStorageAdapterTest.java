package com.tsw.infrastructure.storage;

import com.tsw.exception.StorageOperationException;
import com.tsw.storage.FileUpload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SupabaseStorageAdapterTest {

    private static final String SUPABASE_URL = "https://project.supabase.co";
    private static final String SERVICE_KEY = "test-service-key";
    private static final String BUCKET = "products";

    private MockRestServiceServer server;
    private SupabaseStorageAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder();
        server = MockRestServiceServer.bindTo(restClientBuilder).build();
        adapter = new SupabaseStorageAdapter(
                SUPABASE_URL + "/rest/v1/",
                SERVICE_KEY,
                BUCKET,
                restClientBuilder
        );
    }

    @Test
    void uploadSendsFileAndReturnsPublicUrl() {
        byte[] fileContent = "image-content".getBytes(StandardCharsets.UTF_8);
        String objectPath = "products/test-image.png";

        server.expect(once(), requestTo(SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + objectPath))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + SERVICE_KEY))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE))
                .andExpect(content().bytes(fileContent))
                .andRespond(withSuccess());

        String publicUrl = adapter.upload(
                new FileUpload("test-image.png", MediaType.IMAGE_PNG_VALUE, fileContent),
                objectPath
        );

        assertEquals(
                SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + objectPath,
                publicUrl
        );
        server.verify();
    }

    @Test
    void uploadUsesBinaryContentTypeWhenMimeTypeIsMissing() {
        byte[] fileContent = {1, 2, 3};
        String objectPath = "products/file.bin";

        server.expect(requestTo(SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + objectPath))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(content().bytes(fileContent))
                .andRespond(withSuccess());

        adapter.upload(new FileUpload("file.bin", null, fileContent), objectPath);

        server.verify();
    }

    @Test
    void uploadWrapsHttpErrorInStorageOperationException() {
        FileUpload file = new FileUpload("file.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1});
        String objectPath = "products/file.jpg";

        server.expect(requestTo(SUPABASE_URL + "/storage/v1/object/" + BUCKET + "/" + objectPath))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(StorageOperationException.class, () -> adapter.upload(file, objectPath));
        server.verify();
    }

    @Test
    void deleteSendsRequestOnlyForUrlFromConfiguredBucket() {
        String objectPath = "products/test-image.png";
        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + objectPath;

        server.expect(requestTo(SUPABASE_URL + "/storage/v1/object/" + BUCKET))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer " + SERVICE_KEY))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json("{\"prefixes\":[\"products/test-image.png\"]}"))
                .andRespond(withSuccess());

        adapter.delete(null);
        adapter.delete("https://example.com/image.png");
        adapter.delete(publicUrl);

        server.verify();
    }

    @Test
    void deleteIgnoresStorageHttpError() {
        String objectPath = "products/test-image.png";
        String publicUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET + "/" + objectPath;

        server.expect(requestTo(SUPABASE_URL + "/storage/v1/object/" + BUCKET))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertDoesNotThrow(() -> adapter.delete(publicUrl));
        server.verify();
    }
}
