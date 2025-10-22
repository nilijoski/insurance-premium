package com.example.insurance.util;

import com.example.insurance.model.Application;
import com.example.insurance.model.PostcodeRegion;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/** Singleton SessionFactory builder. */
public class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY = build();

    private static SessionFactory build() {
        Configuration cfg = new Configuration();
        cfg.configure("hibernate.cfg.xml");
        cfg.addAnnotatedClass(Application.class);
        cfg.addAnnotatedClass(PostcodeRegion.class);
        return cfg.buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() { return SESSION_FACTORY; }
}
