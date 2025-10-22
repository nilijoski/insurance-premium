package com.example.insurance.controller;

import com.example.insurance.dao.PostcodeRegionDAO;
import com.example.insurance.model.PostcodeRegion;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** GET /resolve-postcode?pc=50667 -> { postcode, state, stateFactor, found } */
public class ResolvePostcodeController extends HttpServlet {
    private final PostcodeRegionDAO dao = new PostcodeRegionDAO();
    private final ObjectMapper mapper = new ObjectMapper();

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pc = req.getParameter("pc");
        Map<String, Object> out = new HashMap<>();
        out.put("postcode", pc == null ? "" : pc);

        if (pc == null || !pc.matches("\\d{5}")) {
            out.put("found", false);
            out.put("state", "");
            out.put("stateFactor", 1.0);
            send(resp, out, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        PostcodeRegion pr = dao.findByPostcode(pc.trim());
        if (pr == null) {
            out.put("found", false);
            out.put("state", "");
            out.put("stateFactor", 1.0);
            send(resp, out, HttpServletResponse.SC_OK);
            return;
        }

        String state = pr.getState();
        double factor = STATE_FACTORS.getOrDefault(state, 1.0);

        out.put("found", true);
        out.put("state", state);
        out.put("stateFactor", factor);
        send(resp, out, HttpServletResponse.SC_OK);
    }

    private void send(HttpServletResponse resp, Map<String, Object> body, int status) throws IOException {
        resp.setContentType("application/json");
        resp.setStatus(status);
        mapper.writeValue(resp.getWriter(), body);
    }
}
