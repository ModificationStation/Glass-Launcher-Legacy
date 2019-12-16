package net.glasslauncher.jsontemplate;


import lombok.Getter;

@Getter
public class LoginResponse {
    private String accessToken;
    private String error;
    private LoginResponseAgent selectedProfile;
}
