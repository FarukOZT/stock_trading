package com.ing.brokerage.model.dto;

import lombok.Data;

@Data
public class AssetDto {
    private Long id;
    private String assetName;
    private double size;
    private double usableSize;
    private Long customerId;
}
