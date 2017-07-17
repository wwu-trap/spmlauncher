package Gui;

import java.awt.EventQueue;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import Utils.FileComparator;
import Utils.FileManipulator;
import de.wwu.trap.SpmLauncher.App;
import de.wwu.trap.SpmLauncher.OSHandler;

/**
 * This class starts and manages the gui. It holds the methods which add
 * features to the gui which aren't related to the appearance of the gui.
 * 
 * @author Kelvin Sarink
 */
public class GuiSteuerung {

	private Gui gui;
	private UUID uuid;

	public GuiSteuerung() {
		uuid = UUID.randomUUID();
	}

	public void startGui() {
		File[] spmVersions = OSHandler.getSpmVersions();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GuiSteuerung.this.gui = new Gui();
					GuiSteuerung.this.gui.initialize(spmVersions);
					GuiSteuerung.this.gui.frame.setVisible(true);

					GuiSteuerung.this.gui.spmVersionComboBox.addActionListener(
							(e) -> chooseSpmVersion((File) GuiSteuerung.this.gui.spmVersionComboBox.getSelectedItem()));

					chooseSpmVersion((File) GuiSteuerung.this.gui.spmVersionComboBox.getSelectedItem());

					GuiSteuerung.this.gui.bttnStartSpm.addActionListener((e) -> prepareAndStartSpm());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public void prepareAndStartSpm() {
		File spmDir = (File) this.gui.spmVersionComboBox.getSelectedItem();
		LinkedList<File> activatedToolboxes = new LinkedList<>();
		for (JComboBox<File> comboBox : this.gui.comboxBoxList) {
			if (comboBox.isEnabled()) {
				activatedToolboxes.add((File) comboBox.getSelectedItem());
			}
		}

		File[] toolboxes = activatedToolboxes.toArray(new File[]{});
		
		
		boolean mountResult = OSHandler.createMounts(uuid, spmDir, toolboxes);
		if(!mountResult){
			JOptionPane.showMessageDialog(this.gui.frame, "Could not mount the directories!", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		
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
