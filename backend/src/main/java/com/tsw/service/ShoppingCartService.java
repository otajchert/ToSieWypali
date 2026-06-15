package com.tsw.service;

import com.tsw.model.CartItem;
import com.tsw.model.Client;
import com.tsw.model.Product;
import com.tsw.model.ShoppingCart;
import com.tsw.repository.CartItemRepository;
import com.tsw.repository.ClientRepository;
import com.tsw.repository.ProductRepository;
import com.tsw.repository.ShoppingCartRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    public ShoppingCartService(ShoppingCartRepository cartRepository,
                               CartItemRepository cartItemRepository,
                               ClientRepository clientRepository,
                               ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> getItems(UUID clientId) {
        return getOrCreateCart(clientId)
                .map(cart -> cartItemRepository.findByCartId(cart.getId()))
                .orElse(List.of());
    }

    public CartItem addItem(UUID clientId, UUID productId, int qty) {
        ShoppingCart cart = getOrCreateCart(clientId)
                .orElseGet(() -> createCart(clientId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<CartItem> existing = cartItemRepository.findByCartId(cart.getId());
        Optional<CartItem> existingItem = existing.stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQty() + qty;
            if (newQty > product.getQtyInStock()) {
                throw new IllegalArgumentException("Niewystarczająca ilość w magazynie");
            }
            item.setQty(newQty);
            return cartItemRepository.save(item);
        }

        if (qty > product.getQtyInStock()) {
            throw new IllegalArgumentException("Niewystarczająca ilość w magazynie");
        }

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQty(qty);
        return cartItemRepository.save(item);
    }

    public boolean removeItem(UUID clientId, UUID itemId) {
        return cartItemRepository.findById(itemId).map(item -> {
            if (!item.getCart().getClient().getId().equals(clientId)) {
                return false;
            }
            cartItemRepository.delete(item);
            return true;
        }).orElse(false);
    }

    public Optional<CartItem> updateItemQty(UUID clientId, UUID itemId, int qty) {
        return cartItemRepository.findById(itemId).map(item -> {
            if (!item.getCart().getClient().getId().equals(clientId)) {
                return null;
            }
            if (qty < 1 || qty > item.getProduct().getQtyInStock()) {
                return null;
            }
            item.setQty(qty);
            return cartItemRepository.save(item);
        });
    }

    public void clearCart(UUID clientId) {
        getOrCreateCart(clientId).ifPresent(cart -> {
            cartItemRepository.findByCartId(cart.getId())
                    .forEach(cartItemRepository::delete);
        });
    }

    private Optional<ShoppingCart> getOrCreateCart(UUID clientId) {
        return cartRepository.findByClientId(clientId);
    }

    private ShoppingCart createCart(UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));
        ShoppingCart cart = new ShoppingCart();
        cart.setClient(client);
        return cartRepository.save(cart);
    }
}
