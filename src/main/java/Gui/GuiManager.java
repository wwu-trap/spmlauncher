package Gui;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import Utils.FileComparator;
import Utils.FileManipulator;
import de.wwu.trap.SpmLauncher.App;
import de.wwu.trap.SpmLauncher.OSHandler;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebView;

/**
 * This class starts and manages the gui. It holds the methods which add
 * features to the gui which aren't related to the appearance of the gui.
 * 
 * @author Kelvin Sarink
 */
public class GuiManager {

	private Gui gui;

	public GuiManager() {

	}

	public void startGui() {
		File[] spmVersions = OSHandler.getSpmVersions();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiManager.this.gui = new Gui();
					GuiManager.this.gui.initialize(spmVersions);
					GuiManager.this.gui.frame.setVisible(true);

					GuiManager.this.gui.spmVersionComboBox.addActionListener(
							(e) -> chooseSpmVersion((File) GuiManager.this.gui.spmVersionComboBox.getSelectedItem()));

					chooseSpmVersion((File) GuiManager.this.gui.spmVersionComboBox.getSelectedItem());
					getChangelog();

					GuiManager.this.gui.bttnStartSpm.addActionListener((e) -> prepareAndStartSpm());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void getChangelog() {
		Thread p1 = new Thread() {
			@Override
			public void run() {

				File changelogFile = new File(App.MANAGED_SOFTWARE_DIR, "changelog.md");
				if (changelogFile == null || !changelogFile.exists()) {
					System.out.println("No or empty changelog! (" + changelogFile.getAbsolutePath() + ")");
					return;
				}
				
				
				/*
				 * Loading Changelog file
				 */
				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(changelogFile)));
					String line = "";
					while ((line = br.readLine()) != null) {
						sb.append(line + "\n");
					}
					br.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				/*
				 * Parsing the Markdown file to HTML
				 */
				Parser parser = Parser.builder().build();
				Node document = parser.parse(sb.toString());
				HtmlRenderer renderer = HtmlRenderer.builder().build();
				String changelog = "<html><body>\n" + renderer.render(document) + "\n</body></html>";

				/*
				 * Packing the html in the JavaFx Webview
				 */
				
				Platform.runLater(() -> { 
					WebView webView = new WebView();
					webView.getEngine().setUserStyleSheetLocation(getClass().getResource("/changelog.css").toString());
					webView.getEngine().loadContent(changelog);					
					GuiManager.this.gui.changelogPanel.setScene(new Scene(webView));
				});
				
				
			}
		};
		p1.start();
	}

	public void prepareAndStartSpm() {

		this.gui.bttnStartSpm.setEnabled(false);
		File spmDir = (File) this.gui.spmVersionComboBox.getSelectedItem();
		LinkedList<File> activatedToolboxes = new LinkedList<>();
		for (JComboBox<File> comboBox : this.gui.comboxBoxList) {
			if (comboBox.isEnabled()) {
				activatedToolboxes.add((File) comboBox.getSelectedItem());
			}
		}

		LinkedList<File> mountResult = OSHandler.createMounts(spmDir, activatedToolboxes);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// System.out.println("Killing SPM and unmounting all dirs
				// because of sigkill");
				OSHandler.p.destroy();
				OSHandler.umountAllDirs(mountResult, App.LAUNCHER_UUID.toString(), true);
			}
		});

		if (activatedToolboxes.size() + 1 != mountResult.size()) {
			JOptionPane.showMessageDialog(this.gui.frame, "Could not mount the directories!", "Error",
					JOptionPane.ERROR_MESSAGE);
			OSHandler.umountAllDirs(mountResult, App.LAUNCHER_UUID.toString(), false);
			System.exit(1);
			return;
		}

		this.gui.frame.setVisible(false);
		this.gui.frame.dispose();
		this.gui.frame = null;
		this.gui = null;
		Thread p1 = new Thread() {
			@Override
			public void run() {

				File tmpSpmDir = new File(App.MOUNT_DIR + "/" + App.LAUNCHER_UUID.toString());
				OSHandler.startSpmAndWait(tmpSpmDir);

				OSHandler.umountAllDirs(mountResult, App.LAUNCHER_UUID.toString(), true);
				System.exit(0);

			}
		};
		p1.start();

	}

	public void chooseSpmVersion(File spmDir) {
		if (spmDir == null)
			return;
		gui.clearAndSetupToolboxes();
		File spmToolboxDir = new File(App.MANAGED_SOFTWARE_DIR, "toolbox" + File.separatorChar + spmDir.getName());
		File[] toolboxes = spmToolboxDir.listFiles((dir) -> dir.isDirectory());

		if (toolboxes != null) {
			Arrays.sort(toolboxes, new FileComparator<File>());

			for (File toolbox : toolboxes) {
				File[] toolboxVersions = toolbox.listFiles((dir) -> dir.isDirectory());
				FileManipulator.onlyNameInToString(toolboxVersions);
				Arrays.sort(toolboxVersions, new FileComparator<>(true));

				File toolboxIsStandard = new File(toolbox, "standard");

				boolean isStandard = false;
				if (toolboxIsStandard != null) {
					isStandard = toolboxIsStandard.exists();
				}

				gui.addToolbox(toolboxVersions, isStandard);
			}
		}

		this.gui.frame.validate();
		this.gui.frame.repaint();
	}

}
