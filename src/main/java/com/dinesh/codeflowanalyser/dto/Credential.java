package com.dinesh.codeflowanalyser.dto;

public class Credential {
    private String user;
    private String password;

    public Credential(String use, String password) {
        this.user = use;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
