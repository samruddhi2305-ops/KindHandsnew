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

    // 🔥 MUST match backend
    private String documentPath;

    private String status;
    private Long userId;

    // Empty constructor (IMPORTANT for Retrofit)
    public Organization() {}

    // Optional constructor
    public Organization(String name, String email, String password,
                        String contact, String type, String address,
                        String pincode, String documentPath, Long userId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.contact = contact;
        this.type = type;
        this.address = address;
        this.pincode = pincode;
        this.userId = userId;
        this.documentPath = documentPath;
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

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
}
