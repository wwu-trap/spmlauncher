package Utils;

import java.io.File;

public class FileManipulator {

	public static void onlyNameInToString(File... fs) {
		for (int i = 0; i < fs.length; i++) {
			fs[i] = new File(fs[i].getAbsolutePath()) {
				private static final long serialVersionUID = 7025340296172367872L;

				@Override
				public String toString() {
					return this.getName();
				}
			};
		}

	}

}
