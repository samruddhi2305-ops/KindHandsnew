package com.kindhands.app.model;

public class Organization {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String contact;
    private String type;
    private String address;
    private String pincode;
    private String documentPath;
    private String status;
    private boolean publicProfile; // ✅ Added to track privacy preference

    public Organization() {}

    public Organization(String name, String email, String password,
                        String contact, String type, String address,
                        String pincode, String documentPath) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.contact = contact;
        this.type = type;
        this.address = address;
        this.pincode = pincode;
        this.documentPath = documentPath;
        this.publicProfile = false;
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isPublicProfile() { return publicProfile; }
    public void setPublicProfile(boolean publicProfile) { this.publicProfile = publicProfile; }
}
