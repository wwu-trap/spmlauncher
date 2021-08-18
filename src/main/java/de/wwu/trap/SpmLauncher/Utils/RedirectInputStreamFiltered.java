package de.wwu.trap.SpmLauncher.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class RedirectInputStreamFiltered {
	public static String BYE_STRING = "Bye for now...";

	public static void filterAndRedirect(InputStream in, PrintStream out, Runnable onExit) {
		Thread p = new Thread() {
			private static final int DEFAULT_BUFFER_SIZE = 8192;

			public void run() {
				try {
					byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
					boolean continueRead = true;
					int read;
					while (continueRead && (read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
						String str = new String(buffer);
						out.write(buffer, 0, read);
						if (str.contains(BYE_STRING)) {
							continueRead = false;
							onExit.run();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		p.start();
	}

}
