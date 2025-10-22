package com.example.insurance.dao;

import com.example.insurance.model.Application;
import com.example.insurance.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ApplicationDAO {
    public void save(Application app) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(app);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
