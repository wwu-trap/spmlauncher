package de.wwu.trap.SpmLauncher;

import javax.swing.UIManager;

/**
 * 
 * @author Kelvin Sarink
 */
public class App {

	public static final String MANAGED_SOFTWARE_DIR = "/opt/applications/SPMLauncher/ManagedSoftware";
	public static final String MOUNT_DIR = "/tmp/SPMLauncher";
	public static final String MOUNT_SCRIPT = "/usr/local/bin/tmp-mount";
	

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		} catch (Exception e) {
		}

		Gui gui = new Gui();
		gui.setVisible(true);

	}
}
