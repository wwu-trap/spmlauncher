package de.wwu.trap.SpmLauncher;

import java.awt.EventQueue;

import javax.swing.UIManager;

import org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel;

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
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(new SubstanceMarinerLookAndFeel());
		} catch (Exception e) {
		}

		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui gui = new Gui();
					gui.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		

	}
}
