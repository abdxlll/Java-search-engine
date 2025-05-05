public class SearchResultImp implements SearchResult{
    private String title;
    private double score;

    public SearchResultImp(String title, double score) {
        this.title = title;
        this.score = score;
    }


    public String getTitle() {
        return title;
    }


    public double getScore() {
        return score;
    }
}
