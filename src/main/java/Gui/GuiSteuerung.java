package Gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;

import Utils.FileComparator;
import Utils.FileManipulator;
import de.wwu.trap.SpmLauncher.App;
import de.wwu.trap.SpmLauncher.OSHandler;

public class GuiSteuerung {

	private Gui gui;

	public GuiSteuerung() {

	}

	public void startGui() {
		File[] spmVersions = OSHandler.getSpmVersions();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiSteuerung.this.gui = new Gui();
					GuiSteuerung.this.gui.initialize(spmVersions);
					GuiSteuerung.this.gui.frame.setVisible(true);

					GuiSteuerung.this.gui.spmVersionComboBox.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							chooseSpmVersion((File) GuiSteuerung.this.gui.spmVersionComboBox.getSelectedItem());
						}
					});

					chooseSpmVersion((File) GuiSteuerung.this.gui.spmVersionComboBox.getSelectedItem());


				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void chooseSpmVersion(File spmDir) {
		File spmToolboxDir = new File(App.MANAGED_SOFTWARE_DIR, "toolbox" + File.separatorChar + spmDir.getName());
		File[] toolboxes = spmToolboxDir.listFiles((dir) -> dir.isDirectory());
		Arrays.sort(toolboxes, new FileComparator<File>());
		if (toolboxes == null) {
			return;
		}

		gui.clearAndSetupToolboxes();

		for (File toolbox : toolboxes) {
			File[] toolboxVersions = toolbox.listFiles((dir) -> dir.isDirectory());
			FileManipulator.onlyNameInToString(toolboxVersions);
			Arrays.sort(toolboxVersions, new FileComparator<>(true));

			gui.addToolbox(toolboxVersions, false);
		}

		this.gui.frame.validate();
		this.gui.frame.repaint();
	}

}
