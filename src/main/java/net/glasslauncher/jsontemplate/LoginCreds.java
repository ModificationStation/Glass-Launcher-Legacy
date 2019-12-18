package net.glasslauncher.jsontemplate;

import lombok.Setter;

@Setter
public class LoginCreds {
    private String username;
    private String password;
    private LoginCredsAgent agent = new LoginCredsAgent();
}
