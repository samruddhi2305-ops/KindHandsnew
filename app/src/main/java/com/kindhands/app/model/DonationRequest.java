package com.kindhands.app.model;

public class DonationRequest {
    // Matches the backend 'Request' entity logic
    // Add fields that your backend Request entity has.
    // Assuming standard fields based on your frontend form:
    
    private Long id;
    private String category;
    private String details; // Or description
    private int quantity;
    private String otherDetails;
    
    // Status fields from backend
    private String status; 
    private Long organizationId;
    private Long donorId;

    public DonationRequest() {}

    public DonationRequest(String category, String details, int quantity, String otherDetails) {
        this.category = category;
        this.details = details;
        this.quantity = quantity;
        this.otherDetails = otherDetails;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getOtherDetails() { return otherDetails; }
    public void setOtherDetails(String otherDetails) { this.otherDetails = otherDetails; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
}// PR test change by Samruddhi (date)

