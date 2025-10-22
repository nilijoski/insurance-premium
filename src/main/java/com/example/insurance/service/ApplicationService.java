package com.example.insurance.service;

import com.example.insurance.dao.ApplicationDAO;
import com.example.insurance.model.Application;

public class ApplicationService {
    private final ApplicationDAO dao = new ApplicationDAO();
    public void saveApplication(Application app) { dao.save(app); }
}
