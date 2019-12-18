package net.glasslauncher.legacy;

import net.glasslauncher.legacy.util.ConsoleStream;
import net.glasslauncher.legacy.util.Pastee;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;

public class ConsoleWindow extends JFrame {
    private JTextArea textArea = new JTextArea();
    private final PrintStream oldout = System.out;
    private final PrintStream olderr = System.err;
    private final ConsoleStream pin = new ConsoleStream(textArea, oldout);
    private final ConsoleStream pin2 = new ConsoleStream(textArea, olderr);

    public ConsoleWindow() {
        // create all components and add them
        setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        setTitle("Glass Launcher Console");
        addWindowListener(new WindowAdapter() {
                              public void windowClosing(WindowEvent we) {
                                  if (!Main.isLauncherActive()) {
                                      Main.getLogger().info("Closing...");
                                      System.exit(0);
                                  }
                                  Main.getLogger().info("Close the launcher using the main window!");
                              }
                          }
        );
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        try {
            setIconImage(ImageIO.read(ConsoleWindow.class.getResource("assets/glass.png").toURI().toURL()));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
        setBounds(0, 0, 600, 300);

        textArea.setEditable(false);
        textArea.setBackground(Color.black);
        textArea.setForeground(Color.lightGray);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane textContainer = new JScrollPane(textArea);

        JButton uploadButton = new JButton();
        uploadButton.setText("Upload log to paste.ee");
        uploadButton.setOpaque(false);
        uploadButton.addActionListener(event -> {
            Pastee paste = new Pastee(textArea.getText());
            String url = paste.post();
            if (url != null) {
                new OpenLinkWindow(null, url);
            } else {
                JOptionPane.showMessageDialog(null, "An error occurred while uploading. Try again later.");
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout());
        panel.add(textContainer);
        add(panel, BorderLayout.CENTER);
        add(uploadButton, BorderLayout.SOUTH);

        setLocation(5, 10);
        setVisible(true);

        System.setOut(new PrintStream(this.pin, true));
        System.setErr(new PrintStream(this.pin2, true));
    }
}