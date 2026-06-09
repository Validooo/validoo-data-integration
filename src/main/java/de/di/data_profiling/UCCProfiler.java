package de.di.data_profiling;

import de.di.Relation;
import de.di.data_profiling.structures.AttributeList;
import de.di.data_profiling.structures.PositionListIndex;
import de.di.data_profiling.structures.UCC;

import java.util.ArrayList;
import java.util.List;

public class UCCProfiler {

    /**
     * Discovers all minimal, non-trivial unique column combinations in the provided relation.
     * @param relation The relation that should be profiled for unique column combinations.
     * @return The list of all minimal, non-trivial unique column combinations in ths provided relation.
     */
    public List<UCC> profile(Relation relation) {
        int numAttributes = relation.getAttributes().length;
        List<UCC> uniques = new ArrayList<>();
        List<PositionListIndex> currentNonUniques = new ArrayList<>();

        // Calculate all unary UCCs and unary non-UCCs
        for (int attribute = 0; attribute < numAttributes; attribute++) {
            AttributeList attributes = new AttributeList(attribute);
            PositionListIndex pli = new PositionListIndex(attributes, relation.getColumns()[attribute]);

            if (pli.isUnique())
                uniques.add(new UCC(relation, attributes));
            else
                currentNonUniques.add(pli);
        }

        // Discover UCCs of size > 1 level-wise
        while (!currentNonUniques.isEmpty()) {
            List<PositionListIndex> nextNonUniques = new ArrayList<>();

            for (int i = 0; i < currentNonUniques.size(); i++) {
                for (int j = i + 1; j < currentNonUniques.size(); j++) {
                    PositionListIndex pli1 = currentNonUniques.get(i);
                    PositionListIndex pli2 = currentNonUniques.get(j);

                    AttributeList attributes1 = pli1.getAttributes();
                    AttributeList attributes2 = pli2.getAttributes();

                    if (!attributes1.samePrefixAs(attributes2))
                        continue;

                    AttributeList candidateAttributes = attributes1.union(attributes2);

                    boolean isMinimal = true;
                    for (UCC unique : uniques) {
                        if (unique.getAttributeList().subsetOf(candidateAttributes)) {
                            isMinimal = false;
                            break;
                        }
                    }

                    if (!isMinimal)
                        continue;

                    PositionListIndex candidatePli = pli1.intersect(pli2);

                    if (candidatePli.isUnique())
                        uniques.add(new UCC(relation, candidateAttributes));
                    else
                        nextNonUniques.add(candidatePli);
                }
            }

            currentNonUniques = nextNonUniques;
        }

        return uniques;
    }
}
