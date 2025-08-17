package com.ing.brokerage.service;

import com.ing.brokerage.model.Asset;
import com.ing.brokerage.model.Customer;
import com.ing.brokerage.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeycloakCustomerService {

    private final Keycloak keycloak;
    private final CustomerRepository customerRepository;
    private final AssetService assetService;

    public Customer createCustomerInKeycloak(Customer customer) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(customer.getUsername());
        user.setEmail(customer.getEmail());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setTemporary(false);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(customer.getPassword());

        user.setCredentials(Collections.singletonList(credential));

        List<String> roles = Collections.singletonList("customer");
        user.setRealmRoles(roles);

        Response response = keycloak.realm("master").users().create(user);

        if (response.getStatus() == 201) {

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
            customer.setKeycloakId(userId);
            RoleRepresentation role = keycloak.realm("master").roles()
                    .get("customer").toRepresentation();
            customer.setRole("customer");
            keycloak.realm("master").users().get(userId)
                    .roles().realmLevel().add(Collections.singletonList(role));
            customerRepository.save(customer);
            Asset asset = Asset.builder()
                    .assetName("TRY")
                    .size(100)
                    .usableSize(100)
                    .customer(customer)
                    .build();
            assetService.createAsset(asset);
            return customer;
        } else {
            throw new RuntimeException("Keycloak user creation failed: " + response.getStatus());
        }
    }

    public Optional<Customer> getCustomer(String keycloakId){
        return customerRepository.findByKeycloakId(keycloakId);
    }
}

