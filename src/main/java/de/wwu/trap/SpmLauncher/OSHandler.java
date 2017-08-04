package de.wwu.trap.SpmLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.wwu.trap.Utils.FileComparator;
import de.wwu.trap.Utils.FileManipulator;
import de.wwu.trap.Utils.MountedDirComparator;

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
	 * @return Paths to the spm installations
	 */
	public static File[] getSpmVersions() {
		File spmDir = new File(App.MANAGED_SOFTWARE_DIR, "spm");
		File[] spms = spmDir.listFiles((e) -> e.isDirectory());
		if (spms != null) {
			FileManipulator.onlyNameInToString(spms);
			Arrays.sort(spms, new FileComparator<File>());
		}

		return spms;
	}

	public static Process p;

	/**
	 * This method searches for the launch_command.txt within the spmDir and
	 * starts the spm installation with it
	 * 
	 * @param tmpSpmDir
	 *            the temporary mount SPM directory with a launch.sh in it
	 * @return
	 */
	public static void startSpmAndWait(File tmpSpmDir) {
		System.out.println("Starting " + tmpSpmDir.getName());
		String launchCommand = tmpSpmDir.getAbsolutePath() + "/launch.sh " + tmpSpmDir.getAbsolutePath();

		System.out.println("Starting with: " + launchCommand);

		/*
		 * Start spm and wait
		 */
		try {
			ProcessBuilder pb = new ProcessBuilder(launchCommand.split(" "));

			p = pb.start();

			// Process p = Runtime.getRuntime().exec(launchCommand);

			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(p.getInputStream()));
			Thread p1 = new Thread() {
				@Override
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = "";
					try {
						while ((line = br.readLine()) != null) {
							System.out.println(line);
							if (line.contains("Bye for now...")) {
								System.out.println("Killing SPM because of \">> Bye for now...\"");
								p.destroy();
							}
						}
					} catch (IOException e) {
					}
				}
			};
			p1.start();
			Thread p2 = new Thread() {
				@Override
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					String line = "";
					try {
						while ((line = br.readLine()) != null) {
							System.err.println(line);
							if (line.contains(">> Bye for now...")) {
								System.out.println("Killing SPM because of \">> Bye for now...\"");
								p.destroy();
							}
						}
					} catch (IOException e) {
					}
				}
			};
			p2.start();

			p.waitFor();

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

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
	 * @return a LinkedList with the dirs which has been mounted successfully
	 */
	public static LinkedList<File> createMounts(File spmDir, Collection<File> toolboxes) {

		/*
		 * Preparations
		 */
		LinkedList<File> mountedDirs = new LinkedList<>();
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
			return mountedDirs;
		}

		pwPids.println("SPMLauncher," + getPid());

		/*
		 * Create the mounts
		 */
		ret = mount(spmDir, uuidDir);
		if (ret) {
			mountedDirs.add(uuidDir);
			pwMounts.println(uuidDir);
			pwMounts.flush();
		} else {
			pwPids.close();
			pwMounts.close();
			return mountedDirs;
		}

		for (File toolbox : toolboxes) {
			File mountedToolboxDir = new File(uuidDir.getAbsolutePath(),
					"/toolbox/" + toolbox.getParentFile().getName());

			ret = ret && mount(toolbox, mountedToolboxDir);
			if (ret) {
				mountedDirs.add(mountedToolboxDir);
				pwMounts.println(mountedToolboxDir);
				pwMounts.flush();
			} else {
				pwPids.close();
				pwMounts.close();
				return mountedDirs;
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

		return mountedDirs;
	}

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

		// TODO check whether an spm installation has an launch.sh
		// inside the spm dir
		return ret;
	}

	/**
	 * This method unmounts a list of directories. The list doesn't need to be
	 * sorted. If subdirs of dirs has to be unmounted before the parent, and the
	 * parent is in this list, the subdir will be unmounted before the
	 * parentdir.
	 * 
	 * @param dirs
	 *            the directories which shall be unmounted
	 */
	public static void umountAllDirs(List<File> dirs, String uuid) {
		if (dirs == null) {
			return;
		}
		dirs.sort(new MountedDirComparator());

		for (File dir : dirs) {
			boolean deleteDir = dir.getAbsolutePath().equals(App.MOUNT_DIR + "/" + uuid);
			umount(dir, deleteDir);
		}

		File logMount = new File(App.MOUNT_DIR, uuid + App.MOUNT_LOG_SUFFIX);
		logMount.delete();
		File logPid = new File(App.MOUNT_DIR, uuid + App.PID_LOG_SUFFIX);
		logPid.delete();
		File logFile = new File(App.MOUNT_DIR, App.LAUNCHER_UUID + ".log");
		logFile.delete();

	}

	/**
	 * Tries to unmount dir. dir has to be subdir of App.MOUNT_DIR Tries to
	 * unmount with sudo App.MOUNT_SCRIPT -u
	 * 
	 * @param dir
	 *            the dir which will be unmounted
	 * @return whether the unmount was successfull
	 */
	private static boolean umount(File dir, boolean delete) {
		boolean ret = false;
		String path = dir.getAbsolutePath();
		if (!path.startsWith(App.MOUNT_DIR + "/")) {
			System.out.println(dir.getAbsolutePath() + " does not start with " + App.MOUNT_DIR + "/");
			return false;
		}
		String relativePath = path.replaceFirst(App.MOUNT_DIR + "/", "");

		String[] cmd = new String[] { "sudo", App.MOUNT_SCRIPT, "-u", relativePath };

		try {
			System.out.println("Unmounting with delete=" + delete + " " + dir);
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

		if (dir != null & delete) {
			boolean empty = dir.listFiles().length == 0;
			if (dir.exists() && dir.isDirectory() && empty) {
				dir.delete();
			}

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
	private static boolean mount(File oldDir, File newDir) {
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
