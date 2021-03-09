package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

import java.util.*;

@Getter
public class MCVersions {
    private Map<String, MCVersion> client;
    private Map<String, MCVersion> mappings;
}
