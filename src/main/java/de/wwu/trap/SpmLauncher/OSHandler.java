package de.wwu.trap.SpmLauncher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import de.wwu.trap.SpmLauncher.Utils.FileComparator;
import de.wwu.trap.SpmLauncher.Utils.FileManipulator;
import de.wwu.trap.SpmLauncher.Utils.MountedDirComparator;
import de.wwu.trap.SpmLauncher.Utils.RedirectInputStreamFiltered;

/**
 * This class holds static methods to do stuff related to the OS like
 * manipulating the filesystem
 * 
 * @author Kelvin Sarink
 */
public class OSHandler {

	/**
	 * This method searches for all toolboxes which have an addToPath file. This
	 * means that the root of the toolbox needs to be added to the MATLAB path.
	 * 
	 * @return List of the toolboxes which path needs to be added to the path
	 */
	public static List<File> whichToolboxesNeedPathEntry(List<File> activatedToolboxes) {
		LinkedList<File> pathToolboxes = new LinkedList<>();
		for (File activatedToolbox : activatedToolboxes) {
			File pathFile = new File(activatedToolbox.getParentFile(), "addToPath");
			if (pathFile.exists())
				pathToolboxes.add(activatedToolbox.getParentFile());
		}
		return pathToolboxes;
	}

	/**
	 * This method searches for all toolboxes which have an addToPathRecursively
	 * file. This means that the root and all its subdirs (recursively) of the
	 * toolbox needs to be added to the MATLAB path.
	 * 
	 * @return List of the toolboxes which path needs to be added to the path
	 *         recursively
	 */
	public static List<File> whichToolboxesNeedRecursivePathEntry(List<File> activatedToolboxes) {
		LinkedList<File> pathToolboxes = new LinkedList<>();
		for (File activatedToolbox : activatedToolboxes) {
			File pathFile = new File(activatedToolbox.getParentFile(), "addToPathRecursively");
			if (pathFile.exists())
				pathToolboxes.add(activatedToolbox.getParentFile());
		}
		return pathToolboxes;
	}

	/**
	 * Returns all the spm installations. toString method of files overwritten to
	 * only return name of file and not absolute path
	 * 
	 * @return Paths to the spm installations
	 */
	public static File[] getSpmVersions() {
		File spmDir = new File(App.getManagedSoftwareDir(), "spm");
		File[] spms = spmDir.listFiles((e) -> e.isDirectory());
		if (spms != null) {
			FileManipulator.onlyNameInToString(spms);
			Arrays.sort(spms, new FileComparator<File>(false));
		}

		return spms;
	}

	/**
	 * Return all MATLAB installation paths. Searching in PATH, /opt/, and
	 * /usr/local/ TODO: search for windows (and mac) paths toString method of files
	 * overwritten to only return name of file and not absolute path
	 * 
	 * @param preferredMatlabVersions MATLAB dirs preferred for specific spm dirs
	 * 
	 * @return Paths to the MATLAB installations
	 */
	public static List<File> getMatlabVersions(HashMap<File, File> preferredMatlabVersions) {
		List<File> matlabDirs = new LinkedList<>();
		File[] possibleMatlabDirs = { new File("/opt/applications/MATLAB/"), new File("/opt/MATLAB/"),
				new File("/usr/local/MATLAB/"), };

		for (File matlabDir : possibleMatlabDirs) {
			if (!matlabDir.exists())
				continue;

			File[] foundMatlabDirs = matlabDir.listFiles((file, name) -> {
				if (file.isFile() || !new File(file, name + "/bin/matlab").exists())
					return false;
				return Pattern.matches("R\\d{4}[a-z]", name);
			});

			Collections.addAll(matlabDirs, foundMatlabDirs);
		}

		if (preferredMatlabVersions != null) {
			matlabDirs.addAll(preferredMatlabVersions.values());
		}

		FileManipulator.replaceWithCanonicalPath(matlabDirs);
		FileManipulator.onlyNameInToString(matlabDirs);
		matlabDirs = matlabDirs.stream().distinct().collect(Collectors.toList());
		matlabDirs.sort(new Comparator<File>() {

			@Override
			public int compare(File arg0, File arg1) {
				Comparator<String> c = Comparator.naturalOrder();
				return c.compare(arg0.getName(), arg1.getName());
			}
		});
		return matlabDirs;
	}

	public static Process p;


	private static String generateMatlabPathCommand(File tmpSpmDir, List<File> activatedToolboxes) {
		/*
		 * Map the toolbox dirs from the source directory e.g.
		 * .../ManagedSoftare/toolbox/spm12/cat12/r1742 to the tmp spm dir under
		 * /tmp/SPMLauncher/1e1a0082-7595-457b-8afb-91230a58d0d4/toolbox/cat12
		 */
		File spmToolboxDir = new File(tmpSpmDir, "toolbox");
		HashMap<File, File> tmpActivatedToolboxDirs = new HashMap<>();

		for (File toolboxDir : activatedToolboxes) {
			String toolboxName = toolboxDir.getParentFile().getName();
			File tmpToolboxDir = new File(spmToolboxDir, toolboxName);
			try {
				toolboxDir = toolboxDir.getCanonicalFile().getParentFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			tmpActivatedToolboxDirs.put(toolboxDir, tmpToolboxDir);
		}

		/*
		 * Find out which toolboxes need normal or recursive path entries in MATLAB
		 */
		List<File> toolboxes = whichToolboxesNeedPathEntry(activatedToolboxes);
		List<File> toolboxesRec = whichToolboxesNeedRecursivePathEntry(activatedToolboxes);

		/*
		 * Generate path entries
		 */
		StringBuilder sb = new StringBuilder();
		sb.append("path('" + tmpSpmDir.getAbsolutePath() + "',path);");

		for (File toolbox : toolboxes) {
			try {
				toolbox = toolbox.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			sb.append("path('" + tmpActivatedToolboxDirs.get(toolbox) + "',path);");
		}

		for (File toolbox : toolboxesRec) {
			try {
				toolbox = toolbox.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File tmpToolbox = tmpActivatedToolboxDirs.get(toolbox);
			if (tmpToolbox == null) {
				System.out.println("Error: " + toolbox.getAbsolutePath());
				continue;
			}
			try {
				Files.walk(Paths.get(tmpToolbox.getAbsolutePath())).filter(Files::isDirectory).forEach(path -> {
					System.out.println(path);
					sb.append("path('" + path + "',path);");
				});
				;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	/**
	 * This method builds the launch command in MATLAB for the spm installation with
	 * addPath calls for the toolboxes
	 * 
	 * @param matlabDir
	 * @param tmpSpmDir
	 * @param activatedToolboxes
	 * @param devmode
	 */
	public static void buildLaunchCmdStartAndWait(File matlabDir, File tmpSpmDir, List<File> activatedToolboxes,
			boolean devmode) {
		System.out.println(matlabDir.getAbsolutePath());
		System.out.println("Starting " + tmpSpmDir.getName());

		LinkedList<String> launchCommand = new LinkedList<>();
		launchCommand.add("nice");
		launchCommand.add("-n");
		launchCommand.add("+1");
		launchCommand.add(matlabDir.getAbsolutePath() + "/bin/matlab"); // absolute path to matlab binary
		launchCommand.add("-r");
		String matlabCommands = generateMatlabPathCommand(tmpSpmDir, activatedToolboxes) + "cd('/spm-data');"
				+ "spm fmri;"
//				+ "quit"
		;
		launchCommand.add(matlabCommands);

		if (devmode)
			launchCommand.add("-desktop");
		else {
			launchCommand.add("-nodesktop");
			launchCommand.add("-nosplash");
		}

		System.out.println("\nStarting MATLAB / SPM with the following command:");
		for (String s : launchCommand) {
			System.out.println(s.replace(";", "; \n").replace("path(", "\tpath("));
		}

		ProcessBuilder pb = new ProcessBuilder().inheritIO().command(launchCommand);
		pb.redirectOutput(ProcessBuilder.Redirect.PIPE);
		try {
			p = pb.start();
			RedirectInputStreamFiltered.filterAndRedirect(p.getInputStream(), System.out, new Runnable() {

				@Override
				public void run() {
					p.destroy();
				}
			});
			p.waitFor();

			/*
			 * Make the cmd 'sane' again. Otherwise you cannot see what you write after the
			 * SPMLauncher closes
			 */
			new ProcessBuilder().command("stty", "sane").inheritIO().start();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method mounts the spm installation with the chosen toolboxes to a
	 * temporary directory (e.g.
	 * App.getMountDir()/6d9e5da2-cd28-43c8-af46-9f5e3e29d7de), so spm can be started
	 * with and only with the specified toolboxes. It also creates a log file in
	 * case this Launcher crashes.
	 * 
	 * @param spmDir    the path to the chosen spm installation
	 * @param toolboxes the paths to the chosen versions of the toolboxes
	 * @return a LinkedList with the dirs which has been mounted successfully
	 */
	public static LinkedList<File> createMounts(File spmDir, Collection<File> toolboxes) {

		/*
		 * Preparations
		 */
		LinkedList<File> mountedDirs = new LinkedList<>();
		boolean ret = false;
		File uuidDir = new File(App.getMountDir(), "/" + App.LAUNCHER_UUID.toString());
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

	public static long getPid() {
		return ProcessHandle.current().pid();
	}

	/**
	 * This method checks, whether the SPM-installation has the necessary subdirs to
	 * create the mounts for the toolboxes
	 * 
	 * @param spmDir the dir of the SPM-installation
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
	 * parent is in this list, the subdir will be unmounted before the parentdir.
	 * 
	 * @param dirs the directories which shall be unmounted
	 */
	public static void umountAllDirs(List<File> dirs, String uuid) {
		if (dirs == null) {
			return;
		}
		dirs.sort(new MountedDirComparator());

		for (File dir : dirs) {
			boolean deleteDir = dir.getAbsolutePath().equals(App.getMountDir() + "/" + uuid);
			umount(dir, deleteDir);
		}

		File logMount = new File(App.getMountDir(), uuid + App.MOUNT_LOG_SUFFIX);
		logMount.delete();
		File logPid = new File(App.getMountDir(), uuid + App.PID_LOG_SUFFIX);
		logPid.delete();
		File logFile = new File(App.getMountDir(), App.LAUNCHER_UUID + ".log");
		logFile.delete();

	}

	/**
	 * Tries to unmount dir. dir has to be subdir of App.getMountDir() Tries to
	 * unmount with sudo App.getMountScript() -u
	 * 
	 * @param dir the dir which will be unmounted
	 * @return whether the unmount was successfull
	 */
	private static boolean umount(File dir, boolean delete) {
		boolean ret = false;
		String path = dir.getAbsolutePath();
		if (!path.startsWith(App.getMountDir() + "/")) {
			System.out.println(dir.getAbsolutePath() + " does not start with " + App.getMountDir() + "/");
			return false;
		}
		String relativePath = path.replaceFirst(App.getMountDir() + "/", "");

		String[] cmd = new String[] { "sudo", App.getMountScript(), "-u", relativePath };

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
	 * App.getManagedSoftwareDir() and newDir has to be subdir of App.getMountDir()
	 * Tries to mount with sudo App.getMountScript() -m
	 * 
	 * @param oldDir source of the mount
	 * @param newDir target of the mount
	 * @return Whether the mount was successfull
	 */
	private static boolean mount(File oldDir, File newDir) {
		boolean ret = false;
		String oldPath = oldDir.getAbsolutePath();
		if (!oldPath.startsWith(App.getManagedSoftwareDir() + "/")) {
			System.err.println(
					"Oldpath starts wrong. Excpected: " + App.getManagedSoftwareDir() + "/" + ", recieved: " + oldPath);
			return false;
		}
		String oldRelativePath = oldPath.replaceFirst(App.getManagedSoftwareDir() + "/", "");

		String newPath = newDir.getAbsolutePath();
		if (!newPath.startsWith(App.getMountDir() + "/")) {
			System.err.println("Newpath starts wrong. Excpected: " + App.getMountDir() + "/" + ", recieved: " + newPath);
			return false;
		}
		String newRelativePath = newPath.replaceFirst(App.getMountDir() + "/", "");

		if (!newDir.mkdirs() && !newDir.exists()) {
			System.err.println("Could not create" + newDir.getAbsolutePath());
			return false;
		}

		String[] cmd = new String[] { "sudo", App.getMountScript(), "-m", oldRelativePath, newRelativePath };

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

	/**
	 * Load the preferred_matlab_versions.csv from the
	 * {@link de.wwu.trap.SpmLauncher.App#MANAGED_SOFTWARE_DIR} and puts them in a
	 * HashMap
	 * 
	 * @return HashMap with spmdir as key and matlabdir as value
	 */
	public static HashMap<File, File> loadPreferredMatlabVersions() {
		HashMap<File, File> matlabVersionsMap = new HashMap<>();
		File csvFile = new File(App.getManagedSoftwareDir(), "preferred_matlab_versions.csv");
		CSVParser cp;
		try {
			Reader reader = new InputStreamReader(new FileInputStream(csvFile));
			CSVFormat format = CSVFormat.DEFAULT;
			format.builder().setCommentMarker('#').build();
			cp = new CSVParser(reader, format);
		} catch (IOException e) {
			return null;
		}

		Iterator<CSVRecord> csvIterator = cp.iterator();

		while (csvIterator.hasNext()) {
			CSVRecord record = csvIterator.next();
			try {
				File keydir = new File(App.getManagedSoftwareDir(), "spm/" + record.get(0));
				File valuedir = new File(record.get(1));

				if (new File(valuedir, "/bin/matlab").exists()) {
					if (Pattern.matches("R\\d{4}[a-z]", valuedir.getName())) {
						matlabVersionsMap.put(keydir, valuedir);
					} else {
						System.err.println("The preferred MATLAB version (" + valuedir.getAbsolutePath() + ") for "
								+ keydir.getName() + " cannot be added since this is not a MATLAB directory!");
					}
				} else {
					System.err.println("The preferred MATLAB version (" + valuedir.getAbsolutePath() + ") for "
							+ keydir.getName() + " cannot be added since the matlab binary cannot be found! - "
							+ "Please check " + csvFile.getName());
				}

			} catch (Exception e) {
			}
		}

		if (cp != null) {
			try {
				cp.close();
			} catch (IOException e) {
			}
		}
		return matlabVersionsMap;
	}

	/**
	 * This method loads the tooltips.csv from the
	 * {@link de.wwu.trap.SpmLauncher.App#MANAGED_SOFTWARE_DIR} and puts them in a
	 * HashMap
	 * 
	 * @return the described HashMap<File, String> where the file ist the directory
	 *         of the spm installation, the toolbox, or the toolbox version.
	 */
	public static HashMap<File, String> getTooltips() {
		HashMap<File, String> tooltipsMap = new HashMap<>();

		CSVParser cp;
		try {
			Reader reader = new InputStreamReader(
					new FileInputStream(new File(App.getManagedSoftwareDir(), "tooltips.csv")));
			CSVFormat format = CSVFormat.DEFAULT;
			format.builder().setCommentMarker('#').build();
			cp = new CSVParser(reader, format);
		} catch (IOException e) {
			return null;
		}

		Iterator<CSVRecord> csvIterator = cp.iterator();

		while (csvIterator.hasNext()) {
			CSVRecord record = csvIterator.next();
			try {
				File dir = new File(App.getManagedSoftwareDir(), record.get(0));
				tooltipsMap.put(dir, record.get(1).replace("\\n", "\n"));
			} catch (Exception e) {
			}
		}

		if (cp != null) {
			try {
				cp.close();
			} catch (IOException e) {
			}
		}

		return tooltipsMap;
	}

}
