package net.glasslauncher.legacy;

import gg.codie.mineonline.gui.MicrosoftLoginController;
import lombok.Getter;
import net.glasslauncher.legacy.components.templates.JButtonScaling;
import net.glasslauncher.legacy.jsontemplate.LoginInfo;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

public class MSLoginWindow extends JDialog {

    @Getter private JButtonScaling continueButton;

    private JTextField textField;

    public MSLoginWindow(Window frame) {
        super(frame);

        LoginInfo loginInfo = Config.getLauncherConfig().getLoginInfo();
        if (loginInfo != null && (loginInfo.getUuid() == null || (MicrosoftLoginController.getError() == null && MicrosoftLoginController.validateToken(loginInfo.getAccessToken())))) {
            Main.mainwin.setHasToken(true);
            dispose(); // We aren't required after all.
            return;
        }

        setPreferredSize(new Dimension(300, 200));

        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle("Microsoft Login");

        ActionListener msListener = (e) -> {
            LoginInfo loginInfoInner = Config.getLauncherConfig().getLoginInfo();
            if (loginInfoInner != null && (loginInfoInner.getUuid() == null || (MicrosoftLoginController.getError() == null && MicrosoftLoginController.validateToken(loginInfoInner.getAccessToken())))) {
                Main.mainwin.setHasToken(true);
            }
            dispose();
        };

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JEditorPane info = new JEditorPane();
        info.setBorder(BorderFactory.createEmptyBorder());
        info.setEditable(false);
        info.setContentType("text/html");
        info.addHyperlinkListener(event -> {
            if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        textField = new JTextField();

        continueButton = new JButtonScaling();
        continueButton.setText("Continue");
        continueButton.addActionListener(msListener);
        continueButton.setEnabled(false);

        MicrosoftLoginController.loadDeviceCode(this);
        textField.setText(MicrosoftLoginController.getLoginCode());
        info.setText("<p style=\"font-family: sans-serif; margin-left: 5px; margin-right: 5px;\">In order to sign in, you need to visit <a href=\"" + MicrosoftLoginController.getVerificationUrl() + "\">" + MicrosoftLoginController.getVerificationUrl() + "</a> and enter the below code. The continue button should light up once you do so.</p>");

        panel.add(info);
        panel.add(textField);
        panel.add(continueButton);
        add(panel);
        pack();
        setLocationRelativeTo(frame);

        setVisible(true);
    }

    public void setErrorText(String text) {
        textField.setForeground(Color.RED);
        textField.setText(text);
    }
}
