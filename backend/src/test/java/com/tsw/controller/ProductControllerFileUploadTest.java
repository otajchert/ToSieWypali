package com.tsw.controller;

import com.tsw.dto.ProductImageResponse;
import com.tsw.dto.ProductResponse;
import com.tsw.exception.StorageOperationException;
import com.tsw.service.ProductService;
import com.tsw.storage.FileUpload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerFileUploadTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void addImageMapsMultipartFileToNeutralFileUpload() {
        UUID productId = UUID.randomUUID();
        byte[] content = "gallery-image".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "gallery.jpg",
                "image/jpeg",
                content
        );
        ProductImageResponse expectedResponse = new ProductImageResponse(
                UUID.randomUUID(),
                "https://storage.example/products/gallery.jpg",
                0
        );
        when(productService.addGalleryImage(eq(productId), any(FileUpload.class)))
                .thenReturn(expectedResponse);

        ProductImageResponse response = productController.addImage(productId, multipartFile);

        ArgumentCaptor<FileUpload> uploadCaptor = ArgumentCaptor.forClass(FileUpload.class);
        verify(productService).addGalleryImage(eq(productId), uploadCaptor.capture());
        assertFileUpload(uploadCaptor.getValue(), "gallery.jpg", "image/jpeg", content);
        assertSame(expectedResponse, response);
    }

    @Test
    void setMainPhotoMapsMultipartFileToNeutralFileUpload() {
        UUID productId = UUID.randomUUID();
        byte[] content = "main-photo".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file",
                "main.png",
                "image/png",
                content
        );
        ProductResponse expectedResponse = productResponse(productId);
        when(productService.setMainPhoto(eq(productId), any(FileUpload.class)))
                .thenReturn(expectedResponse);

        ProductResponse response = productController.setMainPhoto(productId, multipartFile);

        ArgumentCaptor<FileUpload> uploadCaptor = ArgumentCaptor.forClass(FileUpload.class);
        verify(productService).setMainPhoto(eq(productId), uploadCaptor.capture());
        assertFileUpload(uploadCaptor.getValue(), "main.png", "image/png", content);
        assertSame(expectedResponse, response);
    }

    @Test
    void wrapsFileReadFailureInStorageOperationException() throws IOException {
        UUID productId = UUID.randomUUID();
        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
        IOException cause = new IOException("Cannot read multipart file");
        when(multipartFile.getOriginalFilename()).thenReturn("broken.jpg");
        when(multipartFile.getContentType()).thenReturn("image/jpeg");
        when(multipartFile.getBytes()).thenThrow(cause);

        StorageOperationException exception = assertThrows(
                StorageOperationException.class,
                () -> productController.addImage(productId, multipartFile)
        );

        assertSame(cause, exception.getCause());
        verifyNoInteractions(productService);
    }

    private void assertFileUpload(FileUpload upload,
                                  String originalFilename,
                                  String contentType,
                                  byte[] content) {
        assertEquals(originalFilename, upload.originalFilename());
        assertEquals(contentType, upload.contentType());
        assertArrayEquals(content, upload.content());
    }

    private ProductResponse productResponse(UUID productId) {
        return new ProductResponse(
                productId,
                "Test product",
                null,
                "https://storage.example/products/main.png",
                1,
                "TSW-TEST",
                BigDecimal.ONE,
                null,
                null,
                null,
                null,
                Set.of(),
                List.of()
        );
    }
}
