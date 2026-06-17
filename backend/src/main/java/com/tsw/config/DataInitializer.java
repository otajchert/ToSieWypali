package com.tsw.config;

import com.tsw.model.*;
import com.tsw.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AddressRepository addressRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final ShippingMethodRepository shippingMethodRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ShopOrderRepository shopOrderRepository;
    private final OrderProductRepository orderProductRepository;

    public DataInitializer(ClientRepository clientRepository,
                           PasswordEncoder passwordEncoder,
                           AddressRepository addressRepository,
                           ClientAddressRepository clientAddressRepository,
                           OrderStatusRepository orderStatusRepository,
                           ShippingMethodRepository shippingMethodRepository,
                           ProductRepository productRepository,
                           CategoryRepository categoryRepository,
                           ProductImageRepository productImageRepository,
                           ShopOrderRepository shopOrderRepository,
                           OrderProductRepository orderProductRepository) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressRepository = addressRepository;
        this.clientAddressRepository = clientAddressRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productImageRepository = productImageRepository;
        this.shopOrderRepository = shopOrderRepository;
        this.orderProductRepository = orderProductRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedProducts();
        backfillProductWeights();
        seedTestUser();
    }

    private void seedAdmin() {
        if (clientRepository.findByEmail("admin@gmail.com").isEmpty()) {
            Client admin = new Client();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("TSW");
            admin.setRole("ADMIN");
            clientRepository.save(admin);
        }
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        Category kubki     = categoryRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000001")).orElse(null);
        Category miski     = categoryRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000002")).orElse(null);
        Category wazony    = categoryRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000003")).orElse(null);
        Category dekoracje = categoryRepository.findById(UUID.fromString("00000000-0000-0000-0000-000000000004")).orElse(null);

        Product kubek = new Product();
        kubek.setId(UUID.fromString("00000000-0000-0000-0000-000000001001"));
        kubek.setName("Kubek ceramiczny w pieski");
        kubek.setDescription("Recznie robiony kubek z jasnej gliny z rysunkami pieskow.");
        kubek.setPhoto("https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-16%20151806.png");
        kubek.setQtyInStock(4);
        kubek.setSku("MUG-001");
        kubek.setPrice(new BigDecimal("65.00"));
        kubek.setWeight("350 g");
        kubek.setHeight("10cm");
        kubek.setWidth("8cm");
        kubek.setProductLength("8cm");
        kubek.setCategories(new LinkedHashSet<>());
        if (kubki != null) kubek.getCategories().add(kubki);
        kubek = productRepository.save(kubek);

        ProductImage kubkImg = new ProductImage();
        kubkImg.setImageUrl("https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-14%20215052.png");
        kubkImg.setSortOrder(0);
        kubkImg.setProduct(kubek);
        productImageRepository.save(kubkImg);

        Product mydelniczka = new Product();
        mydelniczka.setId(UUID.fromString("00000000-0000-0000-0000-000000001002"));
        mydelniczka.setName("Mydelniczka duża");
        mydelniczka.setDescription("Organiczna forma z zaglebieniem na mydlo");
        mydelniczka.setPhoto("https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-14%20215009.png");
        mydelniczka.setQtyInStock(8);
        mydelniczka.setSku("DEC-001");
        mydelniczka.setPrice(new BigDecimal("110.00"));
        mydelniczka.setWeight("200 g");
        mydelniczka.setHeight("5cm");
        mydelniczka.setWidth("18cm");
        mydelniczka.setProductLength("13cm");
        mydelniczka.setCategories(new LinkedHashSet<>());
        if (dekoracje != null) mydelniczka.getCategories().add(dekoracje);
        mydelniczka = productRepository.save(mydelniczka);

        ProductImage mydelImg = new ProductImage();
        mydelImg.setImageUrl("https://kriocushjyphhprzosml.supabase.co/storage/v1/object/public/products/Zrzut%20ekranu%202025-12-16%20151924.png");
        mydelImg.setSortOrder(0);
        mydelImg.setProduct(mydelniczka);
        productImageRepository.save(mydelImg);

        Product miska = new Product();
        miska.setId(UUID.fromString("00000000-0000-0000-0000-000000001003"));
        miska.setName("Miska do zupy");
        miska.setDescription("Sredniej wielkosci miska");
        miska.setQtyInStock(12);
        miska.setSku("BWL-001");
        miska.setPrice(new BigDecimal("55.00"));
        miska.setWeight("480 g");
        miska.setHeight("11cm");
        miska.setWidth("16cm");
        miska.setProductLength("16cm");
        miska.setCategories(new LinkedHashSet<>());
        if (miski != null) miska.getCategories().add(miski);
        productRepository.save(miska);

        Product wazon = new Product();
        wazon.setId(UUID.fromString("00000000-0000-0000-0000-000000001004"));
        wazon.setName("Wazon Brzuszek");
        wazon.setDescription("Duzy wazon - rzezba brzucha");
        wazon.setQtyInStock(0);
        wazon.setSku("VAS-001");
        wazon.setPrice(new BigDecimal("120.00"));
        wazon.setWeight("1800 g");
        wazon.setHeight("45cm");
        wazon.setWidth("25cm");
        wazon.setProductLength("35cm");
        wazon.setCategories(new LinkedHashSet<>());
        if (wazony != null) wazon.getCategories().add(wazony);
        if (dekoracje != null) wazon.getCategories().add(dekoracje);
        productRepository.save(wazon);
    }

    private void backfillProductWeights() {
        java.util.Map<UUID, String> weights = new java.util.LinkedHashMap<>();
        weights.put(UUID.fromString("00000000-0000-0000-0000-000000001001"), "350 g");
        weights.put(UUID.fromString("00000000-0000-0000-0000-000000001002"), "200 g");
        weights.put(UUID.fromString("00000000-0000-0000-0000-000000001003"), "480 g");
        weights.put(UUID.fromString("00000000-0000-0000-0000-000000001004"), "1800 g");
        weights.forEach((id, w) ->
            productRepository.findById(id).ifPresent(p -> {
                if (p.getWeight() == null || p.getWeight().isBlank()) {
                    p.setWeight(w);
                    productRepository.save(p);
                }
            })
        );
    }

    private void seedTestUser() {
        if (clientRepository.findByEmail("abc@gmail.com").isPresent()) return;

        Client user = new Client();
        user.setEmail("abc@gmail.com");
        user.setPassword(passwordEncoder.encode("abc123"));
        user.setFirstName("abc");
        user.setLastName("abc");
        user.setRole("CLIENT");
        user = clientRepository.save(user);

        Address address = new Address();
        address.setCity("Poznań");
        address.setRegion("Wielkopolskie");
        address.setPostalCode("61-001");
        address.setStreetNumber("ul. Półwiejska 42");
        address = addressRepository.save(address);

        ClientAddressId linkId = new ClientAddressId();
        linkId.setClientId(user.getId());
        linkId.setAddressId(address.getId());
        ClientAddress link = new ClientAddress();
        link.setId(linkId);
        link.setClient(user);
        link.setAddress(address);
        link.setName("Dom");
        link.setIsDefault(true);
        clientAddressRepository.save(link);

        OrderStatus delivered = orderStatusRepository.findByName("Dostarczone").orElse(null);
        ShippingMethod dpd = shippingMethodRepository.findByName("Kurier DPD").orElse(null);

        UUID kubkId = UUID.fromString("00000000-0000-0000-0000-000000001001");
        UUID miskaId = UUID.fromString("00000000-0000-0000-0000-000000001003");
        Product kubek = productRepository.findById(kubkId).orElse(null);
        Product miska = productRepository.findById(miskaId).orElse(null);

        if (kubek != null && dpd != null) {
            createOrder(user, address, dpd, delivered,
                    kubek, 1, kubek.getPrice().add(dpd.getPrice()),
                    LocalDateTime.now().minusDays(14));
        }

        if (miska != null && dpd != null) {
            createOrder(user, address, dpd, delivered,
                    miska, 2, miska.getPrice().multiply(BigDecimal.valueOf(2)).add(dpd.getPrice()),
                    LocalDateTime.now().minusDays(7));
        }
    }

    private void createOrder(Client client, Address address, ShippingMethod shipping,
                              OrderStatus status, Product product, int qty,
                              BigDecimal total, LocalDateTime date) {
        ShopOrder order = new ShopOrder();
        order.setClient(client);
        order.setShippingAddress(address);
        order.setShippingMethod(shipping);
        order.setOrderStatus(status);
        order.setOrderTotal(total);
        order.setOrderDate(date);
        order = shopOrderRepository.save(order);

        OrderProductId opId = new OrderProductId();
        opId.setOrderId(order.getId());
        opId.setProductId(product.getId());

        OrderProduct op = new OrderProduct();
        op.setId(opId);
        op.setOrder(order);
        op.setProduct(product);
        op.setQty(qty);
        op.setPrice(product.getPrice());
        orderProductRepository.save(op);
    }
}
