package de.wwu.trap.SpmLauncher;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import Utils.FileComparator;

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
		File[] spms = spmDir.listFiles();
		// Hiermit wird die toString Methode der File Objekte, sodass in der
		// JComboBox nicht der ganze Pfad angegeben wird.
		for (int i = 0; i < spms.length; i++) {
			spms[i] = new File(spms[i].toString()) {
				private static final long serialVersionUID = 2344589028794682568L;

				@Override
				public String toString() {
					return this.getName();
				}

			};
		}
		Arrays.sort(spms, new FileComparator<File>());
		return spms;
	}

	/**
	 * 
	 */

	/**
	 * This method mounts the spm installation wth the chosen toolboxes to a
	 * temporary directory (e.g.
	 * App.MOUNT_DIR/6d9e5da2-cd28-43c8-af46-9f5e3e29d7de), so spm can be
	 * started with and only with the specified toolboxes
	 * 
	 * @param spmDir
	 *            the path to the chosen spm installation
	 * @param toolboxes
	 *            the paths to the chosen versions of the toolboxes
	 */
	public static boolean createMounts(UUID uuid, File spmDir, File[] toolboxes) {
		// TODO complete createMounts. Don't forget: log to info file
		boolean ret = false;

		File uuidDir = new File(App.MOUNT_DIR, "/" + uuid.toString());
		uuidDir.mkdirs();

		
		
		
		
		mount(spmDir, uuidDir);

		return ret;
	}

	/**
	 * This method checks, whether the SPM-installation has the necessary
	 * subdirs to create the mounts for the toolboxes
	 * 
	 * @param spmDir the dir of the SPM-installation
	 * @return whether the subdirs exist
	 */
	public static boolean checkForNecessarySubdirsForToolboxes(File spmDir) {
		boolean ret = false;
		//TODO complete check for subtirs in toolbox dir

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
			return false;
		}
		String oldRelativePath = oldPath.replaceFirst(App.MANAGED_SOFTWARE_DIR + "/", "");

		String newPath = newDir.getAbsolutePath();
		if (!newPath.startsWith(App.MOUNT_DIR + "/")) {
			return false;
		}
		String newRelativePath = newPath.replaceFirst(App.MOUNT_DIR + "/", "");

		if (!newDir.mkdirs() && !newDir.exists()) {
			System.err.println("Could not create" + newDir.getAbsolutePath());
			return false;
		}

		String[] cmd = new String[] { "sudo", App.MOUNT_SCRIPT, "-m", oldRelativePath, newRelativePath };

		try {
			Process p = new ProcessBuilder(cmd).start();

			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(p.getErrorStream()));
			// String line = "";
			// while ((line = br.readLine()) != null) {
			// System.out.println(line);
			// }
			//
			ret = p.waitFor() == 0;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return ret;
	}

}
