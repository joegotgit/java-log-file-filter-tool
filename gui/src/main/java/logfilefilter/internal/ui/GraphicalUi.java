package logfilefilter.internal.ui;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logfilefilter.JavaLogFileFilter;

public class GraphicalUi extends Application {
	TextField sourceFile = new TextField();
	TextField targetFile = new TextField();
	TextField exclude = new TextField();
	TextField include = new TextField();
	TextField timeStamp = new TextField(JavaLogFileFilter.DEFAULT_DATE_PATTERN);

	public static void main(String[] args) {
		launch();
	}

	Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("Java log file filter tool");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(25, 25, 25, 25));

		Text scenetitle = new Text("Java Log-file filter tool");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		grid.add(scenetitle, 0, 0, 2, 1);

		final int labelColumn = 0;
		final int fieldColumn = 1;
		int currentRow = 1;
		Label sfLabel = new Label("Source file:");
		grid.add(sfLabel, labelColumn, currentRow);
		grid.add(sourceFile, fieldColumn, currentRow);
		Button openSourceFileChooser = new Button();
		openSourceFileChooser.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select source file");
			File result = fileChooser.showOpenDialog(primaryStage);
			if (result != null) {
				sourceFile.setText(result.getAbsolutePath());
			}
		});
		grid.add(openSourceFileChooser, 2, currentRow++);

		Label tfLabel = new Label("Target file:");
		grid.add(tfLabel, labelColumn, currentRow);
		targetFile.setPrefWidth(800);
		grid.add(targetFile, fieldColumn, currentRow);
		Button openTargetFileChooser = new Button();
		openTargetFileChooser.setOnAction((event) -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select target file");
			File result = fileChooser.showSaveDialog(primaryStage);
			if (result != null) {
				targetFile.setText(result.getAbsolutePath());
			}
		});
		grid.add(openTargetFileChooser, 2, currentRow++);

		Label excludeLabel = new Label("Exclude Regex-pattern");
		grid.add(excludeLabel, labelColumn, currentRow);
		grid.add(exclude, fieldColumn, currentRow++);

		Label includeLabel = new Label("Include Regex-pattern");
		grid.add(includeLabel, labelColumn, currentRow);
		grid.add(include, fieldColumn, currentRow++);

		Label tsLabel = new Label("Timestamp Regex-pattern");
		grid.add(tsLabel, labelColumn, currentRow);
		grid.add(timeStamp, fieldColumn, currentRow++);

		Button btn = new Button();
		btn.setText("Start filter");
		btn.setOnAction(this::startProcessor);

		HBox hbBtn = new HBox(10);
		hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
		hbBtn.getChildren().add(btn);
		grid.add(hbBtn, fieldColumn, currentRow++);

		primaryStage.setScene(new Scene(grid, 1024, 250));
		primaryStage.show();
	}

	public void startProcessor(ActionEvent event) {
		JavaLogFileFilter processor = new JavaLogFileFilter();

		try {
			setPropertyMustNotEmpty("Source file", sourceFile::getText, processor::setSourceFilePath);
			setPropertyMustNotEmpty("Target file", targetFile::getText, processor::setTargetFilePath);

			setPropertyIfNotEmpty(exclude::getText, processor::setExcludePattern);
			setPropertyIfNotEmpty(include::getText, processor::setIncludePattern);
			setPropertyIfNotEmpty(timeStamp::getText, processor::setDatePattern);

			processor.execute();
		} catch (Exception e) {
			// StringWriter sw = new StringWriter();
			// PrintWriter pw = new PrintWriter(sw);
			// e.printStackTrace(pw);

			final Stage dialog = new Stage();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(primaryStage);
			VBox dialogVbox = new VBox(20);
			// dialogVbox.getChildren().add(new Text(sw.toString()));
			dialogVbox.getChildren().add(new Text(e.getMessage()));
			Scene dialogScene = new Scene(dialogVbox, 300, 200);
			dialog.setScene(dialogScene);
			dialog.show();
		}
	}

	private void setPropertyMustNotEmpty(String attribute, Supplier<String> stringSupplier, Consumer<String> consumer) {
		String string = stringSupplier.get();
		Objects.requireNonNull(string);

		if (string.isEmpty()) {
			throw new IllegalArgumentException(attribute + " must not be empty");
		}
		consumer.accept(string);
	}

	private void setPropertyIfNotEmpty(Supplier<String> stringSupplier, Consumer<String> consumer) {
		String string = stringSupplier.get();
		if (string != null && !string.isEmpty()) {
			consumer.accept(string);
		}
	}
}
