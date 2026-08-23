package com.tsw.controller;

import com.tsw.dto.ImageUrlRequest;
import com.tsw.dto.ProductRequest;
import com.tsw.model.Product;
import com.tsw.model.ProductImage;
import com.tsw.service.ProductService;
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
    public List<Product> getAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable UUID id) {
        return productService.getById(id);
    }

    @PostMapping
    public Product create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/images")
    public ProductImage addImage(@PathVariable UUID id,
                                 @RequestParam("file") MultipartFile file) {
        return productService.addGalleryImage(id, file);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> removeImage(@PathVariable UUID id,
                                             @PathVariable UUID imageId) {
        productService.removeGalleryImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/photo-url")
    public Product linkPhotoUrl(@PathVariable UUID id,
                                @Valid @RequestBody ImageUrlRequest request) {
        return productService.linkPhotoUrl(id, request.url());
    }

    @PostMapping("/{id}/image-url")
    public ProductImage linkGalleryUrl(@PathVariable UUID id,
                                       @Valid @RequestBody ImageUrlRequest request) {
        return productService.linkGalleryUrl(id, request.url());
    }

    @PutMapping("/{id}/images/{imageId}/set-main")
    public Product setImageAsMain(@PathVariable UUID id,
                                  @PathVariable UUID imageId) {
        return productService.setImageAsMain(id, imageId);
    }

    @PostMapping("/{id}/photo")
    public Product setMainPhoto(@PathVariable UUID id,
                                @RequestParam("file") MultipartFile file) {
        return productService.setMainPhoto(id, file);
    }
}
