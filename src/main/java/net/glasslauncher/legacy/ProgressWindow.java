package net.glasslauncher.legacy;

import lombok.Setter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ProgressWindow extends JDialog {
    private JPanel panel;
    private JProgressBar progressBar;
    @Setter private Thread thread;

    public ProgressWindow(Window frame, String title) {
        super(frame);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle(title);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                try {
                    int isClosing = JOptionPane.showOptionDialog(
                            getContentPane(),
                            "Cancelling may corrupt any open files. Are you sure?",
                            "Confirmation",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null,
                            null,
                            JOptionPane.NO_OPTION
                    );
                    if (isClosing == JOptionPane.YES_OPTION) {
                        if (thread != null) {
                            thread.interrupt();
                        }
                        dispose();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        panel = new JPanel(null);
        panel.setPreferredSize(new Dimension(340, 72));

        progressBar = new JProgressBar();
        progressBar.setBounds(20, 20, 300, 32);
        progressBar.setStringPainted(true);

        panel.add(progressBar);
        add(panel);

        pack();
        setLocationRelativeTo(frame);
    }

    public void setProgressText(String progressText) {
        progressBar.setString(progressText);
        progressBar.repaint();
    }

    public void setProgressColor(Color color) {
        progressBar.setForeground(color);
    }

    public void setProgressTextColor(Color selected, Color unselected) {
        progressBar.setUI(new BasicProgressBarUI() {
            protected Color getSelectionBackground() {return unselected;}
            protected Color getSelectionForeground() {return selected;}
        });
        progressBar.repaint();
    }

    public void setProgress(int progress) {
        progressBar.setValue(progress);
        progressBar.repaint();
    }

    public void setProgressMax(int max) {
        progressBar.setMaximum(max);
        progressBar.repaint();
    }

    public void increaseProgress() {
        progressBar.setValue(progressBar.getValue() + 1);
    }
}