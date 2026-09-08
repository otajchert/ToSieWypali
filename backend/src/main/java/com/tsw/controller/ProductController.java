package com.tsw.controller;

import com.tsw.dto.ImageUrlRequest;
import com.tsw.dto.ProductImageResponse;
import com.tsw.dto.ProductRequest;
import com.tsw.dto.ProductResponse;
import com.tsw.exception.StorageOperationException;
import com.tsw.service.ProductService;
import com.tsw.storage.FileUpload;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ProductImageResponse addImage(@PathVariable UUID id,
                                         @RequestParam("file") MultipartFile file) {
        return productService.addGalleryImage(id, toFileUpload(file));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> removeImage(@PathVariable UUID id,
                                             @PathVariable UUID imageId) {
        productService.removeGalleryImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photo-url")
    public ProductResponse linkPhotoUrl(@PathVariable UUID id,
                                        @Valid @RequestBody ImageUrlRequest request) {
        return productService.linkPhotoUrl(id, request.url());
    }

    @PostMapping("/{id}/image-url")
    public ProductImageResponse linkGalleryUrl(@PathVariable UUID id,
                                               @Valid @RequestBody ImageUrlRequest request) {
        return productService.linkGalleryUrl(id, request.url());
    }

    @PutMapping("/{id}/images/{imageId}/set-main")
    public ProductResponse setImageAsMain(@PathVariable UUID id,
                                          @PathVariable UUID imageId) {
        return productService.setImageAsMain(id, imageId);
    }

    @PostMapping("/{id}/photo")
    public ProductResponse setMainPhoto(@PathVariable UUID id,
                                        @RequestParam("file") MultipartFile file) {
        return productService.setMainPhoto(id, toFileUpload(file));
    }

    private FileUpload toFileUpload(MultipartFile file) {
        try {
            return new FileUpload(file.getOriginalFilename(), file.getContentType(), file.getBytes());
        } catch (IOException exception) {
            throw new StorageOperationException(exception);
        }
    }
}
