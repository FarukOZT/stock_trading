package com.ing.brokerage.service;

import com.ing.brokerage.model.Asset;
import com.ing.brokerage.model.dto.AssetDto;
import com.ing.brokerage.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;

    public Asset createAsset(Asset asset) {
        return assetRepository.save(asset);
    }

    public List<AssetDto> getAssetsByCustomer(Long customerId) {
        List<Asset> assets = assetRepository.findByCustomerId(customerId);

        return assets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());    }

    public Asset getAsset(Long customerId, String assetName) {
        return assetRepository.findByCustomerIdAndAssetName(customerId, assetName)
                .orElse(null);
    }

    public void updateUsableSize(Long customerId, String assetName, double delta) {
        Optional<Asset> assetOpt = assetRepository.findByCustomerIdAndAssetName(customerId, assetName);
        if (assetOpt.isPresent()) {
            Asset asset = assetOpt.get();
            asset.setUsableSize(asset.getUsableSize() + delta);
            asset.setSize(asset.getSize() + delta);
            if(asset.getSize() == 0){
                assetRepository.delete(asset);
            } else {
                assetRepository.save(asset);
            }
        }
    }

    private AssetDto convertToDto(Asset asset) {
        AssetDto dto = new AssetDto();
        dto.setId(asset.getId());
        dto.setAssetName(asset.getAssetName());
        dto.setSize(asset.getSize());
        dto.setUsableSize(asset.getUsableSize());
        dto.setCustomerId(asset.getCustomer().getId());
        return dto;
    }
}

