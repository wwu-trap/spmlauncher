package de.wwu.trap.SpmLauncher;

import java.awt.Font;
import java.io.File;

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
		lblSpmLauncher.setBounds(0, 0, 450, 59);
		lblSpmLauncher.setFont(new Font("Ubuntu", Font.BOLD, 18));
		lblSpmLauncher.setHorizontalAlignment(SwingConstants.CENTER);
		frame.getContentPane().add(lblSpmLauncher);

		JLabel lblSpmVersion = new JLabel("SPM Version");
		lblSpmVersion.setBounds(12, 71, 125, 20);
		frame.getContentPane().add(lblSpmVersion);

		JComboBox<File> comboBox = new JComboBox<>();
		comboBox.setModel(new DefaultComboBoxModel<File>(OSHandler.getSpmVersions()));
		comboBox.setBounds(182, 71, 246, 24);
		frame.getContentPane().add(comboBox);

		JLabel lblToolboxes = new JLabel("Toolbox");
		lblToolboxes.setBounds(12, 110, 100, 20);
		frame.getContentPane().add(lblToolboxes);
		
		
		/*
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 2, 5, 5));
		
		JCheckBox chckbxCat = new JCheckBox("cat");
		panel.add(chckbxCat);
		
		JComboBox comboBox_1 = new JComboBox();
		panel.add(comboBox_1);
		
		JCheckBox chckbxTfce = new JCheckBox("TFCE");
		panel.add(chckbxTfce);
		
		JComboBox comboBox_2 = new JComboBox();
		panel.add(comboBox_2);
		
		JCheckBox chckbxWasAnderesAsdasdasdasdasd = new JCheckBox("was anderes asdasdasdasdasd");
		panel.add(chckbxWasAnderesAsdasdasdasdasd);
		
		JComboBox comboBox_3 = new JComboBox();
		panel.add(comboBox_3);
		*/

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(12, 142, 426, 394);
		frame.getContentPane().add(scrollPane);
		
		
		Box verticalBox = Box.createVerticalBox();
		scrollPane.setViewportView(verticalBox);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut);
		
		Box horizontalBox = Box.createHorizontalBox();
		verticalBox.add(horizontalBox);
		horizontalBox.setMaximumSize(new Dimension(10000, 20));
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut);
		
		JCheckBox chckbxCat = new JCheckBox("caasdfasdfasdfasdft");
		horizontalBox.add(chckbxCat);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue);
		
		JComboBox comboBox_1 = new JComboBox();
		horizontalBox.add(comboBox_1);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalBox.add(horizontalStrut_1);
		
		
		
		JButton bttnStartSpm = new JButton("Start SPM");
		bttnStartSpm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		

		ImageIcon icon = new ImageIcon(Gui.class.getResource("/spm12.png"));
		frame.setIconImage(icon.getImage());
	}

	public JFrame getJFrame() {
		return this.frame;
	}

	public void setVisible(boolean b) {
		frame.setVisible(b);
	}
}
