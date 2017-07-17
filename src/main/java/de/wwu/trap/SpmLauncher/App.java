package de.wwu.trap.SpmLauncher;

import javax.swing.UIManager;

import Gui.GuiSteuerung;

/**
 * 
 * @author Kelvin Sarink
 */
public class App {

	public static final String MANAGED_SOFTWARE_DIR = "/opt/applications/SPMLauncher/ManagedSoftware";
	public static final String MOUNT_DIR = "/tmp/SPMLauncher";
	public static final String MOUNT_SCRIPT = "/usr/local/bin/tmp-mount";

	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		
		new GuiSteuerung().startGui();
		System.out.println(System.currentTimeMillis() - t1);

	}
}
