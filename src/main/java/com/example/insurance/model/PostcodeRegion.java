package com.example.insurance.model;

import jakarta.persistence.*;

@Entity
@Table(name = "postcode_regions", indexes = { @Index(name = "idx_postcode", columnList = "postcode") })
public class PostcodeRegion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** REGION1 (state) */
    private String state;

    /** POSTLEITZAHL (postal code) */
    private String postcode;

    public Long getId() { return id; }
    public String getState() { return state; }
    public String getPostcode() { return postcode; }
    public void setId(Long id) { this.id = id; }
    public void setState(String state) { this.state = state; }
    public void setPostcode(String postcode) { this.postcode = postcode; }
}
