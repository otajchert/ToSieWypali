package com.tsw.repository;

import com.tsw.model.ClientAddress;
import com.tsw.model.ClientAddressId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClientAddressRepository extends JpaRepository<ClientAddress, ClientAddressId> {
    List<ClientAddress> findByIdClientId(UUID clientId);
}
