package com.ing.brokerage.controller;

import com.ing.brokerage.model.Asset;
import com.ing.brokerage.model.AssetDto;
import com.ing.brokerage.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    public Asset createAsset(@RequestBody Asset asset){
        return assetService.createAsset(asset);
    }
    @GetMapping
    public List<AssetDto> getAssets(@RequestParam Long customerId) {
        return assetService.getAssetsByCustomer(customerId);
    }
}

