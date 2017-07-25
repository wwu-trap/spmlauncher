package de.wwu.trap.SpmLauncher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import Utils.FileComparator;
import Utils.FileManipulator;

/**
 * This class holds static methods to do stuff related to the OS like
 * manipulating the filesystem
 * 
 * @author Kelvin Sarink
 */
public class OSHandler {

	/**
	 * Returns all the spm installations. toString method of files overwritten
	 * to only return name of file and not absolute path
	 * 
	 * @return Paths to the spm installtions
	 */
	public static File[] getSpmVersions() {
		File spmDir = new File(App.MANAGED_SOFTWARE_DIR, "spm");
		File[] spms = spmDir.listFiles((e) -> e.isDirectory());
		FileManipulator.onlyNameInToString(spms);
		Arrays.sort(spms, new FileComparator<File>());
		return spms;
	}

	public static boolean startSPM() {
		boolean ret = false;

		return ret;
	}

	/**
	 * This method mounts the spm installation with the chosen toolboxes to a
	 * temporary directory (e.g.
	 * App.MOUNT_DIR/6d9e5da2-cd28-43c8-af46-9f5e3e29d7de), so spm can be
	 * started with and only with the specified toolboxes. It also creates a log
	 * file in case this Launcher crashes.
	 * 
	 * @param spmDir
	 *            the path to the chosen spm installation
	 * @param toolboxes
	 *            the paths to the chosen versions of the toolboxes
	 */
	public static boolean createMounts(File spmDir, Collection<File> toolboxes) {
		// TODO complete createMounts. Don't forget: log to info file
		// e.g. check if /spm/toolbox dir exists
		// and check whether all offered Toolboxes have empty dirs in toolbox
		// dir

		/*
		 * Preparations
		 */
		boolean ret = false;
		File uuidDir = new File(App.MOUNT_DIR, "/" + App.LAUNCHER_UUID.toString());
		uuidDir.mkdirs();

		/*
		 * Start the FileWriters
		 */
		PrintWriter pwPids = null;
		PrintWriter pwMounts = null;
		try {
			File pidLogFile = new File(uuidDir.getAbsolutePath() + App.PID_LOG_SUFFIX);
			pidLogFile.createNewFile();
			pwPids = new PrintWriter(new FileOutputStream(pidLogFile), true);

			File mountLogFile = new File(uuidDir.getAbsolutePath() + App.MOUNT_LOG_SUFFIX);
			mountLogFile.createNewFile();
			pwMounts = new PrintWriter(new FileOutputStream(mountLogFile), true);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		/*
		 * Create the mounts
		 */
		ret = mount(spmDir, uuidDir);
		if (!ret) {

			pwPids.close();
			pwMounts.close();
			return false;
		}

		for (File toolbox : toolboxes) {
			File mountedToolboxDir = new File(uuidDir.getAbsolutePath(),
					"/toolbox/" + toolbox.getParentFile().getName());

			ret = ret && mount(toolbox, mountedToolboxDir);
			if (!ret) {
				pwPids.close();
				pwMounts.close();
				return false;
			}
		}

		/*
		 * Finishing this method
		 */
		if (pwPids != null) {
			pwPids.flush();
			pwPids.close();
		}
		if (pwMounts != null) {
			pwMounts.flush();
			pwMounts.close();
		}

		return ret;
	}

	@SuppressWarnings("restriction")
	public static int getPid() {
		int pid = -1;
		try {
			java.lang.management.RuntimeMXBean runtime = java.lang.management.ManagementFactory.getRuntimeMXBean();
			java.lang.reflect.Field jvm = runtime.getClass().getDeclaredField("jvm");
			jvm.setAccessible(true);
			sun.management.VMManagement mgmt = (sun.management.VMManagement) jvm.get(runtime);
			java.lang.reflect.Method pid_method = mgmt.getClass().getDeclaredMethod("getProcessId");
			pid_method.setAccessible(true);

			pid = (Integer) pid_method.invoke(mgmt);
			System.out.println("This PID: " + pid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pid;
	}

	/**
	 * This method checks, whether the SPM-installation has the necessary
	 * subdirs to create the mounts for the toolboxes
	 * 
	 * @param spmDir
	 *            the dir of the SPM-installation
	 * @return whether the subdirs exist
	 */
	public static boolean checkForNecessarySubdirsForToolboxes(File spmDir) {
		boolean ret = false;
		// TODO complete check for subtirs in toolbox dir
		// alternativly disable toolboxes with no toolbox dir in spm
		// installation

		// TODO check whether an spm installation has an launch_command.txt
		// inside the spm dir
		return ret;
	}

	/**
	 * Tries to unmount dir. dir has to be subdir of App.MOUNT_DIR Tries to
	 * unmount with sudo App.MOUNT_SCRIPT -u
	 * 
	 * @param dir
	 *            the dir which will be unmounted
	 * @return whether the unmount was successfull
	 */
	public static boolean umount(File dir) {
		boolean ret = false;
		String path = dir.getAbsolutePath();
		if (!path.startsWith(App.MOUNT_DIR + "/")) {
			return false;
		}
		String relativePath = path.replaceFirst(App.MOUNT_DIR + "/", "");

		String[] cmd = new String[] { "sudo", App.MOUNT_SCRIPT, "-u", relativePath };

		try {
			Process p = new ProcessBuilder(cmd).start();

			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(p.getErrorStream()));
			// String line = "";
			// while ((line = br.readLine()) != null) {
			// System.out.println(line);
			// }

			ret = p.waitFor() == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		boolean empty = dir.listFiles().length == 0;
		if (dir.exists() && dir.isDirectory() && empty) {
			dir.delete();
		}
		return ret;
	}

	/**
	 * Tries to mount (via rebind) oldDir to newDir. oldDir has to be subdir of
	 * App.MANAGED_SOFTWARE_DIR and newDir has to be subdir of App.MOUNT_DIR
	 * Tries to mount with sudo App.MOUNT_SCRIPT -m
	 * 
	 * @param oldDir
	 *            source of the mount
	 * @param newDir
	 *            target of the mount
	 * @return Whether the mount was successfull
	 */
	public static boolean mount(File oldDir, File newDir) {
		boolean ret = false;
		String oldPath = oldDir.getAbsolutePath();
		if (!oldPath.startsWith(App.MANAGED_SOFTWARE_DIR + "/")) {
			System.err.println(
					"Oldpath starts wrong. Excpected: " + App.MANAGED_SOFTWARE_DIR + "/" + ", recieved: " + oldPath);
			return false;
		}
		String oldRelativePath = oldPath.replaceFirst(App.MANAGED_SOFTWARE_DIR + "/", "");

		String newPath = newDir.getAbsolutePath();
		if (!newPath.startsWith(App.MOUNT_DIR + "/")) {
			System.err.println("Newpath starts wrong. Excpected: " + App.MOUNT_DIR + "/" + ", recieved: " + newPath);
			return false;
		}
		String newRelativePath = newPath.replaceFirst(App.MOUNT_DIR + "/", "");

		if (!newDir.mkdirs() && !newDir.exists()) {
			System.err.println("Could not create" + newDir.getAbsolutePath());
			return false;
		}

		String[] cmd = new String[] { "sudo", App.MOUNT_SCRIPT, "-m", oldRelativePath, newRelativePath };

		try {
			System.out.println("Mounting " + oldDir.getAbsolutePath() + " to " + newDir.getAbsolutePath());
			Process p = new ProcessBuilder(cmd).start();

			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(p.getErrorStream()));
			// String line = "";
			// while ((line = br.readLine()) != null) {
			// System.out.println(line);
			// }
			//
			ret = p.waitFor() == 0;
			if (!ret) {
				System.err.println("Could not mount " + oldDir + " to " + newDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return ret;
	}

}
