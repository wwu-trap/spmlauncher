package de.wwu.trap.SpmLauncher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.UUID;

import javax.swing.UIManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.wwu.trap.SpmLauncher.Gui.FxGuiController;
import de.wwu.trap.SpmLauncher.Gui.GuiManager;

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
	public static String getManagedSoftwareDir() {
		if (managedSoftwareDir != null)
			return App.managedSoftwareDir;
		else
			return App.managedSoftwareDirDefault;
	}

	private static String managedSoftwareDir = null;
	private static final String managedSoftwareDirDefault = "/opt/applications/SPMLauncher/ManagedSoftware";

	/**
	 * The directory in which the temporary mounts this Launcher creates will be
	 * placed. If this constant is changed, be aware that you also have to change
	 * the tmp-mount script!
	 */
	public static String getMountDir() {
		if (App.mountDir != null)
			return App.mountDir;
		else
			return App.mountDirDefault;
	}

	private static String mountDir = null;
	private static final String mountDirDefault = "/tmp/SPMLauncher";

	/**
	 * The mount script which can be called with sudo without having to enter a
	 * password. See comment in tmp-mount script.
	 */
	public static String getMountScript() {
		if (App.mountScript != null)
			return App.mountScript;
		else
			return App.mountScriptDefault;
	}

	private static String mountScript = null;
	private static String mountScriptDefault = "/usr/local/bin/tmp-mount";

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

		/*
		 * CMD Argument Parsing
		 */
		final Options options = new Options();

		// General options
		Option optionNc = new Option("nc", "no-console", false, "no console");
		options.addOption(optionNc);
		Option optionNofx = new Option("nofx", "disable-fx", false, "Disable the fx GUI and use swing");
		options.addOption(optionNofx);

		// Path options
		Option optionMansofdir = new Option("msd", "managed-software-dir", false,
				"Path to the ManagedSoftware directory - place where spm and it's toolboxes are stored. "
						+ "Be aware to adjust the tmp-mount script accordingly");
		options.addOption(optionMansofdir);
		Option optionMountdir = new Option("md", "mount-dir", false,
				"Path to the directory where the temporary spm dirs are mounted");
		options.addOption(optionMountdir);
		Option optionMountscript = new Option("ms", "mount-script", false, "Path to the mount script tmp-mount");
		options.addOption(optionMountscript);

		// parsing
		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException e2) {
			e2.printStackTrace();
		}

		/*
		 * Set Options
		 */
		App.managedSoftwareDir = line.getOptionValue(optionMansofdir);
		App.mountDir = line.getOptionValue(optionMountdir);
		App.mountScript = line.getOptionValue(optionMountscript);
		
		/*
		 * Create mount dir with correct permissions for multiuser environments
		 */
		File mountDir = new File(App.getMountDir());
		if (!mountDir.exists()) {
			mountDir.mkdirs();
			mountDir.setReadable(true, false);
			mountDir.setWritable(true, false);
			mountDir.setExecutable(true, false);
		}

		if (line != null && line.hasOption(optionNc)) {
			try {
				// Write Sysout to logFile if it isn't started with argument
				// --console or -c
				File logFile = new File(App.getMountDir(), LAUNCHER_UUID + ".log");
				System.setOut(new PrintStream(logFile));
				System.setErr(new PrintStream(logFile));
			} catch (FileNotFoundException e1) {

			}
		}

		System.out.println("This PID: " + OSHandler.getPid());

		if (line != null && line.hasOption(optionNofx)) {
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
