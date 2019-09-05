package net.glass.glassl;
/*
 A simple Java Console for your application (Swing version)
 Requires Java 1.1.5 or higher

 Disclaimer the use of this source is at your own risk.

 Permision to use and distribute into your own applications

 RJHM van den Bergh , rvdb@comweb.nl
*/

// Modified for use with Glass Launcher.
// Because java UI libs and consoles are weird.

import javax.imageio.ImageIO;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URISyntaxException;

public class AWTConsole extends WindowAdapter
{
	private Frame frame;

	private TextArea textArea = new TextArea();
	private final PrintStream oldout = System.out;
	private final PrintStream olderr = System.err;
	private final ConsoleStream pin=new ConsoleStream(textArea, oldout);
	private final ConsoleStream pin2=new ConsoleStream(textArea, olderr);
	
	public AWTConsole()
	{
		// create all components and add them
		frame=new Frame("Console");
		try {
			frame.setIconImage(ImageIO.read(AWTConsole.class.getResource("assets/glass.png").toURI().toURL()));
		}
		catch (URISyntaxException | IOException e) {
			e.printStackTrace();
		}
		frame.setLayout(new GridLayout());
		frame.setBounds(0, 0,600, 300);

		textArea.setEditable(false);
		textArea.setBackground(Color.black);
		textArea.setForeground(Color.lightGray);
		
		Panel panel=new Panel();
		panel.setLayout(new GridLayout());
		panel.add(textArea);
		frame.add(panel);

		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		System.setOut(new PrintStream(this.pin,true));
		System.setErr(new PrintStream(this.pin2,true));
	}

	public Frame getConsoleFrame() {
		return frame;
	}
}