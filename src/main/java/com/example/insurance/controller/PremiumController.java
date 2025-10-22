package com.example.insurance.controller;

import com.example.insurance.model.Application;
import com.example.insurance.model.dto.CalculationResponse;
import com.example.insurance.model.dto.ErrorResponse;
import com.example.insurance.service.ApplicationService;
import com.example.insurance.service.PremiumService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.*;
import java.io.IOException;

/** POST /calculate — body: {kilometers, vehicleType, postcode} */
public class PremiumController extends HttpServlet {
    private final PremiumService premiumService = new PremiumService();
    private final ApplicationService applicationService = new ApplicationService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Application input = mapper.readValue(req.getInputStream(), Application.class);

        // Validate that postcode resolves to a known state before any calculation
        String state = premiumService.resolveState(input.getPostcode());
        if (state == null || state.isBlank()) {
            sendError(resp,
                    "Bundesland zur angegebenen Postleitzahl wurde nicht gefunden. Bitte Postleitzahl prüfen.");
            return;
        }

        var result = premiumService.calculate(input); // includes factors + premium

        // Persist only if a state has been found by the given Postcode
        Application toSave = new Application();
        toSave.setKilometers(input.getKilometers());
        toSave.setVehicleType(input.getVehicleType());
        toSave.setPostcode(input.getPostcode());
        toSave.setPremium(result.premium());
        applicationService.saveApplication(toSave);

        CalculationResponse out = new CalculationResponse(
                result.premium(),
                result.state(),
                result.kmFactor(),
                result.vehicleFactor(),
                result.stateFactor(),
                input.getKilometers(),
                input.getVehicleType(),
                input.getPostcode()
        );

        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);
        mapper.writeValue(resp.getWriter(), out);
    }

    private void sendError(HttpServletResponse resp, String message) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        mapper.writeValue(resp.getWriter(), new ErrorResponse(message));
    }
}
