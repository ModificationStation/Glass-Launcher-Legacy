package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

@Getter
public class LoginInfo {
    private final String username;
    private final String accessToken;

    public LoginInfo(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }
}
