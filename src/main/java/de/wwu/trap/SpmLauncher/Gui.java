package de.wwu.trap.SpmLauncher;

import java.awt.Font;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.ScrollPaneConstants;
import javax.swing.Box;
import javax.swing.JCheckBox;
import java.awt.Component;
import java.awt.Dimension;

public class Gui {

	private JFrame frame;
	private JScrollPane scrollPane;
	private Box verticalBox;

	/**
	 * Create the application.
	 */
	public Gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setBounds(100, 100, 450, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setLocationRelativeTo(null);
		frame.getContentPane().setLayout(null);

		JLabel lblSpmLauncher = new JLabel("SPM Launcher");
		lblSpmLauncher.setBounds(0, 0, 434, 59);
		lblSpmLauncher.setFont(new Font("Ubuntu", Font.BOLD, 18));
		lblSpmLauncher.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lblSpmLauncher);

		JLabel lblSpmVersion = new JLabel("SPM Version");
		lblSpmVersion.setBounds(12, 71, 125, 20);
		frame.getContentPane().add(lblSpmVersion);

		final JComboBox<File> comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<File>(OSHandler.getSpmVersions()));
		comboBox.setBounds(182, 71, 242, 24);
		frame.getContentPane().add(comboBox);
		comboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object chosen = comboBox.getSelectedItem();
				if (chosen instanceof File) {
					chooseSpmVersion((File) chosen);
				}

			}
		});

		JLabel lblToolboxes = new JLabel("Toolbox");
		lblToolboxes.setBounds(12, 111, 100, 20);
		frame.getContentPane().add(lblToolboxes);

		/*
		 * JPanel panel = new JPanel(); panel.setLayout(new GridLayout(3, 2, 5,
		 * 5));
		 * 
		 * JCheckBox chckbxCat = new JCheckBox("cat"); panel.add(chckbxCat);
		 * 
		 * JComboBox comboBox_1 = new JComboBox(); panel.add(comboBox_1);
		 * 
		 * JCheckBox chckbxTfce = new JCheckBox("TFCE"); panel.add(chckbxTfce);
		 * 
		 * JComboBox comboBox_2 = new JComboBox(); panel.add(comboBox_2);
		 * 
		 * JCheckBox chckbxWasAnderesAsdasdasdasdasd = new
		 * JCheckBox("was anderes asdasdasdasdasd");
		 * panel.add(chckbxWasAnderesAsdasdasdasdasd);
		 * 
		 * JComboBox comboBox_3 = new JComboBox(); panel.add(comboBox_3);
		 */

		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(12, 142, 412, 367);
		frame.getContentPane().add(scrollPane);

		JButton bttnStartSpm = new JButton("Start SPM");
		bttnStartSpm.setLocation(158, 520);
		bttnStartSpm.setSize(111, 30);
		bttnStartSpm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		frame.getContentPane().add(bttnStartSpm);

		verticalBox = Box.createVerticalBox();
		scrollPane.setViewportView(verticalBox);

		Component verticalStrut = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut);

		ImageIcon icon = new ImageIcon(Gui.class.getResource("/spm12.png"));
		frame.setIconImage(icon.getImage());

		Object chosen = comboBox.getSelectedItem();
		chooseSpmVersion((File) chosen);
	}

	private void chooseSpmVersion(File spmDir) {
		System.out.println(spmDir);
		verticalBox.removeAll();

		File spmToolboxDir = new File(App.MANAGED_SOFTWARE_DIR, "toolbox" + File.separatorChar + spmDir.getName());
		System.out.println(spmToolboxDir);

		File[] toolboxes = spmToolboxDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		if (toolboxes == null) {
			return;
		}

		for (File toolbox : toolboxes) {
			File[] toolboxVersions = toolbox.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});

			addToolBox(toolboxVersions);
		}
		frame.validate();
		frame.repaint();
	}

	private void addToolBox(File[] versions) {
		Arrays.sort(versions, Collections.reverseOrder());
		if (versions == null || versions.length == 0) {
			return;
		}

		for (int i = 0; i < versions.length; i++) {
			versions[i] = new File(versions[i].getPath()) {
				private static final long serialVersionUID = -3418093095462240036L;

				@Override
				public String toString() {
					return this.getName();
				}
			};
		}

		Component verticalStrut = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut);

		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setMaximumSize(new Dimension(10000, 30));

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut);

		final JCheckBox chckbxCat = new JCheckBox(versions[0].getParentFile().getName());
		horizontalBox.add(chckbxCat);

		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue);

		final JComboBox<File> comboBox_1 = new JComboBox<File>(versions);
		comboBox_1.setEnabled(false);
		horizontalBox.add(comboBox_1);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut_1);

		chckbxCat.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				comboBox_1.setEnabled(chckbxCat.isSelected());
			}
		});

		verticalBox.add(horizontalBox);

	}

	public JFrame getJFrame() {
		return this.frame;
	}

	public void setVisible(boolean b) {
		frame.setVisible(b);
	}
}
