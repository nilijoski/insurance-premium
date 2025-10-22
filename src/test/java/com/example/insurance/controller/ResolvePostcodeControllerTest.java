package com.example.insurance.controller;

import com.example.insurance.Main;
import com.example.insurance.util.PostcodeCsvImporter;
import com.example.insurance.util.HibernateUtil;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple integration-style test: starts Jetty on a random port (8080 assumed free),
 * ensures CSV is imported, and exercises /resolve-postcode.
 *
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResolvePostcodeControllerTest {

    private static boolean started = false;

    @BeforeAll
    static void boot() throws Exception {
        HibernateUtil.getSessionFactory();
        PostcodeCsvImporter.importPostcodesIfEmpty("/postcodes.csv");

        // Start Jetty in a thread
        new Thread(() -> {
            try {
                Main.main(new String[]{});
            } catch (Exception e) {
                // ignore if already started by another test
            }
        }).start();

        Thread.sleep(1200);
        started = true;
    }

    @Test
    @Order(1)
    void resolveKnownPostcode() throws Exception {
        assumeServer();
        HttpClient c = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/resolve-postcode?pc=50667"))
                .GET().build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"found\":true"));
        assertTrue(res.body().contains("\"state\""));
        assertTrue(res.body().contains("\"stateFactor\""));
    }

    @Test
    @Order(2)
    void resolveUnknownPostcode() throws Exception {
        assumeServer();
        HttpClient c = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/resolve-postcode?pc=00000"))
                .GET().build();
        HttpResponse<String> res = c.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"found\":false"));
    }

    private static void assumeServer() {
        assertTrue(started, "Server did not start");
    }
}
