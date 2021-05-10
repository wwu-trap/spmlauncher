package de.wwu.trap.SpmLauncher.Utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileManipulator {
	
	public static File onlyNameInToString(File f) {
		f = new File(f.getAbsolutePath()) {
			private static final long serialVersionUID = 7025340296172367872L;

			@Override
			public String toString() {
				return this.getName();
			}
		};
		return f;
	}

	public static void onlyNameInToString(File... fs) {
		for (int i = 0; i < fs.length; i++) {
			fs[i] = onlyNameInToString(fs[i]);
		}
	}
	
	public static void onlyNameInToString(List<File> fs) {
		for (int i = 0; i < fs.size(); i++) {
			fs.set(i, onlyNameInToString(fs.get(i)));
		}
	}

	public static void replaceWithCanonicalPath(List<File> fs) {
		for (int i = 0; i < fs.size(); i++) {
			try {
				fs.set(i, fs.get(i).getCanonicalFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
