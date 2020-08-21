package net.glasslauncher.legacy.jsontemplate;

import lombok.Getter;

@Getter
public class MultiMCComponent {
    private String cachedName;
    private String cachedVersion;
    private String uid;
    private boolean dependencyOnly = false;
    private boolean important = false;
    private boolean disabled = false;
}
