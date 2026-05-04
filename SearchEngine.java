import java.util.*;
public class SearchEngine {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DataIndexer indexer = new DataIndexer();
        indexer.indexFromFolder("dataset"); 

        BooleanEngine booleanEngine = new BooleanEngine(indexer);
        TolerantRetrieval tolerantEngine = new TolerantRetrieval(indexer);
        
        if (!indexer.allDocs.isEmpty()) {
            tolerantEngine.buildKgram();
        }
        while(true){
            System.out.print("\nMasukkan Query (Boolean/Wildcard/Typo): ");
            String input = sc.nextLine();
            if (input.equalsIgnoreCase("exit")) break;
            if (input.trim().isEmpty()) continue;
            Set<Integer> results = booleanEngine.parseIntegratedQuery(input, tolerantEngine);
            System.out.println("Hasil ditemukan di Doc IDs: " + results);
            
            for (int id : results) {
                System.out.println("  > [" + id + "]: " + indexer.docStore.get(id));
            }
        }
    }
}