package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.IND;

import java.util.*;
import java.util.stream.Collectors;

public class INDProfiler {

    /**
     * Discovers all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     * @param relations The relations that should be profiled for inclusion dependencies.
     * @return The list of all non-trivial unary (and n-ary) inclusion dependencies in the provided relations.
     */
    public List<IND> profile(List<Relation> relations, boolean discoverNary) {
        List<IND> inclusionDependencies = new ArrayList<>();

        if (discoverNary)
            throw new RuntimeException("Sorry, n-ary IND discovery is not supported by this solution.");

        // Convert every relation's columns into sets
        Map<Relation, List<Set<String>>> relationColumnSets = new HashMap<>();

        for (Relation relation : relations) {
            relationColumnSets.put(relation, this.toColumnSets(relation.getColumns()));
        }

        // Compare every column with every other column
        for (Relation lhsRelation : relations) {
            List<Set<String>> lhsColumnSets = relationColumnSets.get(lhsRelation);

            for (int lhsAttribute = 0; lhsAttribute < lhsColumnSets.size(); lhsAttribute++) {
                Set<String> lhsValues = lhsColumnSets.get(lhsAttribute);

                for (Relation rhsRelation : relations) {
                    List<Set<String>> rhsColumnSets = relationColumnSets.get(rhsRelation);

                    for (int rhsAttribute = 0; rhsAttribute < rhsColumnSets.size(); rhsAttribute++) {

                        // Skip trivial IND: same relation and same attribute
                        if (lhsRelation.equals(rhsRelation) && lhsAttribute == rhsAttribute)
                            continue;

                        Set<String> rhsValues = rhsColumnSets.get(rhsAttribute);

                        // Check lhs ⊆ rhs
                        if (rhsValues.containsAll(lhsValues)) {
                            inclusionDependencies.add(
                                    new IND(lhsRelation, lhsAttribute, rhsRelation, rhsAttribute)
                            );
                        }
                    }
                }
            }
        }

        return inclusionDependencies;
    }

    private List<Set<String>> toColumnSets(String[][] columns) {
        return Arrays.stream(columns)
                .map(column -> new HashSet<>(new ArrayList<>(List.of(column))))
                .collect(Collectors.toList());
    }


}
