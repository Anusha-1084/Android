package com.astinil.AndroidTimesheet.api.model;

public class SignupRequest {

    public String firstName;
    public String lastName;
    public String username;
    public String password;
    public String email;
    public String mobile_no;     // ✅ Snake_case to match backend
    public String bio;
    public String location;
    public String profileImage;

    public SignupRequest(String firstName, String lastName, String username,
                         String password, String email, String mobile_no,
                         String bio, String location, String profileImage) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.mobile_no = mobile_no;   // ✅ match backend getMobile_no()
        this.bio = bio;
        this.location = location;
        this.profileImage = profileImage;
    }
}
