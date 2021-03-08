package net.glasslauncher.legacy.jsontemplate;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import net.glasslauncher.common.JsonConfig;

@Getter @Setter
public class LauncherConfig extends JsonConfig {

    @SerializedName("lastusedinstance")
    private String lastUsedInstance = "";

    @SerializedName("lastusedemail")
    private String lastUsedEmail = "";

    @SerializedName("clienttoken")
    private String clientToken = "";

    @SerializedName("ismstoken")
    private boolean isMSToken = false;

    @SerializedName("ishidingmsbutton")
    private boolean isHidingMSButton = false;

    @SerializedName("isthemedisabled")
    private boolean isThemeDisabled = false;

    @SerializedName("logininfo")
    private LoginInfo loginInfo = null;

    public LauncherConfig(String path) {
        super(path);
    }

    public void setLoginInfo(LoginInfo loginInfo) {
        setLoginInfo(loginInfo, false);
    }

    public void setLoginInfo(LoginInfo loginInfo, boolean isMSToken) {
        this.isMSToken = isMSToken;
        this.loginInfo = loginInfo;
        saveFile();
    }
}
