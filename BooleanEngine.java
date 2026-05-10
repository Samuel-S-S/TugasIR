import java.util.*;
public class BooleanEngine {
        DataIndexer indexer;
        public BooleanEngine(DataIndexer indexer) {
            this.indexer = indexer;
        }
        private Set<Integer> union(Set<Integer> setA, Set<Integer> setB) {
            Set<Integer> result = new HashSet<>(setA);
            result.addAll(setB);
            return result;
        }
        private Set<Integer> intersect(Set<Integer> setA, Set<Integer> setB) {
            Set<Integer> result = new HashSet<>(setA);
            result.retainAll(setB);
            return result;
        }
        private Set<Integer> not(Set<Integer> setA) {
            Set<Integer> result = new HashSet<>(indexer.allDocs);
            result.removeAll(setA);
            return result;
        }
        public Set<Integer> parseQuery(String query, TolerantRetrieval tolerant) {
            query = query.replaceAll("\\(", " ( ").replaceAll("\\)", " ) ").trim();
            String[] tokens = query.split("\\s+");

            Stack<Set<Integer>> stack = new Stack<>();
            String currentOp = "OR"; 
            boolean applyNot = false;

            for (String token : tokens) {
                String upperToken = token.toUpperCase();
                if (upperToken.equals("AND") || upperToken.equals("OR")) {
                    currentOp = upperToken;
                } else if (upperToken.equals("NOT")) {
                    applyNot = true;
                } else if (token.equals("(") || token.equals(")")) {
                    continue; 
                } else {
                    Set<Integer> termDocs;

                    if (token.contains("*")) {
                        List<String> matched = tolerant.getWildcardMatchedTerms(token);
                        System.out.println("[Wildcard Result] '" + token + "' matches: " + matched);
                        termDocs = tolerant.wildcardSearch(token);
                    } 
                    else {
                        String corrected = tolerant.spellingCorrection(token.toLowerCase());
                        if (!token.equalsIgnoreCase(corrected)) {
                            System.out.println("[Typo Detected] '" + token + "' corrected to -> '" + corrected + "'");
                        }
                        termDocs = indexer.invertedIndex.getOrDefault(corrected, new HashSet<>());
                    }

                    if (applyNot) {
                        termDocs = not(termDocs);
                        applyNot = false;
                    }

                    if (stack.isEmpty()) {
                        stack.push(termDocs);
                    } else {
                        Set<Integer> prevDocs = stack.pop();
                        if (currentOp.equals("AND")) stack.push(intersect(prevDocs, termDocs));
                        else stack.push(union(prevDocs, termDocs));
                    }
                }
            }
            return stack.isEmpty() ? new HashSet<>() : stack.pop();
        }
    }