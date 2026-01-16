package com.kindhands.app.model;

public class User {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String mobile; // Matches 'mobile' column in DB
    private String address; // Matches 'address' column in DB
    private String pincode; // Matches 'pincode' column in DB
    private String gender; // Matches 'gender' column in DB
    private String role; // "DONOR"

    // Empty Constructor
    public User() {}

    // Constructor for Registration (Matches DB fields)
    public User(String name, String email, String password, String mobile, String address, String pincode, String gender, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
        this.address = address;
        this.pincode = pincode;
        this.gender = gender;
        this.role = role;
    }

    // Constructor for Login
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
