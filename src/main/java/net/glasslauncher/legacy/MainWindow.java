package net.glasslauncher.legacy;

import net.glasslauncher.legacy.components.DirtPanel;
import net.glasslauncher.legacy.components.HintTextField;
import net.glasslauncher.legacy.components.Logo;
import net.glasslauncher.legacy.mc.LaunchArgs;
import net.glasslauncher.legacy.mc.Wrapper;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Scanner;

import static net.glasslauncher.legacy.Main.logger;

class MainWindow extends Frame {

    private int orgwidth = 854;
    private int orgheight = 480;

    /**
     * Sets up the main window object.
     * @// TODO: 20/09/2019 To be split into a proper class with multiple functions.
     * @param args Args passed from main. Unused.
     * @param console The console for the launcher. Used to close the console.
     */
    MainWindow(String[] args, Frame console) {
        // Setting the size, icon, location and layout of the launcher
        Insets insets = getInsets();
        logger.info("Starting...");
        setTitle("Glass Launcher " + Config.version);
        setIconImage(Toolkit.getDefaultToolkit().createImage(MainWindow.class.getResource("assets/glass.png")));
        setLayout(new GridLayout(1, 1));
        setLocationRelativeTo(null);
        pack();
        setPreferredSize(new Dimension(orgwidth + insets.left + insets.right, orgheight + insets.top + insets.bottom));
        setMinimumSize(new Dimension(650, 400));

        // Container to make my brain hurt less
        Panel panel = new Panel();
        panel.setLayout(new BorderLayout());
        add(panel);

        // Makes it so the launcher closes when you press the close button
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                  console.dispose();
                                  dispose();
                                  System.exit(0);
                              }
                          }
        );

        // Blog Window
        String page = new Scanner(MainWindow.class.getResourceAsStream("assets/blog.html"), "UTF-8").useDelimiter("\\A").next();
        page = page.replaceAll("\\$\\{root}\\$", MainWindow.class.getResource("assets/").toString());
        JTextPane blog = new JTextPane(

        );
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
        blogcontainer.setBounds(new Rectangle(0, 0, panel.getWidth(), panel.getHeight() - 200));

        // Login form
        DirtPanel loginform = new DirtPanel();
        loginform.setLayout(new BorderLayout());

        JPanel loginpanel = new JPanel();
        loginpanel.setLayout(null);
        loginpanel.setOpaque(false);
        loginpanel.setPreferredSize(new Dimension(255, 100));

        // Logo
        Logo logo = new Logo();

        // Username field
        HintTextField username = new HintTextField("Username or Email");
        username.setBounds(0, 14, 166, 22);

        // Password field
        JPasswordField password = new JPasswordField();
        password.setEchoChar((char) 0);
        password.setForeground(Color.gray);
        password.setText("Password");

        password.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                if (password.getForeground() == Color.gray) {
                    password.setText("");
                    password.setForeground(Color.black);
                    password.setEchoChar('•');
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ((String.valueOf(password.getPassword()).isEmpty())) {
                    password.setText("Password");
                    password.setForeground(Color.gray);
                    password.setEchoChar((char) 0);
                }
            }
        });

        password.setBounds(0, 40, 166, 22);

        // Instance selector
        JComboBox instsel = new JComboBox();
        File file = new File(Config.glasspath + "instances");
        String[] instances = file.list((current, name) -> new File(current, name).isDirectory());
        if (instances != null) {
            for (String instance : instances) {
                instsel.addItem(instance);
            }
        }
        instsel.setBounds(0, 66, 166, 22);

        // Options button
        JButton options = new JButton();
        options.setText("Options");
        options.setBounds(168, 14, 70, 22);
        options.setOpaque(false);
        options.addActionListener(event -> {
            new OptionsWindow(this, (String) instsel.getSelectedItem());
        });

        // Login button
        JButton login = new JButton();
        login.setText("Login");
        login.setBounds(168, 40, 70, 22);
        login.setOpaque(false);
        login.addActionListener(event -> {
            logger.info((String) instsel.getSelectedItem());
            String pass = "";
            if (password.getForeground() != Color.gray) {
                pass = String.valueOf(password.getPassword());
            }
            String[] launchargs = {username.getText(), pass, (String) instsel.getSelectedItem()};
            launchargs = (new LaunchArgs()).getArgs(launchargs);
            if (launchargs != null) {
                Wrapper mc = new Wrapper(launchargs);
                mc.startMC();
            } else {
                password.setText("");
            }
        });

        // Instance manager button
        JButton instancesButton = new JButton();
        instancesButton.setText("Instances");
        instancesButton.setBounds(168, 66, 70, 22);
        instancesButton.setOpaque(false);
        instancesButton.setMargin(new Insets(0, 0, 0, 0));
        instancesButton.addActionListener(event -> {
            new InstanceManagerWindow(this);
        });

        // Adding widgets and making the launcher visible
        loginform.add(logo, BorderLayout.WEST);
        loginform.add(loginpanel, BorderLayout.EAST);
        loginpanel.add(instsel);
        loginpanel.add(username);
        loginpanel.add(options);
        loginpanel.add(password);
        loginpanel.add(login);
        loginpanel.add(instancesButton);

        panel.add(blogcontainer, BorderLayout.CENTER);
        panel.add(loginform, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
