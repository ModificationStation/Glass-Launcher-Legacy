package net.glasslauncher.legacy;

import gg.codie.mineonline.gui.MicrosoftLoginController;
import net.glasslauncher.common.CommonConfig;
import net.glasslauncher.common.FileUtils;
import net.glasslauncher.legacy.components.JPanelDirt;
import net.glasslauncher.legacy.components.MSLoginPanel;
import net.glasslauncher.legacy.components.MinecraftLogo;
import net.glasslauncher.legacy.components.templates.JButtonScaling;
import net.glasslauncher.legacy.jsontemplate.LoginInfo;
import net.glasslauncher.legacy.mc.Launcher;
import net.glasslauncher.legacy.util.LoginVerifier;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class MainWindow extends JFrame {
    private int orgWidth = 870;
    private int orgHeight = 520;

    private final Panel mainPanel = new Panel();
    private MSLoginPanel loginPanel;

    private JComboBox<String> instsel;

    /**
     * Sets up the main window object.
     * @param console The console for the launcher. Used to close the console.
     */
    public MainWindow(Frame console) {
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
                                  FileUtils.delete(new File(CommonConfig.getGlassPath() + "cache/repo-images"));
                                  System.exit(0);
                              }
                          }
        );

        makeGUI();
    }

    public void setHasToken(boolean hasToken) {
        loginPanel.setHasToken(hasToken);
    }

    public void setUsername(String username) {
        loginPanel.getUsernameField().setText(username);
    }

    private void makeGUI() {

        JScrollPane blogContainer = makeBlog();

        // Login form
        JPanelDirt loginForm = new JPanelDirt();
        loginForm.setLayout(new BorderLayout());

        ActionListener msListener = (e) -> {
            if (LoginVerifier.verifyLogin(this, true)) {
                loginPanel.getUsernameField().setText(Config.getLauncherConfig().getLoginInfo().getUsername());
                loginPanel.setHasToken(true);
                startMinecraft();
            }
        };

        loginPanel = new MSLoginPanel(msListener);

        // Logo
        MinecraftLogo logo = new MinecraftLogo();

        // Auto selection of username or password.
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e){
                if (Config.getLauncherConfig().getLoginInfo() == null) {
                    loginPanel.getLoginButton().grabFocus();
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

        if (instsel.getSelectedItem() != null) {
            Config.getLauncherConfig().setLastUsedInstance(instsel.getSelectedItem().toString());
        }

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
        File file = new File(CommonConfig.getGlassPath() + "instances");
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
        if (instsel.getSelectedItem() == null || instsel.getSelectedItem().toString().equalsIgnoreCase("none")) {
            Main.LOGGER.severe("Selected instance is null or empty! Aborting launch.");
            return;
        }

        if (!Objects.equals(System.getProperty("sun.arch.data.model"), "64")) {
            if(!Config.getLauncherConfig().isHidingJavaWarning()) {
                JOptionPane.showMessageDialog(null, "Detected using " + System.getProperty("sun.arch.data.model") + ". If you're planning on using more than 1GB of RAM in your instances, you should use the x64 version of Java!");
            }
        }

        Config.getLauncherConfig().setLastUsedUsername(Config.getLauncherConfig().getLoginInfo().getUsername());
        Config.getLauncherConfig().setLastUsedInstance(instsel.getSelectedItem().toString());
        Config.getLauncherConfig().saveFile();
        Launcher mc = new Launcher();
        mc.startMC();
        LoginInfo loginInfo = Config.getLauncherConfig().getLoginInfo();
        if (loginInfo != null && MicrosoftLoginController.validateToken(loginInfo.getAccessToken())) {
            loginPanel.setHasToken(false);
            Config.getLauncherConfig().setLoginInfo(null);
        }
    }
}
