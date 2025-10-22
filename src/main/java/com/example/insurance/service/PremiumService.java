package com.example.insurance.service;

import com.example.insurance.dao.PostcodeRegionDAO;
import com.example.insurance.model.Application;
import com.example.insurance.model.PostcodeRegion;

import java.util.Map;

/** Premium = kmFactor * vehicleFactor * stateFactor */
public class PremiumService {
    private final PostcodeRegionDAO postcodeRegionDAO = new PostcodeRegionDAO();

    private static final Map<String, Double> STATE_FACTORS = Map.ofEntries(
            Map.entry("Baden-Württemberg", 1.05),
            Map.entry("Bayern", 1.10),
            Map.entry("Nordrhein-Westfalen", 1.15),
            Map.entry("Berlin", 1.20),
            Map.entry("Brandenburg", 1.25),
            Map.entry("Bremen", 1.30),
            Map.entry("Hamburg", 1.35),
            Map.entry("Hessen", 1.40),
            Map.entry("Mecklenburg-Vorpommern", 1.50),
            Map.entry("Niedersachsen", 1.55),
            Map.entry("Rheinland-Pfalz", 1.60),
            Map.entry("Saarland", 1.65),
            Map.entry("Sachsen", 1.70),
            Map.entry("Sachsen-Anhalt", 1.80),
            Map.entry("Schleswig-Holstein", 1.85),
            Map.entry("Thüringen", 1.90)
    );

    public Result calculate(Application in) {
        double km = kilometerFactor(in.getKilometers());
        double veh = vehicleFactor(in.getVehicleType());
        String state = resolveState(in.getPostcode());
        double reg = STATE_FACTORS.getOrDefault(state, 1.0);
        double premium = km * veh * reg;
        return new Result(premium, state, km, veh, reg);
    }

    public double kilometerFactor(int km) {
        if (km <= 5000) return 0.5;
        if (km <= 10000) return 1.0;
        if (km <= 20000) return 1.5;
        return 2.0;
    }

    public double vehicleFactor(String type) {
        if (type == null) return 1.0;
        return switch (type.toLowerCase()) {
            case "suv" -> 1.2;
            case "sports", "sportscar" -> 1.5;
            case "compact", "small" -> 0.8;
            case "sedan" -> 1.0;
            default -> 1.0;
        };
    }

    public String resolveState(String postcode) {
        if (postcode == null || postcode.isBlank()) return "";
        PostcodeRegion pr = postcodeRegionDAO.findByPostcode(postcode.trim());
        return pr != null ? pr.getState() : "";
    }

    /** Immutable result record */
    public record Result(double premium, String state, double kmFactor, double vehicleFactor, double stateFactor) {}
}
