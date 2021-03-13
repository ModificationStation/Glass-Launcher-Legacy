package net.glasslauncher.legacy;

import net.chris54721.openmcauthenticator.OpenMCAuthenticator;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.components.JPanelDirt;
import net.glasslauncher.legacy.components.LoginPanel;
import net.glasslauncher.legacy.components.MinecraftLogo;
import net.glasslauncher.legacy.components.templates.JButtonScaling;
import net.glasslauncher.legacy.jsontemplate.LoginInfo;
import net.glasslauncher.legacy.mc.Wrapper;
import net.glasslauncher.legacy.util.MSLoginHandler;
import net.glasslauncher.legacy.util.MojangLoginHandler;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

class MainWindow extends JFrame {
    private int orgWidth = 854;
    private int orgHeight = 480;

    private final Panel mainPanel = new Panel();
    private LoginPanel loginPanel;

    private JComboBox<String> instsel;

    /**
     * Sets up the main window object.
     * @param console The console for the launcher. Used to close the console.
     */
    MainWindow(Frame console) {
        // Setting the size, icon, location and layout of the launcher
        Insets insets = getInsets();
        Main.LOGGER.info("Starting...");
        setTitle("Glass Launcher " + Config.VERSION);
        setIconImage(Toolkit.getDefaultToolkit().createImage(MainWindow.class.getResource("assets/glass.png")));
        setLayout(new GridLayout(1, 1));
        pack();
        setPreferredSize(new Dimension(orgWidth + insets.left + insets.right, orgHeight + insets.top + insets.bottom));
        setMinimumSize(new Dimension(650, 400));

        // Container to make my brain hurt less
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        // Makes it so the launcher closes when you press the close button
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                  if (console != null) {
                                      console.dispose();
                                  }
                                  dispose();
                                  FileUtils.delete(new File(CommonConfig.GLASS_PATH + "cache/repo-images"));
                                  System.exit(0);
                              }
                          }
        );

        makeGUI();
    }

    private void makeGUI() {

        JScrollPane blogContainer = makeBlog();

        // Login form
        JPanelDirt loginForm = new JPanelDirt();
        loginForm.setLayout(new BorderLayout());

        ActionListener mojangListener = (e) -> {
            startMinecraft();
        };

        ActionListener msListener = (e) -> {
            (new MSLoginHandler(Main.mainwin)).login();
            if (Config.getLauncherConfig().getLoginInfo() != null) {
                loginPanel.getUsername().setText(Config.getLauncherConfig().getLoginInfo().getUsername());
                loginPanel.setHasToken(true);
            }
        };

        loginPanel = new LoginPanel(mojangListener, msListener);

        // Logo
        MinecraftLogo logo = new MinecraftLogo();

        // Auto selection of username or password.
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e){
                if (Config.getLauncherConfig().getLoginInfo() == null) {
                    if (loginPanel.getUsername().getText().isEmpty()) {
                        loginPanel.getUsername().grabFocus();
                    } else {
                        loginPanel.getPassword().grabFocus();
                    }
                }
            }
        });

        // Instance selector
        instsel = new JComboBox<>();
        refreshInstanceList();
        instsel.setBounds(24, 66, 166, 22);

        // Options button
        JButtonScaling options = new JButtonScaling();
        options.setText("Options");
        options.setBounds(192, 14, 70, 22);
        options.setOpaque(false);
        options.addActionListener(event -> new OptionsWindow(this, (String) instsel.getSelectedItem()));

        // Instance manager button
        JButtonScaling instancesButton = new JButtonScaling();
        instancesButton.setText("Instances");
        instancesButton.setBounds(192, 66, 70, 22);
        instancesButton.setOpaque(false);
        instancesButton.setMargin(new Insets(0, 0, 0, 0));
        instancesButton.addActionListener(event -> {
            new InstanceManagerWindow(this);
            refreshInstanceList();
        });

        // Adding widgets and making the launcher visible
        loginForm.add(logo, BorderLayout.WEST);
        loginForm.add(loginPanel, BorderLayout.EAST);
        loginPanel.add(instsel);
        loginPanel.add(options);
        loginPanel.add(instancesButton);

        mainPanel.add(blogContainer, BorderLayout.CENTER);
        mainPanel.add(loginForm, BorderLayout.SOUTH);

        loginPanel.setHasToken(verifyLogin(false));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JScrollPane makeBlog() {
        String page = new Scanner(MainWindow.class.getResourceAsStream("assets/blog.html"), "UTF-8").useDelimiter("\\A").next();
        page = page.replaceAll("\\$\\{root}\\$", MainWindow.class.getResource("assets/").toString());
        JTextPane blog = new JTextPane();
        blog.setContentType("text/html");
        blog.setText(page);
        blog.setBorder(BorderFactory.createEmptyBorder());
        blog.setEditable(false);
        blog.addHyperlinkListener(event -> {
            if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                try {
                    Desktop.getDesktop().browse(event.getURL().toURI());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        JScrollPane blogcontainer = new JScrollPane(blog);
        blogcontainer.setBorder(BorderFactory.createEmptyBorder());
        blogcontainer.setBounds(new Rectangle(0, 0, mainPanel.getWidth(), mainPanel.getHeight() - 200));
        return blogcontainer;
    }

    public void refreshInstanceList() {
        Main.LOGGER.info("Refreshing instance list...");
        instsel.setModel(new DefaultComboBoxModel<>());
        File file = new File(CommonConfig.GLASS_PATH + "instances");
        String[] instances = file.list((current, name) -> new File(current, name).isDirectory());
        String lastUsedInstance = Config.getLauncherConfig().getLastUsedInstance();
        boolean exists = false;
        if (instances != null) {
            for (String instance : instances) {
                if (instance.equals(lastUsedInstance)) {
                    exists = true;
                }
                instsel.addItem(instance);
            }
        }
        if (lastUsedInstance != null && !lastUsedInstance.isEmpty() && exists) {
            instsel.setSelectedItem(lastUsedInstance);
        }
    }

    private void startMinecraft() {
        loginPanel.setHasToken(true);
        Main.LOGGER.info("Starting instance: " + instsel.getSelectedItem());
        if (instsel.getSelectedItem() == null || instsel.getSelectedItem().toString().toLowerCase().equals("none")) {
            Main.LOGGER.severe("Selected instance is null or empty! Aborting launch.");
            return;
        }
        if (!verifyLogin(true)) {
            loginPanel.setHasToken(false);
            Main.LOGGER.severe("Aborting launch.");
            return;
        }
        Config.getLauncherConfig().setLastUsedEmail(loginPanel.getUsername().getText());
        Config.getLauncherConfig().setLastUsedInstance(instsel.getSelectedItem().toString());
        Config.getLauncherConfig().saveFile();
        Wrapper mc = new Wrapper();
        mc.startMC();
        if (Config.getLauncherConfig().getLoginInfo().getAccessToken().isEmpty()) {
            loginPanel.setHasToken(false);
            Config.getLauncherConfig().setLoginInfo(null);
        }
    }

    private boolean verifyLogin(boolean canOffline) {
        if (Config.getLauncherConfig().isMSToken()) {
            Main.LOGGER.info("Verifying stored MS auth token...");
            if (!(new MSLoginHandler(this)).verify()) {
                Main.LOGGER.severe("Unable to verify stored MS auth token.");
                Config.getLauncherConfig().setLoginInfo(null);
                return false;
            }
            loginPanel.getUsername().setText(Config.getLauncherConfig().getLoginInfo().getUsername());
            Main.LOGGER.info("MS auth token has been verified!");
        }
        else {
            if (Config.getLauncherConfig().getLoginInfo() != null) {
                Main.LOGGER.info("Verifying stored Mojang auth token...");
                try {
                    OpenMCAuthenticator.validate(Config.getLauncherConfig().getLoginInfo().getAccessToken(), Config.getLauncherConfig().getClientToken());
                    Main.LOGGER.info("Mojang auth token has been verified!");
                    return true;
                } catch (Exception e) {
                    Main.LOGGER.warning("Unable to verify stored Mojang auth token.");
                    Config.getLauncherConfig().setLoginInfo(null);
                    return false;
                }
            }
            String pass = "";
            if (loginPanel.getPassword().getForeground() != Color.gray) {
                pass = String.valueOf(loginPanel.getPassword().getPassword());
            }
            if (!pass.isEmpty()) {
                MojangLoginHandler.login(loginPanel.getUsername().getText(), pass);
                LoginInfo loginInfo = Config.getLauncherConfig().getLoginInfo();
                if (loginInfo == null) {
                    Main.LOGGER.severe("Unable to log in!");
                    return false;
                }
            }
            if (!loginPanel.getUsername().getText().isEmpty() && canOffline) {
                Config.getLauncherConfig().setLoginInfo(new LoginInfo(loginPanel.getUsername().getText(), ""));
            }
            return Config.getLauncherConfig().getLoginInfo() != null;
        }
        return true;
    }
}
