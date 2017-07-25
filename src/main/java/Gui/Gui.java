package Gui;

import java.awt.Font;
import java.io.File;
import java.util.LinkedList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.LayoutStyle.ComponentPlacement;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Box;
import javax.swing.JCheckBox;

import java.awt.BorderLayout;
import java.awt.Component;

public class Gui {

	private JScrollPane scrollPane;
	private Box verticalBox;

	JFrame frame;
	JComboBox<File> spmVersionComboBox;
	JButton bttnStartSpm;

	/**
	 * Create the application.
	 */
	public Gui() {

	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize(File[] spmVersions) {

		frame = new JFrame("SPM Launcher");
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

		spmVersionComboBox = new JComboBox<>();
		spmVersionComboBox.setModel(new DefaultComboBoxModel<File>(spmVersions));
		spmVersionComboBox.setBounds(182, 71, 242, 24);
		frame.getContentPane().add(spmVersionComboBox);

		JLabel lblToolboxes = new JLabel("Toolbox");
		lblToolboxes.setBounds(12, 111, 100, 20);
		frame.getContentPane().add(lblToolboxes);

		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(12, 142, 412, 367);
		frame.getContentPane().add(scrollPane);

		bttnStartSpm = new JButton("Start SPM");
		bttnStartSpm.setLocation(158, 520);
		bttnStartSpm.setSize(111, 30);
		frame.getContentPane().add(bttnStartSpm);

		verticalBox = Box.createVerticalBox();
		scrollPane.setViewportView(verticalBox);

		Component verticalStrut = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut);

		ImageIcon icon = new ImageIcon(Gui.class.getResource("/spm12.png"));
		frame.setIconImage(icon.getImage());
		
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		
//		clearAndSetupToolboxes();
	}

	private GroupLayout groupLayout;
	LinkedList<JComboBox<File>> comboxBoxList = new LinkedList<>();
	private ParallelGroup checkBoxGroup;
	private ParallelGroup comboBoxGroup;
	private SequentialGroup pairGroup;

	public void clearAndSetupToolboxes() {
		comboxBoxList.clear();
		JPanel panel = new JPanel();
		this.groupLayout = new GroupLayout(panel);

		this.checkBoxGroup = groupLayout.createParallelGroup(Alignment.LEADING);
		this.comboBoxGroup = groupLayout.createParallelGroup(Alignment.LEADING);
		this.pairGroup = groupLayout.createSequentialGroup().addGap(5);

		groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
				.createSequentialGroup().addGroup(checkBoxGroup).addGap(5).addGroup(comboBoxGroup).addContainerGap()));

		groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(pairGroup));

		panel.setLayout(groupLayout);
		
		
		frame.getContentPane().remove(scrollPane);
		scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setBounds(12, 142, 412, 367);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

	}

	public void addToolbox(File[] toolBoxVersionDirs, boolean selected) {
		if(toolBoxVersionDirs == null || toolBoxVersionDirs.length == 0){
			return;
		}
		
		JComboBox<File> comboBox = new JComboBox<>(toolBoxVersionDirs);
		JCheckBox checkBox = new JCheckBox(toolBoxVersionDirs[0].getParentFile().getName()); 
		checkBox.setSelected(selected);
		checkBox.addActionListener((x) -> comboBox.setEnabled(checkBox.isSelected()));
		comboBox.setEnabled(checkBox.isSelected());
		
		checkBoxGroup.addComponent(checkBox, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
		comboBoxGroup.addComponent(comboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);

		pairGroup
				.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,GroupLayout.PREFERRED_SIZE)
						.addComponent(checkBox))
				.addPreferredGap(ComponentPlacement.UNRELATED);

	}

}
