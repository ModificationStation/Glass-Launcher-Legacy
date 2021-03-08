package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

@Getter
public class MSAccessToken {
    @SerializedName("access_token")
    private String accessToken;
}
