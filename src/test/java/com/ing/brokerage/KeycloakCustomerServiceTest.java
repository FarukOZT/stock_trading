package com.ing.brokerage;

import com.ing.brokerage.model.Asset;
import com.ing.brokerage.model.Customer;
import com.ing.brokerage.repository.CustomerRepository;
import com.ing.brokerage.service.AssetService;
import com.ing.brokerage.service.KeycloakCustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KeycloakCustomerServiceTest {

    private KeycloakCustomerService keycloakCustomerService;
    private Keycloak keycloak;
    private CustomerRepository customerRepository;
    private AssetService assetService;

    private RealmResource realmResource;
    private UsersResource usersResource;
    private UserResource userResource;
    private RolesResource rolesResource;

    @BeforeEach
    void setUp() {
        keycloak = mock(Keycloak.class);
        customerRepository = mock(CustomerRepository.class);
        assetService = mock(AssetService.class);

        realmResource = mock(RealmResource.class);
        usersResource = mock(UsersResource.class);
        userResource = mock(UserResource.class);
        rolesResource = mock(RolesResource.class);

        when(keycloak.realm("master")).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(anyString())).thenReturn(userResource);
        when(realmResource.roles()).thenReturn(rolesResource);

        keycloakCustomerService = new KeycloakCustomerService(keycloak, customerRepository, assetService);
    }

    @Test
    void createCustomerInKeycloak_successful() {
        Customer customer = new Customer();
        customer.setUsername("faruk");
        customer.setEmail("faruk@gmail.com");
        customer.setPassword("pass123");

        Response response = mock(Response.class);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getLocation()).thenReturn(URI.create("http://keycloak/users/123"));

        RoleRepresentation roleRepresentation = new RoleRepresentation();
        roleRepresentation.setName("customer");
        when(rolesResource.get("customer").toRepresentation()).thenReturn(roleRepresentation);

        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArgument(0));
        when(assetService.createAsset(any(Asset.class))).thenAnswer(i -> i.getArgument(0));

        Customer result = keycloakCustomerService.createCustomerInKeycloak(customer);

        assertNotNull(result);
        assertEquals("customer", result.getRole());
        assertEquals("123", result.getKeycloakId());

        verify(customerRepository, times(1)).save(any(Customer.class));
        verify(assetService, times(1)).createAsset(any(Asset.class));
    }

    @Test
    void createCustomerInKeycloak_failure() {
        Customer customer = new Customer();
        customer.setUsername("testuser");

        Response response = mock(Response.class);
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                keycloakCustomerService.createCustomerInKeycloak(customer)
        );

        assertTrue(ex.getMessage().contains("Keycloak user creation failed"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getCustomer_found() {
        Customer customer = new Customer();
        customer.setKeycloakId("abc123");
        when(customerRepository.findByKeycloakId("abc123")).thenReturn(Optional.of(customer));

        Optional<Customer> result = keycloakCustomerService.getCustomer("abc123");

        assertTrue(result.isPresent());
        assertEquals("abc123", result.get().getKeycloakId());
    }

    @Test
    void getCustomer_notFound() {
        when(customerRepository.findByKeycloakId("notfound")).thenReturn(Optional.empty());

        Optional<Customer> result = keycloakCustomerService.getCustomer("notfound");

        assertFalse(result.isPresent());
    }
}
