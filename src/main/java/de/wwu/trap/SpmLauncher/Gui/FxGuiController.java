package de.wwu.trap.SpmLauncher.Gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.controlsfx.control.ToggleSwitch;

import de.wwu.trap.SpmLauncher.App;
import de.wwu.trap.SpmLauncher.OSHandler;
import de.wwu.trap.SpmLauncher.Utils.FileComparator;
import de.wwu.trap.SpmLauncher.Utils.FileManipulator;
import de.wwu.trap.SpmLauncher.Utils.TooltipManipulator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;

public class FxGuiController extends Application implements Initializable {
	@FXML
	private WebView changelogView;

	@FXML
	private GridPane toolboxPane;

	@FXML
	private ScrollPane toolBoxScroll;

	@FXML
	private ComboBox<File> spmComboBox;

	@FXML
	private ComboBox<File> matlabComboBox;

	@FXML
	private Tooltip tt1;

	@FXML
	private Tooltip ttDarkMode;

	@FXML
	private ToggleSwitch devmodeCheckBox;

	@FXML
	private ScrollPane changelogPane;

	@FXML
	private VBox root;

	@FXML
	private SplitPane splitPane;

	@FXML
	private ToggleSwitch darkModeSwitch;

	@FXML
	public void launchSPM(ActionEvent e) {

		new Thread() {
			public void run() {
				prepareAndStartSpm();
			}
		}.start();

	}

	@FXML
	public void applyDarkTheme(MouseEvent e) {
		if (e.getSource() instanceof ToggleSwitch) {
			ToggleSwitch ts = (ToggleSwitch) e.getSource();
			applyDarkTheme(ts.isSelected());
		}
	}

	public void applyDarkTheme(boolean dark) {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.put("DARK_MODE", Boolean.toString(dark));

		if (dark) {
			changelogView.getEngine()
					.setUserStyleSheetLocation(getClass().getResource("/changelog-dark.css").toString());
			new JMetro(Style.DARK).setParent(root);
			toolBoxScroll.setStyle("-fx-background-color: #323232");
			toolBoxScroll.setStyle("-fx-border-color: #000000");
		} else {
			changelogView.getEngine().setUserStyleSheetLocation(getClass().getResource("/changelog.css").toString());
			new JMetro(Style.LIGHT).setParent(root);
			toolBoxScroll.setStyle("-fx-background-color: #e6e6e6");
			toolBoxScroll.setStyle("-fx-border-color: #FFFFFF");
		}
	}

	private HashMap<File, String> tooltips;
	private JMetro jMetro = new JMetro(Style.LIGHT);

	@Override
	public void start(Stage stage) throws Exception {
		String fxmlFile = "/MainGui.fxml";
		FXMLLoader loader = new FXMLLoader();
		Parent rootNode = loader.load(FxGuiController.class.getResourceAsStream(fxmlFile));

		Scene scene = new Scene(rootNode);

		jMetro.setScene(scene);

		String versionNumber = getClass().getPackage().getImplementationVersion();
		String title = "SPMLauncher";
		if (versionNumber != null)
			title += " v" + versionNumber;

		stage.setTitle(title);
		stage.getIcons().add(new Image(getClass().getResourceAsStream("/spm12.png")));
		stage.setScene(scene);

		stage.show();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		root.getStyleClass().add(JMetroStyleClass.BACKGROUND);

		tooltips = OSHandler.getTooltips();
		TooltipManipulator.makeTooltipInstant(tt1);
		TooltipManipulator.makeTooltipInstant(ttDarkMode);

		/*
		 * MATLAB versions
		 */
		List<File> matlabVersions = OSHandler.getMatlabVersions();
		if (matlabVersions != null)
			matlabComboBox.getItems().addAll(matlabVersions);
		matlabComboBox.getSelectionModel().selectLast();
		matlabComboBox.setCellFactory(param -> new ListCell<File>() {
			@Override
			protected void updateItem(File item, boolean empty) {
				super.updateItem(item, empty);

				if (item != null) {
					setText(item.getName());
					String tooltipText = item.getAbsolutePath();
					Tooltip tt = new Tooltip(tooltipText);
					TooltipManipulator.makeTooltipInstant(tt);

					setTooltip(tt);
				} else {
					setText(null);
					setTooltip(null);
				}
			}
		});

		/*
		 * SPM versions
		 */
		File[] spmVersions = OSHandler.getSpmVersions();
		if (spmVersions != null)
			spmComboBox.getItems().addAll(spmVersions);
		spmComboBox.valueProperty().addListener(new ChangeListener<File>() {

			@Override
			public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
				chooseSpmVersion(newValue);
			}

		});
		spmComboBox.getSelectionModel().selectLast();

		spmComboBox.setCellFactory(param -> new ListCell<File>() {
			@Override
			protected void updateItem(File item, boolean empty) {
				super.updateItem(item, empty);

				if (item != null) {
					setText(item.getName());
					String tooltipText;
					if (tooltips != null && (tooltipText = tooltips.get(item)) != null) {
						Tooltip tt = new Tooltip(tooltipText);
						TooltipManipulator.makeTooltipInstant(tt);

						setTooltip(tt);
					} else {
						setTooltip(null);
					}
				} else {
					setText(null);
					setTooltip(null);
				}
			}
		});

		/*
		 * Changelog
		 */
		File changelogFile = new File(App.MANAGED_SOFTWARE_DIR, "changelog.md");
		if (changelogFile == null || !changelogFile.exists()) {
			System.out.println("No or empty changelog! (" + changelogFile.getAbsolutePath() + ")");

			/*
			 * Remove the changelog view
			 */
			splitPane.getItems().remove(changelogPane);
			root.setPrefWidth(root.getPrefWidth() / 2.0);
		} else {
			/*
			 * Parsing the Markdown file to HTML
			 */
			Parser parser = Parser.builder().build();
			String changelog = "";
			try {
				Node document = parser.parseReader(new InputStreamReader(new FileInputStream(changelogFile)));
				HtmlRenderer renderer = HtmlRenderer.builder().build();
				changelog = "<html><body>\n" + renderer.render(document) + "\n</body></html>";
			} catch (IOException e) {
				e.printStackTrace();
			}

			/*
			 * Add changelog to the WebView changelogView and add the css
			 */
			changelogView.getEngine().loadContent(changelog);
			changelogView.getEngine().setUserStyleSheetLocation(getClass().getResource("/changelog.css").toString());

		}

		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String enableDarkMode = prefs.get("DARK_MODE", "false");
		if (enableDarkMode.equalsIgnoreCase("true")) {
			darkModeSwitch.setSelected(true);
			applyDarkTheme(true);
		} else {
			applyDarkTheme(false);
		}

	}

	private LinkedList<ComboBox<File>> comboxBoxList = new LinkedList<>();
	private int toolboxCount = 0;

	public void chooseSpmVersion(File spmDir) {
		if (spmDir == null)
			return;
		toolboxPane.getChildren().clear();
		comboxBoxList.clear();
		toolboxCount = 0;

		File spmToolboxDir = new File(App.MANAGED_SOFTWARE_DIR, "toolbox" + File.separatorChar + spmDir.getName());
		File[] toolboxes = spmToolboxDir.listFiles((dir) -> dir.isDirectory());

		if (toolboxes != null) {
			Arrays.sort(toolboxes, new FileComparator<File>());

			for (File toolbox : toolboxes) {
				File[] toolboxVersions = toolbox.listFiles((dir) -> dir.isDirectory());
				if (toolboxVersions.length == 0)
					continue;
				FileManipulator.onlyNameInToString(toolboxVersions);
				Arrays.sort(toolboxVersions, new FileComparator<>(true));

				File toolboxIsStandard = new File(toolbox, "standard");

				boolean isStandard = false;
				if (toolboxIsStandard != null) {
					isStandard = toolboxIsStandard.exists();
				}

				ComboBox<File> comboBox = new ComboBox<>(FXCollections.observableArrayList(toolboxVersions));
				comboxBoxList.add(comboBox);

				File nonVersionedToolboxDir = toolboxVersions[0].getParentFile();
				CheckBox checkBox = new CheckBox(nonVersionedToolboxDir.getName());
				{
					/*
					 * Get real path of toolbox (if softlinks are used for multiple builds of the
					 * same spm version
					 */
					File canonicalNonVersionedToolboxDir = nonVersionedToolboxDir;
					try {
						canonicalNonVersionedToolboxDir = new File(nonVersionedToolboxDir.getCanonicalPath());
					} catch (IOException e) {
					}

					/*
					 * set Tooltip for CheckBox of the Toolbox
					 */
					String tooltipText;
					if (tooltips != null && (tooltipText = tooltips.get(canonicalNonVersionedToolboxDir)) != null) {
						Tooltip tt = new Tooltip(tooltipText);
						TooltipManipulator.makeTooltipInstant(tt);

						checkBox.setTooltip(tt);
					}
				}

				checkBox.setPrefWidth(Double.MAX_VALUE);
				checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {

					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
							Boolean newValue) {
						comboBox.setDisable(!newValue);
					}

				});

				checkBox.setSelected(isStandard);
				comboBox.setDisable(!isStandard);
				comboBox.setCellFactory(param -> new ListCell<File>() {
					@Override
					protected void updateItem(File item, boolean empty) {
						super.updateItem(item, empty);

						if (item != null) {
							setText(item.getName());
							String tooltipText;
							/*
							 * Get real path of toolbox (if softlinks are used for multiple builds of the
							 * same spm version
							 */
							File canonicalNonVersionedToolboxDir = item;
							try {
								canonicalNonVersionedToolboxDir = new File(item.getCanonicalPath());
							} catch (IOException e) {
							}
							if (tooltips != null
									&& (tooltipText = tooltips.get(canonicalNonVersionedToolboxDir)) != null) {
								Tooltip tt = new Tooltip(tooltipText);
								TooltipManipulator.makeTooltipInstant(tt);

								setTooltip(tt);
							} else {
								setTooltip(null);
							}
						} else {
							setText(null);
							setTooltip(null);
						}
					}
				});

				comboBox.getSelectionModel().selectFirst();
				comboBox.setPrefWidth(Double.MAX_VALUE);
				toolboxPane.add(checkBox, 0, toolboxCount);
				toolboxPane.add(comboBox, 1, toolboxCount);
				GridPane.setHgrow(comboBox, Priority.NEVER);
				GridPane.setHalignment(comboBox, HPos.RIGHT);

				toolboxCount++;
			}
		}
	}

	boolean spmAlreadyStarted = false;

	public void prepareAndStartSpm() {

		if (spmAlreadyStarted) {
			return;
		} else {
			spmAlreadyStarted = true;
		}

		File spmDir = spmComboBox.getValue();
		LinkedList<File> activatedToolboxes = new LinkedList<>();
		for (ComboBox<File> comboBox : this.comboxBoxList) {
			if (!comboBox.isDisabled()) {
				activatedToolboxes.add(comboBox.getValue());
			}
		}

		Platform.exit();

		LinkedList<File> mountResult = OSHandler.createMounts(spmDir, activatedToolboxes);

		Thread shutdownHook = new Thread() {

			@Override
			public void run() {

				try {
					OSHandler.umountAllDirs(mountResult, App.LAUNCHER_UUID.toString());
					OSHandler.p.destroy();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println("Shutdownhook completed");
				}
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		if (activatedToolboxes.size() + 1 != mountResult.size()) {
			JOptionPane.showMessageDialog(null, "Could not mount the directories!", "Error", JOptionPane.ERROR_MESSAGE);
			OSHandler.umountAllDirs(mountResult, App.LAUNCHER_UUID.toString());
			System.exit(1);
			return;
		}

		Thread p1 = new Thread() {
			@Override
			public void run() {
				File tmpSpmDir = new File(App.MOUNT_DIR + "/" + App.LAUNCHER_UUID.toString());
				File matlabDir = matlabComboBox.getSelectionModel().getSelectedItem();
				OSHandler.buildLaunchCmdStartAndWait(matlabDir, tmpSpmDir, activatedToolboxes,
						devmodeCheckBox.isSelected());
			}
		};
		p1.start();

	}

}
