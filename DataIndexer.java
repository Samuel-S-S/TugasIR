import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.nio.file.*;

public class DataIndexer {
    public Map<Integer, String> docStore = new HashMap<>(); 
    public Map<String, Set<Integer>> invertedIndex = new HashMap<>(); 
    public Set<Integer> allDocs = new HashSet<>();

    public void indexFromFolder(String folderPath) {
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (listOfFiles != null && listOfFiles.length > 0) {
            for (File file : listOfFiles) {
                try {
                    String fileName = file.getName();
                    int docId = Integer.parseInt(fileName.replaceAll("[^0-9]", ""));
                    
                    String content = new String(Files.readAllBytes(file.toPath()));
                    
                    addDocument(docId, content);
                    
                } catch (IOException | NumberFormatException e) {
                    System.err.println("Gagal memproses file: " + file.getName() + " | Error: " + e.getMessage());
                }
            }
            System.out.println("Berhasil mengindeks " + allDocs.size() + " dokumen dari folder '" + folderPath + "'.");
        } else {
            System.err.println("Folder '" + folderPath + "' kosong atau tidak ditemukan!");
        }
    }

    public void addDocument(int docId, String text) {
        docStore.put(docId, text);
        allDocs.add(docId);
        List<String> tokens = preprocess(text);
        
        for (String token : tokens) {
            invertedIndex.putIfAbsent(token, new HashSet<>());
            invertedIndex.get(token).add(docId);
        }
    }

    public List<String> preprocess(String text) {
        List<String> tokens = new ArrayList<>();
        text = text.toLowerCase();
        Matcher m = Pattern.compile("\\b[a-z0-9]+\\b").matcher(text);
        while (m.find()) {
            tokens.add(m.group());
        }
        return tokens;
    }
}