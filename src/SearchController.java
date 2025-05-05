import java.util.List;
public class SearchController {
    private final SearchModel searchModel;
    private final SearchView searchView;

    public SearchController(SearchModel searchModel, SearchView searchView) {
        this.searchModel = searchModel;
        this.searchView = searchView;
        initialize();
    }

    private void initialize() {
        searchView.setSearchButtonAction(() -> {
            String query = searchView.getQuery();
            boolean boostEnabled = searchView.isBoostEnabled();
            int X = 10; // Set the number of results
            List<SearchResult> searchResults = searchModel.search(query, boostEnabled, X);
            searchView.displayResults(searchResults);
        });

        searchView.setBoostButtonAction(() -> {

        });
    }
}