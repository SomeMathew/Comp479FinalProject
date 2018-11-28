package edu.comp479.ranking;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for all kind of I/O interactions.
 *
 * @author Mohsen Parisay <mohsenparisay@gmail.com>
 * @version <1.0> - <17.sep.2018>
 */
public class FileOperations {

    private final String CONFIG_PATH = "config.txt";
    private final String RAW_PATH = "RAW_INDEX_PATH=";
    private final String DATASET_PATH = "DATASET_PATH=";

    public String getDatasetPath() {
        String path = null;
        try {
            List<String> fileContent = readFile(CONFIG_PATH);
            for (String item : fileContent) {
                if (item.startsWith(DATASET_PATH)) {
                    String[] temp = item.split(DATASET_PATH);
                    path = temp[1];
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        return path;
    }

    public String getRawIndexPath() {
        String path = null;
        try {
            List<String> fileContent = readFile(CONFIG_PATH);
            for (String item : fileContent) {
                if (item.startsWith(RAW_PATH)) {
                    String[] temp = item.split(RAW_PATH);
                    path = temp[1];
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }

        return path;
    }

    public Map<String, List<String>> readRawIndexFile(String filePath) {
        return readBlock(filePath);
    }

    public Map<String, List<String>> readBlock(String blockPath) {
        Map<String, List<String>> contents = new TreeMap();

        try {
            List<String> fileContent = readFile(blockPath);

            for (String line : fileContent) {
                if (!line.isEmpty()) {
                    String token = extractToken(line);

                    try {
                        List<String> postingsList = extractPostingsList(line);
                        contents.put(token, postingsList);

                    } catch (Exception exp) {
                        System.err.println(exp.getMessage());
                    }

                }
            }

        } catch (IOException ex) {
            Logger.getLogger(FileOperations.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contents;
    }

    private String extractToken(String line) {
        String[] temp = line.split(":");
        return temp[0].trim();
    }

    private List<String> extractPostingsList(String line) {
        List<String> postingsList = new ArrayList();
        String[] temp = line.split(":");

        if (temp != null && temp[1] != null) {
            String rawLine = temp[1].trim();
            String postings = rawLine.replace("[", "");
            postings = postings.replace("]", "");

            String[] ids = postings.split(",");
            for (String id : ids) {
                if (!id.isEmpty() && id.trim().matches("[0-9]+")) {
                    postingsList.add(id.trim());
                }
            }
        }

        return postingsList;
    }

    public List<String> readFile(String fileName) throws FileNotFoundException, IOException {
        List<String> contents = new ArrayList<>();

        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line;

        try {
            while ((line = br.readLine()) != null) {
                contents.add(line.trim());
            }
        } finally {
            br.close();
        }

        return contents;
    }

    public Map<Integer, List<String>> readDataMap(String filePath) throws IOException {
        Map<Integer, List<String>> dataSet = new TreeMap();
        List<String> contents = readFile(filePath);

        for (String line : contents) {
            int id = extractIdFromDataMap(line);
            List<String> tokens = extractTokensFromDataMap(line);

            if (id != -1) {
                dataSet.put(id, tokens);
            }
        }
        return dataSet;
    }

    public int extractIdFromDataMap(String line) {
        String[] tmp = line.split("=");
        String abc = tmp[0].trim();
        if (abc.isEmpty()) {
            return -1;
        }
        int result = Integer.valueOf(abc);
        return result;
    }

    public List<String> extractTokensFromDataMap(String line) {
        List<String> result = new ArrayList();
        if (line.isEmpty()) {
            return result;
        }

        String[] splitted = line.split("#");
        String tmp = splitted[1];
        tmp = tmp.replace("]", "");
        String[] ids = tmp.split(",");

        for (String id : ids) {
            result.add(id.trim());
        }

        return result;
    }

    public Map<String, Integer> readAfinnFile(String path) throws IOException {
        List<String> contents = this.readFile(path);
        return this.getAfinnData(contents);
    }

    private Map<String, Integer> getAfinnData(List<String> data) {
        Map<String, Integer> dataMap = new TreeMap();

        for (String line : data) {
            String term = getTerm(line);
            int value = getValue(line);
            if (!dataMap.containsKey(term)) {
                dataMap.put(term, value);
            }
        }
        return dataMap;
    }

    private String getTerm(String line) {
        String[] tmp = line.split("\t");
        return tmp[0].trim();
    }

    private int getValue(String line) {
        String[] tmp = line.split("\t");
        return Integer.valueOf(tmp[1].trim());
    }

    public void writeToFile(String filePath, String data) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(filePath, "UTF-8")) {
            writer.println(data);
            writer.flush();
            writer.close();
        }
    }
}
