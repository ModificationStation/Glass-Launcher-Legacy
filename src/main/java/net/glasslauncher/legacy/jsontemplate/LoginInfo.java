package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

@Getter
public class LoginInfo {
    private final String username;
    private final String accessToken;
    private final String uuid;

    public LoginInfo(String username, String accessToken, String uuid) {
        this.username = username;
        this.accessToken = accessToken;
        this.uuid = uuid;
    }
}
