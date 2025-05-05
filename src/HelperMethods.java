import java.io.*;
import java.util.*;

public class HelperMethods {
    public int fileCounter = 0;
    public Map<String, Integer> urlToInteger = new HashMap<>();
    public String DATA_FOLDER = "crawlerdata";

    public void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }


    public String getTitle(String url) {
        try {
            String htmlContent = WebRequester.readURL(url);

            int startIndex = htmlContent.indexOf("<title>");
            int endIndex = htmlContent.indexOf("</title>");

            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return htmlContent.substring(startIndex + 7, endIndex);
            } else {
                return null;
            }

        } catch (IOException e) {
            return null;
        }
    }

    public String getContent(String url) {
        try {
            String htmlContent = WebRequester.readURL(url);

            int startIndex = htmlContent.indexOf("<p>");

            int endIndex = htmlContent.indexOf("</p>");

            if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                return htmlContent.substring(startIndex + 3, endIndex).trim();
            } else {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String computeFullUrl(String seedUrl, String relativeUrl) {
        if (relativeUrl.startsWith("./")) {
            relativeUrl = relativeUrl.substring(2);

            // Find the last forward slash in the seed URL to get the base path
            int lastSlashIndex = seedUrl.lastIndexOf("/");
            String basePath = seedUrl.substring(0, lastSlashIndex + 1);

            return basePath + relativeUrl;
        } else {
            // If the relative URL is not a relative path, use it as is
            return relativeUrl;
        }
    }

    public List<String> getUrls(String seedUrl) {
        try {
            String html = WebRequester.readURL(seedUrl);

            List<String> links = new ArrayList<>();

            List<String> fullLinks = new ArrayList<>();

            int start = 0;

            // Loop to find all occurrences of <a href=" in the HTML content
            while (true) {
                start = html.indexOf("<a href=\"", start);

                if (start == -1) {
                    break;
                }

                int end = html.indexOf("\"", start + 9);

                // Extract the link from the HTML content
                String link = html.substring(start + 9, end);

                links.add(link);

                start = end;
            }

            for (String link : links) {
                fullLinks.add(computeFullUrl(seedUrl, link));
            }

            return fullLinks;
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public int getFileNumber(String url) {
        return urlToInteger.getOrDefault(url, -1);
    }

    public void saveDataToFile(String baseDir, String url, String title, String content, List<String> outgoingLinks) throws IOException {
        int fileNumber = getFileNumber(url);

        if (fileNumber == -1) {
            // The URL is not in the map, add it and use the current counter value
            fileNumber = fileCounter++;
            urlToInteger.put(url, fileNumber);
        }

        File pageDirectory = new File(baseDir, String.valueOf(fileNumber));
        if (!pageDirectory.exists()) {
            if (!pageDirectory.mkdirs()) {
                throw new IOException("Failed to create directory: " + pageDirectory.getAbsolutePath());
            }
        }
        saveStringToFile(pageDirectory, "url.txt", url);
        saveStringToFile(pageDirectory, "title.txt", title);
        saveStringToFile(pageDirectory, "content.txt", content);
        saveListToFile(pageDirectory, "outgoingLinks.txt", outgoingLinks);
    }

    public void saveStringToFile(File directory, String fileName, String content) throws IOException {
        File file = new File(directory, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    //For outgoing links
    public void saveListToFile(File directory, String fileName, List<String> contentList) throws IOException {
        File file = new File(directory, fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String item : contentList) {
                writer.write(item + "\n");
            }
        }
    }
    public String readFromFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
    public boolean containsWord(File file, String word) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(file, "content.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(word)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean containsWordInFile(File file, String word) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().equalsIgnoreCase(word)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public double[][] matrixMult(double[][] matrix1, double[][] matrix2) {
        int rows_A = matrix1.length;
        int cols_A = matrix1[0].length;
        int cols_B = matrix2[0].length;

        double[][] result = new double[rows_A][cols_B];

        for (int i = 0; i < rows_A; i++) {
            for (int j = 0; j < cols_B; j++) {
                for (int k = 0; k < cols_A; k++) {
                    result[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        return result;
    }

    public double euclideanDist(double[][] a, double[][] b) {
        double sum = 0;
        for (int i = 0; i < a[0].length; i++) {
            sum += Math.pow((a[0][i] - b[0][i]), 2);
        }
        return Math.sqrt(sum);
    }

    public double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        double dotProduct = 0;
        double magnitude1 = 0;
        double magnitude2 = 0;

        for (Map.Entry<String, Double> entry : vector1.entrySet()) {
            String word = entry.getKey();
            double value1 = entry.getValue();
            double value2 = vector2.getOrDefault(word, 0.0);

            dotProduct += value1 * value2;
            magnitude1 += Math.pow(value1, 2);
            magnitude2 += Math.pow(value2, 2);
        }

        double magnitudeProduct = Math.sqrt(magnitude1) * Math.sqrt(magnitude2);

        if (magnitudeProduct == 0) {
            return 0; // Avoid division by zero
        }

        return dotProduct / magnitudeProduct;
    }




    public Map<String, Double> calculateQueryTF(String query) {
        String[] words = query.split("\\s+");
        int totalWords = words.length;
        Map<String, Double> queryTF = new HashMap<>();

        for (String word : words) {
            queryTF.put(word, queryTF.getOrDefault(word, 0.0) + 1);
        }

        for (Map.Entry<String, Double> entry : queryTF.entrySet()) {
            entry.setValue(entry.getValue() / totalWords);
        }
        return queryTF;
    }

}
