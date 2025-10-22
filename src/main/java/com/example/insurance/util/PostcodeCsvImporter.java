package com.example.insurance.util;

import com.example.insurance.model.PostcodeRegion;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.opencsv.CSVReaderHeaderAware;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility to import postcodes and states from postcodes.csv into the database.
 */
public class PostcodeCsvImporter {

    /**
     * Import postcodes from CSV if the table is empty.
     * @param resourcePath path to the CSV resource (e.g. "/postcodes.csv")
     */
    public static void importPostcodesIfEmpty(String resourcePath) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            long count = session.createQuery("select count(p) from PostcodeRegion p", Long.class)
                    .uniqueResult();
            if (count > 0) {
                return; // already imported
            }
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();

            InputStream is = PostcodeCsvImporter.class.getResourceAsStream(resourcePath);
            if (is == null) {
                throw new IllegalStateException("CSV not found on classpath: " + resourcePath);
            }

            try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {

                Map<String, String> row;
                while ((row = reader.readMap()) != null) {
                    if (row.isEmpty()) continue;

                    Map<String, String> norm = new HashMap<>();
                    for (Map.Entry<String, String> e : row.entrySet()) {
                        if (e.getKey() == null) continue;
                        String key = stripBom(e.getKey()).trim().toUpperCase(Locale.ROOT);
                        norm.put(key, e.getValue());
                    }

                    String postcode = trimQuotes(norm.getOrDefault("POSTLEITZAHL", ""));
                    String state    = trimQuotes(norm.getOrDefault("REGION1", ""));

                    if (postcode == null || postcode.isBlank() ||
                            state == null || state.isBlank()) {
                        continue;
                    }

                    PostcodeRegion pr = new PostcodeRegion();
                    pr.setPostcode(postcode);
                    pr.setState(state);
                    session.persist(pr);
                }
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error importing CSV", e);
        }
    }

    private static String trimQuotes(String s) {
        if (s == null) return null;
        return s.replace("\"", "").trim();
    }

    private static String stripBom(String s) {
        if (s == null) return null;
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }
}
