package de.wwu.trap.Gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import de.wwu.trap.SpmLauncher.App;
import de.wwu.trap.SpmLauncher.OSHandler;
import de.wwu.trap.Utils.FileComparator;
import de.wwu.trap.Utils.FileManipulator;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

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
	public void launchSPM(ActionEvent e) {
		
		new Thread(){
			public void run() {
				prepareAndStartSpm();
			}
		}.start();
		
		
	}
	
	@Override
    public void start(Stage stage) throws Exception {
        String fxmlFile = "/MainGui.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = (Parent) loader.load(getClass().getResourceAsStream(fxmlFile));
        
        Scene scene = new Scene(rootNode);
//        scene.getStylesheets().add("/styles/styles.css");

        stage.setTitle("SPMLauncher.fx");
        stage.setScene(scene);
        
        stage.show();
    }
	
	public void prepareAndStartSpm() {
		
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
			JOptionPane.showMessageDialog(null, "Could not mount the directories!", "Error",
					JOptionPane.ERROR_MESSAGE);
			OSHandler.umountAllDirs(mountResult, App.LAUNCHER_UUID.toString());
			System.exit(1);
			return;
		}

		Thread p1 = new Thread() {
			@Override
			public void run() {
				File tmpSpmDir = new File(App.MOUNT_DIR + "/" + App.LAUNCHER_UUID.toString());
				OSHandler.startSpmAndWait(tmpSpmDir);
			}
		};
		p1.start();

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		/*
		 * SPM versions
		 */
		File[] spmVersions = OSHandler.getSpmVersions();
		spmComboBox.getItems().addAll(spmVersions);
		spmComboBox.valueProperty().addListener(new ChangeListener<File>() {

			@Override
			public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
				chooseSpmVersion(newValue);
			}

		});
		spmComboBox.getSelectionModel().selectFirst();

		/*
		 * Changelog
		 */
		File changelogFile = new File(App.MANAGED_SOFTWARE_DIR, "changelog.md");
		if (changelogFile == null || !changelogFile.exists()) {
			System.out.println("No or empty changelog! (" + changelogFile.getAbsolutePath() + ")");
			return;
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
				FileManipulator.onlyNameInToString(toolboxVersions);
				Arrays.sort(toolboxVersions, new FileComparator<>(true));

				File toolboxIsStandard = new File(toolbox, "standard");

				boolean isStandard = false;
				if (toolboxIsStandard != null) {
					isStandard = toolboxIsStandard.exists();
				}

				ComboBox<File> comboBox = new ComboBox<>(FXCollections.observableArrayList(toolboxVersions));
				comboxBoxList.add(comboBox);
				
				CheckBox checkBox = new CheckBox(toolboxVersions[0].getParentFile().getName());
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

}
