package de.wwu.trap.SpmLauncher;

import java.io.File;
import java.util.Arrays;

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


}
