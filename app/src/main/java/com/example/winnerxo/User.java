package com.example.winnerxo;

public class User {
    private String phonenumber;
    private String password;
    private boolean isOwner;
    public static User currentUser;
    public User() {
    }

    public User(String phonenumber, String password) {
        this.phonenumber = phonenumber;
        this.password = password;
        if (this.password.equals("12345678")&&this.phonenumber.equals("0584454112")){
            isOwner=true;
        }
        else {
            isOwner=false;
        }
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
