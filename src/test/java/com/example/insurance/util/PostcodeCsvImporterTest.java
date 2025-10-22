package com.example.insurance.util;

import com.example.insurance.dao.PostcodeRegionDAO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PostcodeCsvImporterTest {

    @Test
    void csvImportedAtLeastOneRow() {
        HibernateUtil.getSessionFactory();
        PostcodeCsvImporter.importPostcodesIfEmpty("/postcodes.csv");
        PostcodeRegionDAO dao = new PostcodeRegionDAO();
        assertTrue(dao.countAll() > 0, "CSV should import at least one postcode row");
    }
}
