package de.wwu.trap.SpmLauncher;

import javax.swing.UIManager;

/**
 * 
 * @author Kelvin Sarink
 */
public class App {

	public static final String MANAGED_SOFTWARE_DIR = "/opt/applications/SpmLauncher/ManagedSoftware";

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {
		}

		Gui gui = new Gui();
		gui.setVisible(true);

	}
}
