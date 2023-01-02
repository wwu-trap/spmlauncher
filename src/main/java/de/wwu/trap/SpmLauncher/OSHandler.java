package de.wwu.trap.SpmLauncher;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import de.wwu.trap.SpmLauncher.Utils.FileComparator;
import de.wwu.trap.SpmLauncher.Utils.FileManipulator;
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
				pathToolboxes.add(activatedToolbox);
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
				pathToolboxes.add(activatedToolbox);
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

	private static File globalToStaticToolboxDir(File spmDir, File toolboxDir) {
		String toolboxName = toolboxDir.getParentFile().getName();
		File spmToolboxDir = new File(spmDir, "toolbox");
		File tmpToolboxDir = new File(spmToolboxDir, toolboxName);
		return tmpToolboxDir;
	}

	private static String generateMatlabPathCommand(File tmpSpmDir, List<File> activatedToolboxes,
			HashMap<String, String> toolboxBinds) {
		/*
		 * Map the toolbox dirs from the source directory e.g.
		 * .../ManagedSoftare/toolbox/spm12/cat12/r1742 to the tmp spm dir under
		 * /tmp/SPMLauncher/1e1a0082-7595-457b-8afb-91230a58d0d4/toolbox/cat12
		 */
		HashMap<File, File> tmpActivatedToolboxDirs = new HashMap<>();

		for (File toolboxDir : activatedToolboxes) {
			File tmpToolboxDir = globalToStaticToolboxDir(tmpSpmDir, toolboxDir);
			try {
				File toolboxDirParent = toolboxDir.getCanonicalFile().getParentFile();
				tmpActivatedToolboxDirs.put(toolboxDirParent, tmpToolboxDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			sb.append("path('" + tmpActivatedToolboxDirs.get(toolbox.getParentFile()) + "',path);");
		}

		for (File toolbox : toolboxesRec) {
			try {
				toolbox = toolbox.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			File tmpToolbox = tmpActivatedToolboxDirs.get(toolbox.getParentFile());
			if (tmpToolbox == null) {
				System.out.println("Error: " + toolbox.getAbsolutePath());
				continue;
			}
			try {
				Files.walk(Paths.get(toolbox.getAbsolutePath())).filter(Files::isDirectory).forEach(path -> {
					String pathStr = path.toString();
					for (Map.Entry<String, String> toolboxBindEntry : toolboxBinds.entrySet()) {
						pathStr = pathStr.replace(toolboxBindEntry.getKey(), toolboxBindEntry.getValue());
					}
					System.out.println(pathStr);
					sb.append("path('" + pathStr + "',path);");
				});
				;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	private static HashMap<String, String> generateToolboxBinds(File spmDir, List<File> activatedToolboxes) {
		File spmToolboxDir = new File(spmDir, "toolbox");
		HashMap<String, String> toolboxBinds = new HashMap<>();

		for (File toolboxDir : activatedToolboxes) {
			String toolboxName = toolboxDir.getParentFile().getName();
			File tmpToolboxDir = new File(spmToolboxDir, toolboxName);
			try {
				toolboxDir = toolboxDir.getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			toolboxBinds.put(toolboxDir.getAbsolutePath(), tmpToolboxDir.getAbsolutePath());
		}
		return toolboxBinds;
	}

	private static LinkedList<String> generateToolboxBindsParameters(HashMap<String, String> toolboxBinds) {
		LinkedList<String> toolboxBindParameters = new LinkedList<>();

		for (Map.Entry<String, String> entry : toolboxBinds.entrySet()) {
			toolboxBindParameters.add("--bind");
			toolboxBindParameters.add(entry.getKey());
			toolboxBindParameters.add(entry.getValue());
		}
		
		return toolboxBindParameters;
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
	public static void buildLaunchCmdStartAndWait(File matlabDir, File spmDir, List<File> activatedToolboxes,
			boolean devmode) {
		System.out.println(matlabDir.getAbsolutePath());
		System.out.println("Starting " + spmDir.getName());

		HashMap<String, String> toolboxBinds = generateToolboxBinds(spmDir, activatedToolboxes);
		LinkedList<String> toolboxBindParameters = generateToolboxBindsParameters(toolboxBinds);

		LinkedList<String> launchCommand = new LinkedList<>();
		launchCommand.add("nice");
		launchCommand.add("-n");
		launchCommand.add("+1");
		launchCommand.add("bwrap");
		launchCommand.add("--bind");
		launchCommand.add("/");
		launchCommand.add("/");
		launchCommand.add("--dev-bind");
		launchCommand.add("/dev");
		launchCommand.add("/dev");
//		launchCommand.add("--bind");
//		launchCommand.add(spmDir.getAbsolutePath());
//		launchCommand.add(tmpSpmDir.getAbsolutePath());
		launchCommand.addAll(toolboxBindParameters);
		launchCommand.add(matlabDir.getAbsolutePath() + "/bin/matlab"); // absolute path to matlab binary
		launchCommand.add("-r");
		String matlabCommands = generateMatlabPathCommand(spmDir, activatedToolboxes, toolboxBinds) + "cd('/spm-data');"
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
