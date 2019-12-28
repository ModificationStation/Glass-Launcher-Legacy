package net.glasslauncher.legacy;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

public class ProgressWindow extends JDialog {
    private JPanel panel;
    private JProgressBar progressBar;

    public ProgressWindow(Window frame, String title) {
        super(frame);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setLayout(new GridLayout());
        setResizable(false);
        setTitle(title);

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