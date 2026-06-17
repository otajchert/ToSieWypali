package com.tsw.controller;

import com.tsw.dto.ProductRequest;
import com.tsw.model.Product;
import com.tsw.model.ProductImage;
import com.tsw.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public List<Product> getAll() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable UUID id) {
        return productService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductRequest req) {
        return ResponseEntity.ok(productService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable UUID id, @RequestBody ProductRequest req) {
        return productService.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        try {
            return productService.delete(id)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ProductImage> addImage(@PathVariable UUID id,
                                                  @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(productService.addGalleryImage(id, file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public ResponseEntity<Void> removeImage(@PathVariable UUID id,
                                             @PathVariable UUID imageId) {
        return productService.removeGalleryImage(id, imageId)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/photo-url")
    public ResponseEntity<Product> linkPhotoUrl(@PathVariable UUID id,
                                                 @RequestBody java.util.Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) return ResponseEntity.badRequest().build();
        return productService.linkPhotoUrl(id, url)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/image-url")
    public ResponseEntity<ProductImage> linkGalleryUrl(@PathVariable UUID id,
                                                        @RequestBody java.util.Map<String, String> body) {
        String url = body.get("url");
        if (url == null || url.isBlank()) return ResponseEntity.badRequest().build();
        return productService.linkGalleryUrl(id, url)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/images/{imageId}/set-main")
    public ResponseEntity<Product> setImageAsMain(@PathVariable UUID id,
                                                   @PathVariable UUID imageId) {
        return productService.setImageAsMain(id, imageId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/photo")
    public ResponseEntity<Product> setMainPhoto(@PathVariable UUID id,
                                                 @RequestParam("file") MultipartFile file) {
        try {
            return ResponseEntity.ok(productService.setMainPhoto(id, file));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
