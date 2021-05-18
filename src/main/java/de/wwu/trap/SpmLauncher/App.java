package de.wwu.trap.SpmLauncher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.UUID;

import javax.swing.UIManager;

import de.wwu.trap.SpmLauncher.Gui.GuiManager;
import de.wwu.trap.SpmLauncher.Gui.FxGuiController;

/**
 * 
 * @author Kelvin Sarink
 */
public class App {

	/**
	 * The directory in which the stripped SPM installations and the optional
	 * toolboxes are (stripped: SPM with limited amount of toolboxes). If this
	 * constant is changed, be aware that you also have to change the tmp-mount
	 * script!
	 */
	public static final String MANAGED_SOFTWARE_DIR = "/opt/applications/SPMLauncher/ManagedSoftware";

	/**
	 * The directory in which the temporary mounts this Launcher creates will be
	 * placed. If this constant is changed, be aware that you also have to change
	 * the tmp-mount script!
	 */
	public static final String MOUNT_DIR = "/tmp/SPMLauncher";

	/**
	 * The mount script which can be called with sudo without having to enter a
	 * password. SEE comment in tmp-mount script.
	 */
	public static final String MOUNT_SCRIPT = "/usr/local/bin/tmp-mount";

	/**
	 * The suffix for the name of the file in which the pids of the SPMLauncher and
	 * the launched SPM installation are stored.
	 */
	public static final String PID_LOG_SUFFIX = "_pids.txt";

	/**
	 * The suffix for the name of the file in which a list with created mounts are
	 * stored.
	 */
	public static final String MOUNT_LOG_SUFFIX = "_mounts.txt";

	/**
	 * At the start of this application a unique id will be generated for the
	 * mount_directory which will be created after starting an SPM instance with
	 * this launcher.
	 */
	public static final UUID LAUNCHER_UUID = UUID.randomUUID();

	/**
	 * The official entry point of this java application.
	 * 
	 * @param args the arguments from the commandline
	 */
	public static void main(String[] args) {
		// Create MOUNT_DIR with chmod 777
		File mountDir = new File(App.MOUNT_DIR);
		if (!mountDir.exists()) {
			mountDir.mkdirs();
			mountDir.setReadable(true, false);
			mountDir.setWritable(true, false);
			mountDir.setExecutable(true, false);
		}

		boolean enableFx = true;

		for (String arg : args) {
			if (arg.equalsIgnoreCase("--no-console") || arg.equalsIgnoreCase("-nc")) {
				try {
					// Write Sysout to logFile if it isn't started with argument
					// --console or -c
					File logFile = new File(MOUNT_DIR, LAUNCHER_UUID + ".log");
					System.setOut(new PrintStream(logFile));
					System.setErr(new PrintStream(logFile));
				} catch (FileNotFoundException e1) {

				}
			}

			if (arg.equalsIgnoreCase("--disable-fx")) {
				enableFx = false;
			}

		}

		System.out.println("This PID: " + OSHandler.getPid());

		if (!enableFx) {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {

			}
			new GuiManager().startGui();
		} else {
			FxGuiController.launch(FxGuiController.class, args);
		}

	}
}
