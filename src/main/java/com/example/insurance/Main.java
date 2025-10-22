package com.example.insurance;

import com.example.insurance.controller.PremiumController;
import com.example.insurance.controller.ResolvePostcodeController;
import com.example.insurance.util.PostcodeCsvImporter;
import com.example.insurance.util.HibernateUtil;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.Objects;

/** Starts Jetty, imports CSV once, serves API and static UI. */
public class Main {
    public static void main(String[] args) throws Exception {
        // Init Hibernate and import CSV
        HibernateUtil.getSessionFactory();
        PostcodeCsvImporter.importPostcodesIfEmpty("/postcodes.csv");

        // Jetty
        Server server = new Server(8080);
        ServletContextHandler ctx = new ServletContextHandler(ServletContextHandler.SESSIONS);
        ctx.setContextPath("/");

        // Static UI (index.html)
        ServletHolder staticHolder = new ServletHolder("default", new DefaultServlet());
        staticHolder.setInitParameter("dirAllowed", "false");
        staticHolder.setInitParameter("pathInfoOnly", "true");
        staticHolder.setInitParameter("resourceBase",
                Objects.requireNonNull(Main.class.getResource("/public")).toExternalForm());
        ctx.addServlet(staticHolder, "/*");

        // API
        ctx.addServlet(new ServletHolder(new PremiumController()), "/calculate");
        ctx.addServlet(new ServletHolder(new ResolvePostcodeController()), "/resolve-postcode");

        server.setHandler(ctx);
        server.start();
        server.join();
    }
}
