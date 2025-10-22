package com.example.insurance.dao;

import com.example.insurance.model.PostcodeRegion;
import com.example.insurance.util.HibernateUtil;
import org.hibernate.Session;

public class PostcodeRegionDAO {

    public PostcodeRegion findByPostcode(String postcode) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("from PostcodeRegion p where p.postcode = :pc", PostcodeRegion.class)
                    .setParameter("pc", postcode)
                    .setMaxResults(1)
                    .uniqueResult();
        }
    }

    public long countAll() {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            return s.createQuery("select count(p.id) from PostcodeRegion p", Long.class).uniqueResult();
        }
    }
}
