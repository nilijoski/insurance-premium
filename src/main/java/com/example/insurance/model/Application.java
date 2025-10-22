package com.example.insurance.model;

import jakarta.persistence.*;

/** Persisted application inputs + computed premium. */
@Entity
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int kilometers;
    private String vehicleType;
    private String postcode;
    private Double premium;

    public Long getId() { return id; }
    public int getKilometers() { return kilometers; }
    public String getVehicleType() { return vehicleType; }
    public String getPostcode() { return postcode; }
    public Double getPremium() { return premium; }

    public void setId(Long id) { this.id = id; }
    public void setKilometers(int kilometers) { this.kilometers = kilometers; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public void setPostcode(String postcode) { this.postcode = postcode; }
    public void setPremium(Double premium) { this.premium = premium; }
}
