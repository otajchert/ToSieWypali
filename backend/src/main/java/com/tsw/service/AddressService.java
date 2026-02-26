package com.tsw.service;

import com.tsw.dto.AddressRequest;
import com.tsw.model.Address;
import com.tsw.model.Client;
import com.tsw.model.ClientAddress;
import com.tsw.model.ClientAddressId;
import com.tsw.repository.AddressRepository;
import com.tsw.repository.ClientAddressRepository;
import com.tsw.repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

    public ClientAddress add(UUID clientId, AddressRequest req) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        // if this is marked as default, unset the current default first
        if (req.isDefault()) {
            clearDefault(clientId);
        }

        Address address = new Address();
        applyRequest(address, req);
        address = addressRepository.save(address);

        ClientAddress link = new ClientAddress();
        ClientAddressId linkId = new ClientAddressId();
        linkId.setClientId(clientId);
        linkId.setAddressId(address.getId());
        link.setId(linkId);
        link.setClient(client);
        link.setAddress(address);
        link.setName(req.getName());
        link.setIsDefault(req.isDefault());

        return clientAddressRepository.save(link);
    }

    public boolean remove(UUID clientId, UUID addressId) {
        ClientAddressId linkId = new ClientAddressId();
        linkId.setClientId(clientId);
        linkId.setAddressId(addressId);

        return clientAddressRepository.findById(linkId).map(link -> {
            clientAddressRepository.delete(link);
            addressRepository.deleteById(addressId);
            return true;
        }).orElse(false);
    }

    public boolean setDefault(UUID clientId, UUID addressId) {
        clearDefault(clientId);

        ClientAddressId linkId = new ClientAddressId();
        linkId.setClientId(clientId);
        linkId.setAddressId(addressId);

        return clientAddressRepository.findById(linkId).map(link -> {
            link.setIsDefault(true);
            clientAddressRepository.save(link);
            return true;
        }).orElse(false);
    }

    private void clearDefault(UUID clientId) {
        clientAddressRepository.findByIdClientId(clientId).forEach(link -> {
            if (link.getIsDefault()) {
                link.setIsDefault(false);
                clientAddressRepository.save(link);
            }
        });
    }

    private void applyRequest(Address address, AddressRequest req) {
        address.setCity(req.getCity());
        address.setRegion(req.getRegion());
        address.setPostalCode(req.getPostalCode());
        address.setStreetNumber(req.getStreetNumber());
        address.setFlat(req.getFlat());
    }
}
