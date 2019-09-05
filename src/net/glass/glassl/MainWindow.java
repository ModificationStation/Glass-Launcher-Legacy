package net.glass.glassl;

import net.glass.glassw.Wrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Collection;
import java.util.Scanner;

import static net.glass.glassl.Main.logger;

class MainWindow extends Frame {

    private int orgwidth = 854;
    private int orgheight = 480;

    MainWindow(String[] args, Frame console) {
        // Setting the size, icon, location and layout of the launcher
        Insets insets = getInsets();
        logger.info("Starting...");
        setTitle("Launcher");
        setIconImage(Toolkit.getDefaultToolkit().createImage(MainWindow.class.getResource("assets/glass.png")));
        setLayout(new GridLayout(1, 1));
        setLocationRelativeTo(null);
        pack();
        setPreferredSize(new Dimension(orgwidth+insets.left+insets.right, orgheight+insets.top+insets.bottom));

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
        page = "<head><base href=\"" + MainWindow.class.getResource("assets/").toString() + "\"><link href=\"blog.css\" rel=\"stylesheet\" type=\"text/css\"></head>" + page;
        JTextPane blog = new JTextPane(

        );
        blog.setContentType("text/html");
        blog.setText(page);
        blog.setBorder(BorderFactory.createEmptyBorder());
        blog.setEditable(false);
        JScrollPane blogcontainer = new JScrollPane(blog);
        blogcontainer.setBorder(BorderFactory.createEmptyBorder());
        blogcontainer.setBounds(new Rectangle(0, 0, panel.getWidth(), panel.getHeight()-200));

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
        TextField username = new TextField();
        username.setBounds(0, 14, 166, 22);

        // Password field
        TextField password = new TextField();
        password.setEchoChar('•');
        password.setBounds(0, 40, 166, 22);

        // Instance selector
        Choice instsel = new Choice();
        File file = new File(Config.instpath + "instances");
        String[] instances = file.list((current, name) -> new File(current, name).isDirectory());
        if (instances != null) {
            for (String instance : instances) {
                instsel.addItem(instance);
            }
        }
        instsel.setBounds(0, 66, 166, 22);

        // Options button
        JButton options = new JButton();
        options.setLabel("Options");
        options.setBounds(168, 14, 70, 22);
        options.addActionListener(event -> {
            new OptionsWindow(this, instsel.getSelectedItem());
        });

        // Login button
        JButton login = new JButton();
        login.setLabel("Login");
        login.setBounds(168, 40, 70, 22);
        WidgetArrayList widgetlist = new WidgetArrayList();
        login.addActionListener(event -> {
            logger.info(instsel.getSelectedItem());
            widgetlist.setStateAll(false);
            String[] launchargs = {username.getText(), password.getText(), instsel.getSelectedItem()};
            launchargs = (new LaunchArgs()).getArgs(launchargs);
            if (launchargs != null) {
                new Wrapper().main(launchargs, widgetlist);
            }
        });

        // Adding widgets and making the launcher visible
        loginform.add(logo, BorderLayout.WEST);
        loginform.add(loginpanel, BorderLayout.EAST);
        loginpanel.add(instsel);
        loginpanel.add(username);
        loginpanel.add(options);
        loginpanel.add(password);
        loginpanel.add(login);

        widgetlist.add(login);
        widgetlist.add(instsel);
        widgetlist.add(username);
        widgetlist.add(password);
        widgetlist.add(options);

        panel.add(blogcontainer, BorderLayout.CENTER);
        panel.add(loginform, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
