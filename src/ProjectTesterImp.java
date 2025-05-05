import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.io.File;
import java.nio.file.*;

public class ProjectTesterImp extends HelperMethods implements ProjectTester {

    public void initialize() {
        File directory = new File(DATA_FOLDER);
        if (directory.exists() && directory.isDirectory()) {
            deleteDirectory(directory);
        }
        directory.mkdir();

        directory = new File("idf");
        if (directory.exists() && directory.isDirectory()) {
            deleteDirectory(directory);
        }
        directory.mkdir();

    }


    public void crawl(String seedURL) {
        try {
            Set<String> visitedUrls = new HashSet<>();
            Queue<String> urlQueue = new LinkedList<>();

            urlQueue.offer(seedURL);
            while (!urlQueue.isEmpty()) {
                String currentUrl = urlQueue.poll();

                if (visitedUrls.contains(currentUrl)) {
                    continue;
                }

                String title = getTitle(currentUrl);
                List<String> outgoingLinks = getUrls(currentUrl);
                String content = getContent(currentUrl);

                saveDataToFile(DATA_FOLDER, currentUrl, title, content, outgoingLinks);

                visitedUrls.add(currentUrl);
                saveTFtoFile(content, currentUrl);

                urlQueue.addAll(outgoingLinks);
            }

            File[] files = new File(DATA_FOLDER).listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    File content = new File(file, "content.txt");
                    String words = readFromFile(content).trim();
                    File url = new File(file, "url.txt");
                    String link = readFromFile(url).trim();
                    saveTF_IDFtoFile(words, link);
                }
            }


            saveIDFToFile(urlToInteger);
        } catch (IOException e) {
            System.out.println("Failed to crawl URL: " + seedURL);
            e.printStackTrace();
        }
    }

    ;

    public double getIDF(String word) {
        try {
            File directory = new File(DATA_FOLDER);
            File[] files = directory.listFiles();
            int dirCount = 0;
            int wordCount = 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    dirCount++;
                    if (containsWord(file, word)) {
                        wordCount++;
                    }
                }
            }
            if (wordCount > 0) {
                return Math.log((double) dirCount / (1 + wordCount)) / Math.log(2);

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0.0;
    }
    ;

    public double getTF(String url, String word) {
        try {
            int fileNumber = getFileNumber(url);
            if (fileNumber == -1) {
                return 0.0;
            }
            File pageDirectory = new File(DATA_FOLDER, String.valueOf(fileNumber));
            String content = readFromFile(new File(pageDirectory, "content.txt"));
            String[] words = content.trim().split("\\s+");
            int wordCount = 0;
            for (String w : words) {
                if (word.equals(w)) {
                    wordCount++;
                }
            }
            return (double) wordCount / words.length;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    ;

    public double getTFIDF(String url, String word) {
        double tf = getTF(url, word);
        double idf = getIDF(word);

        return Math.log(1 + tf) / Math.log(2) * idf;
    }

    public void saveTFtoFile(String words, String url){
        int fileNumber = getFileNumber(url);
        File pageDirectory = new File(DATA_FOLDER, String.valueOf(fileNumber));
        String[] fruits = words.trim().split("\\s+");
        File tfDirectory = new File(pageDirectory, "tf");
        if (!tfDirectory.exists()) {
            if (!tfDirectory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + tfDirectory.getAbsolutePath());
            }
        }
        for(String fruit: fruits){
            File fruitFile = new File(tfDirectory, fruit + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fruitFile))) {
                writer.write(String.valueOf(getTF(url, fruit.trim())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void saveIDFToFile(Map<String, Integer> urlToInteger) {
        Set<String> uniqueWords = new HashSet<>();

        // Collect unique words from content files
        for (Map.Entry<String, Integer> entry : urlToInteger.entrySet()) {
            String folderPath = DATA_FOLDER + File.separator + entry.getValue();
            File contentFile = new File(folderPath, "content.txt");

            if (contentFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(contentFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] words = line.split("\\s+");
                        uniqueWords.addAll(Arrays.asList(words));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Calculate IDF for each unique word and save to idf directory
        File idfDirectory = new File("idf");
        if (!idfDirectory.exists()) {
            idfDirectory.mkdir();
        }

        for (String word : uniqueWords) {
            double idfValue = getIDF(word);
            String fileName = word + ".txt";
            String content = String.valueOf(idfValue);

            try {
                saveStringToFile(idfDirectory, fileName, content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveTF_IDFtoFile(String words, String url){
        int fileNumber = getFileNumber(url);
        File pageDirectory = new File(DATA_FOLDER, String.valueOf(fileNumber));
        String[] fruits = words.trim().split("\\s+");
        File tfIDFDirectory = new File(pageDirectory, "tf-idf");
        if (!tfIDFDirectory.exists()) {
            if (!tfIDFDirectory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + tfIDFDirectory.getAbsolutePath());
            }
        }
        for(String fruit: fruits){
            File fruitFile = new File(tfIDFDirectory, fruit + ".txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fruitFile))) {
                writer.write(String.valueOf(getTFIDF(url, fruit.trim())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    ;

    public List<String> getOutgoingLinks(String url) {
        List<String> outgoingLinks = new ArrayList<>();

        Integer folderNumber = urlToInteger.get(url.trim());
        if (folderNumber != null) {
            File pageDirectory = new File(DATA_FOLDER, String.valueOf(folderNumber));
            try {
                String content = readFromFile(new File(pageDirectory, "outgoingLinks.txt"));
                outgoingLinks = List.of(content.trim().split("\\s+"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return outgoingLinks.isEmpty() ? null : outgoingLinks;
    }

    public List<String> getIncomingLinks(String url) {
        List<String> incomingLinks = new ArrayList<>();

        Integer folderNumber = urlToInteger.get(url);
        if (folderNumber != null) {
            String folderPath = DATA_FOLDER + File.separator + folderNumber;

            try (BufferedReader reader = new BufferedReader(new FileReader(folderPath + File.separator + "outgoingLinks.txt"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    incomingLinks.add(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return incomingLinks.isEmpty() ? null : incomingLinks;
    }

    public double getPageRank(String URL) {
        double alpha = 0.1;
        int N = 0;
        Map<Integer, String> urlHashMap = new HashMap<>();

        File directory = new File(DATA_FOLDER);
        File[] folders = directory.listFiles();
        if (folders != null) {
            for (File folder : folders) {
                File urlFile = new File(folder.getPath() + File.separator + "url.txt");
                if (urlFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(urlFile))) {
                        String fileUrl = reader.readLine();
                        if (fileUrl != null) {
                            String url = fileUrl.trim();
                            urlHashMap.put(N, url);
                            N++;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        double[][] aMatrix = new double[N][N];

        for (int i = 0; i < aMatrix.length; i++) {
            List<String> linkList = getOutgoingLinks(urlHashMap.get(i));

            if (linkList != null) {
                for (int j = 0; j < linkList.size(); j++) {
                    for (Map.Entry<Integer, String> entry : urlHashMap.entrySet()) {
                        if (entry.getValue().equals(linkList.get(j))) {
                            aMatrix[i][entry.getKey()] += 1;
                        }
                    }
                }
            }
        }



        for (int i = 0; i < aMatrix.length; i++) {
            int tempN = 0;
            for (int j = 0; j < aMatrix[i].length; j++) {
                if (aMatrix[i][j] == 1) {
                    tempN++;
                }
            }

            for (int k = 0; k < aMatrix[i].length; k++) {
                if (aMatrix[i][k] == 1) {
                    aMatrix[i][k] = (aMatrix[i][k] / tempN) * (1 - alpha);
                }
            }
        }

        for (int i = 0; i < aMatrix.length; i++) {
            for (int j = 0; j < aMatrix[i].length; j++) {
                aMatrix[i][j] += (alpha / N);
            }
        }

        double[][] vMatrix = new double[1][N];
        double[][] vNextMatrix = new double[1][N];

        for (int i = 0; i < N; i++) {
            vMatrix[0][i] = 0;
            vNextMatrix[0][i] = (1.0 / N);
        }

        while (euclideanDist(vMatrix, vNextMatrix) >= 0.0001) {
            vMatrix = vNextMatrix;
            vNextMatrix = matrixMult(vMatrix, aMatrix);
        }

        for (Map.Entry<Integer, String> entry : urlHashMap.entrySet()) {
            if (entry.getValue().equals(URL)) {
                return vNextMatrix[0][entry.getKey()];
            }
        }
        return -1;
    }

    public List<SearchResult> search(String query, boolean boost, int X) {
        try {
            List<SearchResult> similarities = new ArrayList<>();
            List<SearchResult> results = new ArrayList<>();

            // Read data from text files and perform the search
            File pages = new File(DATA_FOLDER);
            File[] pageDirectories = pages.listFiles();
            File idfValues = new File("idf");
            Map<String, Double> queryTF = calculateQueryTF(query);
            Map<String, Double> tfIDFQuery = new HashMap<>();

            for (Map.Entry<String, Double> entry : queryTF.entrySet()) {
                String word = entry.getKey();
                double tfQueryWord = entry.getValue();
                String filename = word.trim() + ".txt";
                String path = idfValues.getPath();
                Path filePath = Paths.get(path, filename);

                if (Files.exists(filePath)) {
                    File fileParameter = new File(path, filename);
                    double idfValue = Double.parseDouble(readFromFile(fileParameter));
                    double tfIdfQueryWord = (Math.log(1 + tfQueryWord) / Math.log(2)) * idfValue;
                    tfIDFQuery.put(word, tfIdfQueryWord);
                } else {
                    tfIDFQuery.put(word, 0.0);
                }
            }
            for (File pageDirectory : pageDirectories) {
                Map<String, Double> tfIDFDoc = new HashMap<>();

                for (Map.Entry<String, Double> entry : queryTF.entrySet()) {
                    String word = entry.getKey();
                    if (containsWordInFile(new File(pageDirectory, "content.txt"), word)) {
                        File tfIdfDirectory = new File(pageDirectory, "tf-idf");
                        File wordFile = new File(tfIdfDirectory, word + ".txt");
                        double tfIdfDocWord = Double.parseDouble(readFromFile(wordFile));
                        tfIDFDoc.put(word, tfIdfDocWord);
                    } else {
                        tfIDFDoc.put(word, 0.0);
                    }
                }
                double similarity = cosineSimilarity(tfIDFQuery, tfIDFDoc);

                if (boost) {
                    double pageRank = getPageRank(readFromFile(new File(pageDirectory, "url.txt")).trim());
                    similarity = pageRank * similarity;
                }

                SearchResult result = new SearchResultImp(readFromFile(new File(pageDirectory, "title.txt")).trim(), similarity);
                similarities.add(result);
            }
            similarities.sort((result1, result2) -> {
                double roundedScore1 = Math.round(result1.getScore() * 1000.0) / 1000.0;
                double roundedScore2 = Math.round(result2.getScore() * 1000.0) / 1000.0;

                int scoreComparison = Double.compare(roundedScore2, roundedScore1);

                if (scoreComparison == 0) {
                    return result1.getTitle().compareTo(result2.getTitle());
                }

                return scoreComparison;
            });

            results.addAll(similarities.subList(0, Math.min(X, similarities.size())));
            return results;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
