package com.example.insurance.model.dto;

/** API response with inputs, resolved state and factors. */
public record CalculationResponse(
        double premium,
        String state,
        double kilometerFactor,
        double vehicleFactor,
        double stateFactor,
        int inputKilometers,
        String inputVehicleType,
        String inputPostcode
) {}
