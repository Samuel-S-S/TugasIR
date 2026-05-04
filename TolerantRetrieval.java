import java.util.*;
public class TolerantRetrieval {
        DataIndexer indexer;
        Map<String, Set<String>> kgramIndex = new HashMap<>();

        public TolerantRetrieval(DataIndexer indexer) {
            this.indexer = indexer;
        }
        public void buildKgram() {
            for (String term : indexer.invertedIndex.keySet()) {
                String padded = "$" + term + "$";
                for (int i = 0; i < padded.length() - 1; i++) {
                    String bigram = padded.substring(i, i + 2);
                    kgramIndex.putIfAbsent(bigram, new HashSet<>());
                    kgramIndex.get(bigram).add(term);
                }
            }
        }

        public int levenshteinDistance(String s1, String s2) {
            int m = s1.length(), n = s2.length();
            int[][] dp = new int[m + 1][n + 1];

            for (int i = 0; i <= m; i++) dp[i][0] = i;
            for (int j = 0; j <= n; j++) dp[0][j] = j;

            for (int i = 1; i <= m; i++) {
                for (int j = 1; j <= n; j++) {
                    if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1];
                    } else {
                        dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
                    }
                }
            }
            return dp[m][n];
        }

        public String spellingCorrection(String term) {
            if (indexer.invertedIndex.containsKey(term)) return term;

            String closestTerm = null;
            int minDistance = Integer.MAX_VALUE;

            for (String vocab : indexer.invertedIndex.keySet()) {
                int dist = levenshteinDistance(term, vocab);
                if (dist < minDistance) {
                    minDistance = dist;
                    closestTerm = vocab;
                }
            }
            return closestTerm;
        }

        public Set<Integer> wildcardSearch(String query) {
            Set<Integer> result = new HashSet<>();
            if (query.endsWith("*")) {
                String prefix = query.substring(0, query.length() - 1);
                String padded = "$" + prefix;
                
                if (padded.length() < 2) return result;
                
                String targetBigram = padded.substring(padded.length() - 2);
                Set<String> candidates = kgramIndex.getOrDefault(targetBigram, new HashSet<>());

                for (String candidate : candidates) {
                    if (candidate.startsWith(prefix)) {
                        result.addAll(indexer.invertedIndex.get(candidate));
                    }
                }
            }
            return result;
        }

        public List<String> getWildcardMatchedTerms(String query) {
            List<String> matchedTerms = new ArrayList<>();
            if (query.endsWith("*")) {
                String prefix = query.substring(0, query.length() - 1).toLowerCase();
                String padded = "$" + prefix;
                if (padded.length() >= 2) {
                    String targetBigram = padded.substring(padded.length() - 2);
                    Set<String> candidates = kgramIndex.getOrDefault(targetBigram, new HashSet<>());
                    for (String candidate : candidates) {
                        if (candidate.startsWith(prefix)) {
                            matchedTerms.add(candidate);
                        }
                    }
                }
            }
            return matchedTerms;
        }
    }