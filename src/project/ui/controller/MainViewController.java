package project.ui.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import project.source.document.DocumentProcessor;
import project.source.document.DocumentProcessorService;
import project.source.search.Result;
import project.source.search.Searcher;
import project.source.stemmer.Stemmer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class MainViewController implements Initializable
{

    DocumentProcessorService service = new DocumentProcessorService();
    Searcher searcher ;
    Stemmer stem ;
	ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
    @FXML
    private TableView<Result> resultTable ;
    @FXML
    private TableColumn<Result , String>  columnKeyWord ;
    @FXML
    private TableColumn<Result , Integer> columnFrequency ;
    @FXML
    private Button buttonSearch ;
    @FXML
    private TextField enterTextField ;
    @FXML
    private ProgressBar progressBar ;
    @FXML
    private Button buttonView ;
    @FXML
    private Button buttonChangeDirectory ;
	@FXML
	private Button buttonChart;
	@FXML
	private Button buttonProperties;
    private Scene previousScene ;

    public void setPreviousScene(Scene scene)
    {
        previousScene = scene;
    }

    public void setSource(String source) {
        service.setSource(source);
    }

	@FXML
	void buttonPropertiesHandler(ActionEvent event) {

		Alert dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setTitle("Properties");
		dialog.setHeaderText("Here Name of the document will be added");
		dialog.setContentText("Here Document Properties will be Added");
		dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
		dialog.initStyle(StageStyle.DECORATED);
		dialog.initModality(Modality.NONE);

		dialog.showAndWait();

	}

	@FXML
	void buttonChartHandler(ActionEvent event) {

		if(!data.isEmpty()) {

			Parent mainViewParent = null;

			try {
				FXMLLoader chartViewFXML = new FXMLLoader(getClass().getResource("/ChartView.fxml"));
				mainViewParent = chartViewFXML.load();

				ChartViewController controller = chartViewFXML.<ChartViewController>getController();
				controller.setPreviousScene(((Node) event.getSource()).getScene());
				controller.setPieChart(enterTextField.getText(), data);

				Scene chartViewScene = new Scene(mainViewParent);
				Stage chartViewStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

				chartViewStage.setScene(chartViewScene);
				chartViewStage.show();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    @FXML
    public void buttonChangeDirectoryHandler(ActionEvent event)
    {
        Stage backStage =(Stage) ((Node) event.getSource()).getScene().getWindow() ;
        backStage.setScene(previousScene);

        backStage.show();

    }

    @FXML
    public void buttonSearchHandler(ActionEvent event)
    {
        try {

          if(enterTextField.getText().length()!= 0)  {

              stem.setWord(enterTextField.getText());
              String stemmed = stem.getWord();

			  data.clear();
              searcher.searchResult(stemmed).forEach(s -> data.add(new PieChart.Data(s.fileName.getValue(), s.frequency.getValue())));
              refreshList(searcher.searchResult(stemmed));
          }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshList(ArrayList list)
    {
        ObservableList data = FXCollections.observableArrayList(list);

        System.out.println(data.size());

        resultTable.setItems(data);
        resultTable.requestFocus();

    }

    @FXML
    private void handleResultTable() {
        Result result = resultTable.getSelectionModel().getSelectedItem();
        if(resultTable.getSelectionModel().getSelectedItem() != null) {
            String directory = DocumentProcessor.getInputDoc() + File.separator + result.fileName.getValue();

            new Thread(() -> {
                try {
                    Desktop.getDesktop().open(new File(directory));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  <tt>null</tt> if the location is not known.
     * @param resources The resources used to localize the root object, or <tt>null</tt> if
     */



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        searcher = new Searcher();
        stem = new Stemmer();
        columnKeyWord.setCellValueFactory(cellData -> cellData.getValue().fileName);
		columnFrequency.setCellValueFactory(cellData -> cellData.getValue().frequency.asObject());

		System.out.println(Font.getFontNames());
//		enterTextField.setFont(Font.font(FOnt));
		simulateProgressBar();
    }

    public void simulateProgressBar()
    {
        progressBar.progressProperty().bind(service.progressProperty());
        if (service.getState() == Worker.State.READY) {
            service.start();
        }

    }
}
