package com.tsw.service;

import com.tsw.dto.ProductImageResponse;
import com.tsw.dto.ProductResponse;
import com.tsw.model.Product;
import com.tsw.model.ProductImage;
import com.tsw.repository.CartItemRepository;
import com.tsw.repository.CategoryRepository;
import com.tsw.repository.OrderProductRepository;
import com.tsw.repository.ProductImageRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.storage.FileStorage;
import com.tsw.storage.FileUpload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceStorageTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private FileStorage fileStorage;

    @InjectMocks
    private ProductService productService;

    @Test
    void addGalleryImageUploadsNeutralFileAndSavesImageWithNextSortOrder() {
        UUID productId = UUID.randomUUID();
        UUID imageId = UUID.randomUUID();
        Product product = product(productId);
        product.setImages(List.of(imageWithSortOrder(1), imageWithSortOrder(4)));
        FileUpload file = new FileUpload(
                "gallery.final.png",
                "image/png",
                "gallery-content".getBytes(StandardCharsets.UTF_8)
        );
        String publicUrl = "https://storage.example/products/gallery.final.png";

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(fileStorage.upload(any(FileUpload.class), any(String.class))).thenReturn(publicUrl);
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(invocation -> {
            ProductImage image = invocation.getArgument(0);
            image.setId(imageId);
            return image;
        });

        ProductImageResponse response = productService.addGalleryImage(productId, file);

        ArgumentCaptor<FileUpload> uploadCaptor = ArgumentCaptor.forClass(FileUpload.class);
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileStorage).upload(uploadCaptor.capture(), pathCaptor.capture());
        assertSame(file, uploadCaptor.getValue());
        assertProductPath(pathCaptor.getValue(), ".png");

        ArgumentCaptor<ProductImage> imageCaptor = ArgumentCaptor.forClass(ProductImage.class);
        verify(productImageRepository).save(imageCaptor.capture());
        ProductImage savedImage = imageCaptor.getValue();
        assertEquals(imageId, savedImage.getId());
        assertEquals(publicUrl, savedImage.getImageUrl());
        assertEquals(5, savedImage.getSortOrder());
        assertSame(product, savedImage.getProduct());

        assertEquals(imageId, response.id());
        assertEquals(publicUrl, response.imageUrl());
        assertEquals(5, response.sortOrder());
    }

    @Test
    void setMainPhotoDeletesOldFileUploadsNewOneAndReturnsSavedProduct() {
        UUID productId = UUID.randomUUID();
        Product product = product(productId);
        String oldUrl = "https://storage.example/products/old.jpg";
        String newUrl = "https://storage.example/products/new.jpeg";
        product.setPhoto(oldUrl);
        FileUpload file = new FileUpload(
                "new.photo.jpeg",
                "image/jpeg",
                "main-photo-content".getBytes(StandardCharsets.UTF_8)
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(fileStorage.upload(any(FileUpload.class), any(String.class))).thenReturn(newUrl);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.setMainPhoto(productId, file);

        ArgumentCaptor<FileUpload> uploadCaptor = ArgumentCaptor.forClass(FileUpload.class);
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        InOrder order = inOrder(fileStorage, productRepository);
        order.verify(fileStorage).delete(oldUrl);
        order.verify(fileStorage).upload(uploadCaptor.capture(), pathCaptor.capture());
        order.verify(productRepository).save(productCaptor.capture());

        assertSame(file, uploadCaptor.getValue());
        assertProductPath(pathCaptor.getValue(), ".jpeg");
        assertSame(product, productCaptor.getValue());
        assertEquals(newUrl, productCaptor.getValue().getPhoto());
        assertEquals(productId, response.id());
        assertEquals(newUrl, response.photo());
    }

    private Product product(UUID id) {
        Product product = new Product();
        product.setId(id);
        product.setName("Test product");
        return product;
    }

    private ProductImage imageWithSortOrder(int sortOrder) {
        ProductImage image = new ProductImage();
        image.setSortOrder(sortOrder);
        return image;
    }

    private void assertProductPath(String path, String extension) {
        String prefix = "products/";
        assertTrue(path.startsWith(prefix));
        assertTrue(path.endsWith(extension));
        String id = path.substring(prefix.length(), path.length() - extension.length());
        assertDoesNotThrow(() -> UUID.fromString(id));
    }
}
