package com.ing.brokerage;

import com.ing.brokerage.model.Asset;
import com.ing.brokerage.model.AssetDto;
import com.ing.brokerage.model.Customer;
import com.ing.brokerage.repository.AssetRepository;
import com.ing.brokerage.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceTest {

    private AssetRepository assetRepository;
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetRepository = mock(AssetRepository.class);
        assetService = new AssetService(assetRepository);
    }

    @Test
    void createAsset_ShouldSaveAsset() {
        Asset asset = new Asset();
        asset.setAssetName("USD");

        when(assetRepository.save(asset)).thenReturn(asset);

        Asset result = assetService.createAsset(asset);

        assertNotNull(result);
        assertEquals("USD", result.getAssetName());
        verify(assetRepository, times(1)).save(asset);
    }

    @Test
    void getAssetsByCustomer_ShouldReturnDtoList() {
        Customer customer = new Customer();
        customer.setId(1L);

        Asset asset1 = new Asset();
        asset1.setId(100L);
        asset1.setAssetName("USD");
        asset1.setSize(50.0);
        asset1.setUsableSize(40.0);
        asset1.setCustomer(customer);

        when(assetRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(asset1));

        List<AssetDto> result = assetService.getAssetsByCustomer(1L);

        assertEquals(1, result.size());
        assertEquals("USD", result.get(0).getAssetName());
        assertEquals(50.0, result.get(0).getSize());
        verify(assetRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void getAsset_ShouldReturnAsset_WhenExists() {
        Asset asset = new Asset();
        asset.setAssetName("USD");

        when(assetRepository.findByCustomerIdAndAssetName(1L, "USD"))
                .thenReturn(Optional.of(asset));

        Asset result = assetService.getAsset(1L, "USD");

        assertNotNull(result);
        assertEquals("USD", result.getAssetName());
    }

    @Test
    void getAsset_ShouldReturnNull_WhenNotExists() {
        when(assetRepository.findByCustomerIdAndAssetName(1L, "EUR"))
                .thenReturn(Optional.empty());

        Asset result = assetService.getAsset(1L, "EUR");

        assertNull(result);
    }

    @Test
    void updateUsableSize_ShouldUpdateAndSave() {
        Customer customer = new Customer();
        customer.setId(1L);

        Asset asset = new Asset();
        asset.setAssetName("USD");
        asset.setUsableSize(10.0);
        asset.setSize(10.0);
        asset.setCustomer(customer);

        when(assetRepository.findByCustomerIdAndAssetName(1L, "USD"))
                .thenReturn(Optional.of(asset));

        assetService.updateUsableSize(1L, "USD", 5.0);

        ArgumentCaptor<Asset> captor = ArgumentCaptor.forClass(Asset.class);
        verify(assetRepository, times(1)).save(captor.capture());

        Asset savedAsset = captor.getValue();
        assertEquals(15.0, savedAsset.getUsableSize());
        assertEquals(15.0, savedAsset.getSize());
    }

    @Test
    void updateUsableSize_ShouldDelete_WhenSizeBecomesZero() {
        Customer customer = new Customer();
        customer.setId(1L);

        Asset asset = new Asset();
        asset.setAssetName("USD");
        asset.setUsableSize(5.0);
        asset.setSize(5.0);
        asset.setCustomer(customer);

        when(assetRepository.findByCustomerIdAndAssetName(1L, "USD"))
                .thenReturn(Optional.of(asset));

        assetService.updateUsableSize(1L, "USD", -5.0);

        verify(assetRepository, times(1)).delete(asset);
        verify(assetRepository, never()).save(any());
    }
}
