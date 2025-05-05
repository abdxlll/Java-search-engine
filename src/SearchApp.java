import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SearchApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    public void start(Stage primaryStage) {
        TextField queryTextField = new TextField();
        Button searchButton = new Button("Search");
        RadioButton boostRadioButton = new RadioButton("Boost");
        ListView<String> resultsListView = new ListView<>();

        SearchView searchView = new SearchView(queryTextField, searchButton, boostRadioButton, resultsListView);
        SearchModel searchModel = new SearchModel();
        SearchController searchController = new SearchController(searchModel, searchView);

        VBox root = new VBox(queryTextField, searchButton, boostRadioButton, resultsListView);
        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Search Application");
        primaryStage.show();
    }
}