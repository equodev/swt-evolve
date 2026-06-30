package dev.equo.javafx;

import dev.equo.swt.Config;
import javafx.embed.swt.FXCanvas;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A richer JavaFX scene embedded in SWT Evolve via {@link FXCanvas}: a small
 * "dashboard" combining a styled header, an interactive control panel
 * (text field, slider + progress bar, radio buttons, check box, button), a
 * {@link TableView}, and a {@link BarChart} — all rendered by real JavaFX (CSS,
 * layout, charts intact) and composited onto the Evolve canvas through Flutter.
 *
 * <p>Exercises pointer (clicks, slider drag), keyboard (typing into the field),
 * and rich rendering (charts, table, gradients, CSS). Interactions update the
 * status bar live, proving the embedded scene is fully interactive.</p>
 *
 * <p>v1 embedding limitations apply: dropdown/menu <em>popups</em> open their own
 * JavaFX stage (not yet hosted) and mouse-wheel scrolling is not yet forwarded,
 * so this demo deliberately uses inline controls and a short, non-scrolling
 * table.</p>
 */
public class FXCanvasDashboardSnippet {

    public static void main(String[] args) {
        Config.forceEquo();

        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("FXCanvasDashboardSnippet");
        shell.setLayout(new FillLayout());

        FXCanvas canvas = new FXCanvas(shell, SWT.NONE);
        canvas.setScene(createScene());

        shell.setSize(760, 520);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }
        display.dispose();
    }

    private static Scene createScene() {
        final Label status = new Label("Ready.");
        status.setStyle("-fx-text-fill: #2b3a4a; -fx-font-size: 12px;");

        // --- Header -------------------------------------------------------
        Label title = new Label("JavaFX Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label subtitle = new Label("running inside SWT Evolve (Flutter) via FXCanvas");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #e8eef7;");
        VBox header = new VBox(2, title, subtitle);
        header.setPadding(new Insets(14, 18, 14, 18));
        header.setStyle("-fx-background-color: linear-gradient(to right, #2b6cb0, #4a90d9);");

        // --- Left: interactive control panel ------------------------------
        TextField nameField = new TextField();
        nameField.setPromptText("Type your name…");
        nameField.textProperty().addListener((o, a, b) ->
                status.setText(b.isEmpty() ? "Ready." : "Hello, " + b + "!"));

        Slider slider = new Slider(0, 100, 40);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setMajorTickUnit(25);
        ProgressBar progress = new ProgressBar(0.4);
        progress.setMaxWidth(Double.MAX_VALUE);
        slider.valueProperty().addListener((o, a, b) -> {
            progress.setProgress(b.doubleValue() / 100.0);
            status.setText(String.format("Level: %.0f%%", b.doubleValue()));
        });

        ToggleGroup sizeGroup = new ToggleGroup();
        RadioButton small = new RadioButton("Small");
        RadioButton medium = new RadioButton("Medium");
        RadioButton large = new RadioButton("Large");
        small.setToggleGroup(sizeGroup);
        medium.setToggleGroup(sizeGroup);
        large.setToggleGroup(sizeGroup);
        medium.setSelected(true);
        HBox sizes = new HBox(12, small, medium, large);
        sizeGroup.selectedToggleProperty().addListener((o, a, b) -> {
            if (b != null) status.setText("Size: " + ((RadioButton) b).getText());
        });

        CheckBox notify = new CheckBox("Enable notifications");
        notify.setSelected(true);
        notify.selectedProperty().addListener((o, a, b) ->
                status.setText("Notifications " + (b ? "on" : "off")));

        Button apply = new Button("Apply settings");
        apply.setDefaultButton(true);
        apply.setOnAction(e -> status.setText("Applied "
                + (nameField.getText().isEmpty() ? "settings" : "settings for " + nameField.getText())
                + " @ " + (int) slider.getValue() + "%"));

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(16));
        form.addRow(0, new Label("Name:"), nameField);
        form.addRow(1, new Label("Level:"), slider);
        form.addRow(2, new Label("Progress:"), progress);
        form.addRow(3, new Label("Size:"), sizes);
        form.add(notify, 1, 4);
        form.add(apply, 1, 5);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(slider, Priority.ALWAYS);

        VBox left = new VBox(new Label("  Settings"), form);
        left.setStyle("-fx-background-color: #f5f8fc; -fx-border-color: #d7e1ee; -fx-border-width: 0 1 0 0;");
        left.setPrefWidth(340);

        // --- Right: table + chart -----------------------------------------
        TableView<Row> table = buildTable();
        BarChart<String, Number> chart = buildChart();
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox right = new VBox(8, new Label("  Team"), table, chart);
        right.setPadding(new Insets(8));

        // --- Compose ------------------------------------------------------
        HBox body = new HBox(left, right);
        HBox.setHgrow(right, Priority.ALWAYS);

        HBox statusBar = new HBox(status);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(6, 12, 6, 12));
        statusBar.setStyle("-fx-background-color: #eef2f7; -fx-border-color: #d7e1ee; -fx-border-width: 1 0 0 0;");

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(body);
        root.setBottom(statusBar);
        root.setStyle("-fx-background-color: white;");

        return new Scene(root, 760, 520);
    }

    private static TableView<Row> buildTable() {
        TableView<Row> table = new TableView<>();
        TableColumn<Row, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        name.setPrefWidth(140);
        TableColumn<Row, String> role = new TableColumn<>("Role");
        role.setCellValueFactory(new PropertyValueFactory<>("role"));
        role.setPrefWidth(120);
        TableColumn<Row, Integer> score = new TableColumn<>("Score");
        score.setCellValueFactory(new PropertyValueFactory<>("score"));
        score.setPrefWidth(70);
        table.getColumns().add(name);
        table.getColumns().add(role);
        table.getColumns().add(score);
        table.setItems(FXCollections.observableArrayList(
                new Row("Ada", "Engineer", 92),
                new Row("Linus", "Architect", 88),
                new Row("Grace", "Designer", 95),
                new Row("Alan", "Researcher", 81)));
        table.setPrefHeight(160);
        return table;
    }

    private static BarChart<String, Number> buildChart() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Scores");
        chart.setLegendVisible(false);
        chart.setPrefHeight(180);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Ada", 92));
        series.getData().add(new XYChart.Data<>("Linus", 88));
        series.getData().add(new XYChart.Data<>("Grace", 95));
        series.getData().add(new XYChart.Data<>("Alan", 81));
        chart.getData().add(series);
        return chart;
    }

    /** Simple table-row bean; public getters are read reflectively by the cell factory. */
    public static class Row {
        private final SimpleStringProperty name;
        private final SimpleStringProperty role;
        private final SimpleIntegerProperty score;

        public Row(String name, String role, int score) {
            this.name = new SimpleStringProperty(name);
            this.role = new SimpleStringProperty(role);
            this.score = new SimpleIntegerProperty(score);
        }

        public String getName() { return name.get(); }
        public String getRole() { return role.get(); }
        public int getScore() { return score.get(); }
    }
}
