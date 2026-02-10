package com.kindhands.app.model;

public class OrganizationLoginRequest {
    private Long userId;
    private String password;

    public OrganizationLoginRequest(Long userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
