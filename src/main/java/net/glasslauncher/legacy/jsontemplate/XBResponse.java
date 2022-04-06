package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.*;

@Getter
public class XBResponse {

    @SerializedName("Token")
    private String accessToken;
    @SerializedName("DisplayClaims")
    private HashMap<String, Object> displayClaims = null;
    @SerializedName("XErr")
    private Long xErr = null;
    @SerializedName("Redirect")
    private String redirect = null;
}
