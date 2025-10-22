package com.example.insurance.service;

import com.example.insurance.model.Application;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PremiumServiceTest {

    private final PremiumService ps = new PremiumService();

    private Application mk(int km, String vehicle, String pc) {
        Application a = new Application();
        a.setKilometers(km);
        a.setVehicleType(vehicle);
        a.setPostcode(pc);
        return a;
    }

    @Test
    void kilometerFactorBoundaries() {
        assertEquals(0.5, ps.kilometerFactor(0), 1e-9);
        assertEquals(0.5, ps.kilometerFactor(5000), 1e-9);
        assertEquals(1.0, ps.kilometerFactor(5001), 1e-9);
        assertEquals(1.0, ps.kilometerFactor(10000), 1e-9);
        assertEquals(1.5, ps.kilometerFactor(10001), 1e-9);
        assertEquals(1.5, ps.kilometerFactor(20000), 1e-9);
        assertEquals(2.0, ps.kilometerFactor(20001), 1e-9);
    }

    @Test
    void vehicleFactorMapping() {
        assertEquals(1.2, ps.vehicleFactor("suv"), 1e-9);
        assertEquals(1.5, ps.vehicleFactor("sports"), 1e-9);
        assertEquals(0.8, ps.vehicleFactor("compact"), 1e-9);
        assertEquals(1.0, ps.vehicleFactor("sedan"), 1e-9);
        assertEquals(1.0, ps.vehicleFactor("unknown"), 1e-9);
        assertEquals(1.0, ps.vehicleFactor(null), 1e-9);
    }

    @Test
    void premiumMultiplicationLogic_noStateFound_defaults1() {
        Application a = mk(12000, "suv", "00000"); // unknown postcode in tests
        var result = ps.calculate(a);
        // km 1.5 * vehicle 1.2 * state 1.0 = 1.8
        assertEquals(1.8, result.premium(), 1e-9);
        assertEquals("", result.state());
        assertEquals(1.5, result.kmFactor(), 1e-9);
        assertEquals(1.2, result.vehicleFactor(), 1e-9);
        assertEquals(1.0, result.stateFactor(), 1e-9);
    }
}
