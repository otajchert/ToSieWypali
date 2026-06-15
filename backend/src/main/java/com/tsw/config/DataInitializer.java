package com.tsw.config;

import com.tsw.model.*;
import com.tsw.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private final ShopOrderRepository shopOrderRepository;
    private final OrderProductRepository orderProductRepository;

    public DataInitializer(ClientRepository clientRepository,
                           PasswordEncoder passwordEncoder,
                           AddressRepository addressRepository,
                           ClientAddressRepository clientAddressRepository,
                           OrderStatusRepository orderStatusRepository,
                           ShippingMethodRepository shippingMethodRepository,
                           ProductRepository productRepository,
                           ShopOrderRepository shopOrderRepository,
                           OrderProductRepository orderProductRepository) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.addressRepository = addressRepository;
        this.clientAddressRepository = clientAddressRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.shippingMethodRepository = shippingMethodRepository;
        this.productRepository = productRepository;
        this.shopOrderRepository = shopOrderRepository;
        this.orderProductRepository = orderProductRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
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
