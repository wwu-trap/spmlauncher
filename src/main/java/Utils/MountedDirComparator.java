package Utils;

import java.io.File;
import java.util.Comparator;

/**
 * This comparator compares the File by the depth of their paths. This is
 * necessary if you want to unmount several dirs and if there are mounts in
 * subdirs of mounts. Here the deepest mount has to be unmounted first.
 * 
 * @author Kelvin Sarink
 */
public class MountedDirComparator implements Comparator<File> {

	
	@Override
	public int compare(File f1, File f2) {
		
		int depthFile1 = f1.getAbsolutePath().split("/").length;
		int depthFile2 = f2.getAbsolutePath().split("/").length;
		
		return (int)Math.signum(depthFile2 - depthFile1);
	}

}
