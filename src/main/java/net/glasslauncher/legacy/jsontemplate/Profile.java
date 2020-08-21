package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

@Getter
public class Profile {
    private String id;
    private String name;
    private ProfileProperties[] properties;
}
