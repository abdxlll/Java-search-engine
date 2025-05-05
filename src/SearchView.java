import javafx.scene.control.*;

import java.util.List;

public class SearchView {
    private TextField queryTextField;
    private Button searchButton;
    private RadioButton boostRadioButton;
    private ListView<String> resultsListView;

    public SearchView(TextField queryTextField, Button searchButton, RadioButton boostRadioButton, ListView<String> resultsListView) {
        this.queryTextField = queryTextField;
        this.searchButton = searchButton;
        this.boostRadioButton = boostRadioButton;
        this.resultsListView = resultsListView;
    }

    public void setSearchButtonAction(Runnable action) {
        searchButton.setOnAction(event -> action.run());
    }

    public void setBoostButtonAction(Runnable action) {
        boostRadioButton.setOnAction(event -> action.run());
    }

    public String getQuery() {
        return queryTextField.getText();
    }

    public boolean isBoostEnabled() {
        return boostRadioButton.isSelected();
    }

    public void displayResults(List<SearchResult> searchResults) {
        resultsListView.getItems().clear();
        for (SearchResult result : searchResults) {
            resultsListView.getItems().add(result.getTitle() + " - Score: " + result.getScore());
        }
    }

}