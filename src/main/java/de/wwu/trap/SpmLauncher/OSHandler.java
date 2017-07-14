package de.wwu.trap.SpmLauncher;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class OSHandler {

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
		Arrays.sort(spms);
		return spms;
	}

	public void createMounts(File spmDir, File[] toolboxes) {
		UUID uuid = UUID.randomUUID();

		File uuidDir = new File(App.MOUNT_DIR, "/" + uuid.toString());
		uuidDir.mkdirs();

	}

	public static void main(String[] args) {
		UUID uuid = UUID.randomUUID();
		File oldDir = new File("/opt/applications/SPMLauncher/ManagedSoftware/toolbox");
		File newDir = new File(App.MOUNT_DIR + "/" + uuid.toString());
//		System.out.println(newDir + " " + newDir.mkdirs());

		mount(oldDir, newDir);
		
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		umount(newDir);
	}

	public static void umount(File dir) {
		String path = dir.getAbsolutePath();
		if (!path.startsWith(App.MOUNT_DIR + "/")) {
			return;
		}
		String relativePath = path.replaceFirst(App.MOUNT_DIR + "/", "");

		String[] cmd = new String[] { "sudo", App.MOUNT_SCRIPT, "-u", relativePath};

		try {
			/*Process p =*/ new ProcessBuilder(cmd).start();

//			BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			String line = "";
//			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//			}

//			System.out.println(p.waitFor());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean empty = dir.listFiles().length == 0;
		if(dir.exists() && dir.isDirectory() && empty){
			dir.delete();
		}
		
	}

	public static void mount(File oldDir, File newDir) {
		String oldPath = oldDir.getAbsolutePath();
		if (!oldPath.startsWith(App.MANAGED_SOFTWARE_DIR + "/")) {
			return;
		}
		String oldRelativePath = oldPath.replaceFirst(App.MANAGED_SOFTWARE_DIR + "/", "");

		String newPath = newDir.getAbsolutePath();
		if (!newPath.startsWith(App.MOUNT_DIR + "/")) {
			return;
		}
		String newRelativePath = newPath.replaceFirst(App.MOUNT_DIR + "/", "");

		if (!newDir.mkdirs() && !newDir.exists()) {
			System.err.println("Could not create" + newDir.getAbsolutePath());
			return;
		}

		String[] cmd = new String[] { "sudo", App.MOUNT_SCRIPT, "-m", oldRelativePath, newRelativePath };

		try {
			/*Process p =*/ new ProcessBuilder(cmd).start();

//			BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
//			String line = "";
//			while ((line = br.readLine()) != null) {
//				System.out.println(line);
//			}
//
//			System.out.println(p.waitFor());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
