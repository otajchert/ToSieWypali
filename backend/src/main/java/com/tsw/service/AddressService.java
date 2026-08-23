package com.tsw.service;

import com.tsw.dto.AddressRequest;
import com.tsw.exception.ApiErrorCode;
import com.tsw.exception.ResourceNotFoundException;
import com.tsw.model.Address;
import com.tsw.model.Client;
import com.tsw.model.ClientAddress;
import com.tsw.model.ClientAddressId;
import com.tsw.repository.AddressRepository;
import com.tsw.repository.ClientAddressRepository;
import com.tsw.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final ClientAddressRepository clientAddressRepository;
    private final ClientRepository clientRepository;

    public AddressService(AddressRepository addressRepository,
                          ClientAddressRepository clientAddressRepository,
                          ClientRepository clientRepository) {
        this.addressRepository = addressRepository;
        this.clientAddressRepository = clientAddressRepository;
        this.clientRepository = clientRepository;
    }

    public List<ClientAddress> findByClient(UUID clientId) {
        return clientAddressRepository.findByIdClientId(clientId);
    }

    @Transactional
    public ClientAddress add(UUID clientId, AddressRequest req) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.CLIENT_NOT_FOUND,
                        "Nie znaleziono klienta"
                ));

        if (req.isDefault()) {
            clearDefault(clientId);
        }

        Address address = new Address();
        applyRequest(address, req);
        address = addressRepository.save(address);

        ClientAddress link = new ClientAddress();
        link.setId(new ClientAddressId(clientId, address.getId()));
        link.setClient(client);
        link.setAddress(address);
        link.setName(req.name());
        link.setIsDefault(req.isDefault());

        return clientAddressRepository.save(link);
    }

    @Transactional
    public void remove(UUID clientId, UUID addressId) {
        ClientAddress link = getAddress(clientId, addressId);
        clientAddressRepository.delete(link);
        addressRepository.deleteById(addressId);
    }

    @Transactional
    public ClientAddress update(UUID clientId, UUID addressId, AddressRequest req) {
        ClientAddress link = getAddress(clientId, addressId);
        applyRequest(link.getAddress(), req);
        addressRepository.save(link.getAddress());
        link.setName(req.name());
        if (req.isDefault()) {
            clearDefault(clientId);
        }
        link.setIsDefault(req.isDefault());
        return clientAddressRepository.save(link);
    }

    @Transactional
    public void setDefault(UUID clientId, UUID addressId) {
        ClientAddress link = getAddress(clientId, addressId);
        clearDefault(clientId);
        link.setIsDefault(true);
        clientAddressRepository.save(link);
    }

    private void clearDefault(UUID clientId) {
        clientAddressRepository.findByIdClientId(clientId).forEach(link -> {
            if (Boolean.TRUE.equals(link.getIsDefault())) {
                link.setIsDefault(false);
                clientAddressRepository.save(link);
            }
        });
    }

    private void applyRequest(Address address, AddressRequest req) {
        address.setCity(req.city());
        address.setRegion(req.region());
        address.setPostalCode(req.postalCode());
        address.setStreetNumber(req.streetNumber());
        address.setFlat(req.flat());
    }

    private ClientAddress getAddress(UUID clientId, UUID addressId) {
        return clientAddressRepository.findById(new ClientAddressId(clientId, addressId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        ApiErrorCode.ADDRESS_NOT_FOUND,
                        "Nie znaleziono adresu"
                ));
    }
}
