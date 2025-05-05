public class SearchCrawl {
    public static void main(String[] args) throws Exception {
        ProjectTester crawler = new ProjectTesterImp();
        crawler.initialize();
        crawler.crawl("https://people.scs.carleton.ca/~davidmckenney/tinyfruits/N-0.html");
    }
}
