package de.di.similarity_measures;

import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class Jaccard implements SimilarityMeasure {

    // The tokenizer that is used to transform string inputs into token lists.
    private final Tokenizer tokenizer;

    // A flag indicating whether the Jaccard algorithm should use set or bag semantics for the similarity calculation.
    private final boolean bagSemantics;

    /**
     * Calculates the Jaccard similarity of the two input strings. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param string1 The first string argument for the similarity calculation.
     * @param string2 The second string argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String string1, String string2) {
        string1 = (string1 == null) ? "" : string1;
        string2 = (string2 == null) ? "" : string2;

        String[] strings1 = this.tokenizer.tokenize(string1);
        String[] strings2 = this.tokenizer.tokenize(string2);
        return this.calculate(strings1, strings2);
    }

    /**
     * Calculates the Jaccard similarity of the two string lists. Note that the Jaccard similarity may use set or
     * multiset, i.e., bag semantics for the union and intersect operations. The maximum Jaccard similarity with
     * multiset semantics is 1/2 and the maximum Jaccard similarity with set semantics is 1.
     * @param strings1 The first string list argument for the similarity calculation.
     * @param strings2 The second string list argument for the similarity calculation.
     * @return The multiset Jaccard similarity of the two arguments.
     */
    @Override
    public double calculate(String[] strings1, String[] strings2) {
        double jaccardSimilarity = 0;

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //                                      DATA INTEGRATION ASSIGNMENT                                           //
        // Calculate the Jaccard similarity of the two String arrays. Note that the Jaccard similarity needs to be    //
        // calculated differently depending on the token semantics: set semantics remove duplicates while bag         //
        // semantics consider them during the calculation. The solution should be able to calculate the Jaccard       //
        // similarity either of the two semantics by respecting the inner bagSemantics flag.                          //



        //                                                                                                            //
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////


        if (strings1 == null) strings1 = new String[0];
        if (strings2 == null) strings2 = new String[0];

        if (this.bagSemantics) {
            Map<String, Long> counts1 = Arrays.stream(strings1)
                                              .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            Map<String, Long> counts2 = Arrays.stream(strings2)
                                              .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

            Set<String> allTokens = new HashSet<>();
            allTokens.addAll(counts1.keySet());
            allTokens.addAll(counts2.keySet());

            long intersection = 0;
            long union = 0;

            for (String token : allTokens) {
                long count1 = counts1.getOrDefault(token, 0L);
                long count2 = counts2.getOrDefault(token, 0L);

                intersection += Math.min(count1, count2);
                union += count1 + count2;
            }

            jaccardSimilarity = union == 0 ? 0 : (double) intersection / union;
        } else {
            Set<String> set1 = new HashSet<>(Arrays.asList(strings1));
            Set<String> set2 = new HashSet<>(Arrays.asList(strings2));

            Set<String> intersection = new HashSet<>(set1);
            intersection.retainAll(set2);

            Set<String> union = new HashSet<>(set1);
            union.addAll(set2);

            jaccardSimilarity = union.isEmpty() ? 0 : (double) intersection.size() / union.size();
        }




        return jaccardSimilarity;
    }
}
