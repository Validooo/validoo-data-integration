package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.AttrSimWeight;
import de.di.duplicate_detection.structures.Duplicate;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.helper.Tokenizer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class SortedNeighborhood {

    @Data
    @AllArgsConstructor
    private static class Record {
        private int index;
        private String[] values;
    }

    public Set<Duplicate> detectDuplicates(Relation relation,
                                           int[] sortingKeys,
                                           int windowSize,
                                           RecordComparator recordComparator) {

        Set<Duplicate> duplicates = new HashSet<>();

        Record[] records = new Record[relation.getRecords().length];

        for (int i = 0; i < relation.getRecords().length; i++)
            records[i] = new Record(i, relation.getRecords()[i]);

        for (int sortingKey : sortingKeys) {

            Record[] sorted = Arrays.copyOf(records, records.length);

            Arrays.sort(sorted, Comparator.comparing(
                    r -> sortingKey < r.getValues().length
                            ? r.getValues()[sortingKey]
                            : ""
            ));

            for (int i = 0; i < sorted.length; i++) {

                for (int j = i + 1;
                     j < sorted.length && j < i + windowSize;
                     j++) {

                    double sim = recordComparator.compare(
                            sorted[i].getValues(),
                            sorted[j].getValues());

                    if (recordComparator.isDuplicate(sim)) {
                        duplicates.add(new Duplicate(
                                sorted[i].getIndex(),
                                sorted[j].getIndex(),
                                sim,
                                relation
                        ));
                    }
                }
            }
        }

        return duplicates;
    }

    public static RecordComparator suggestRecordComparatorFor(Relation relation) {

        List<AttrSimWeight> attrSimWeights =
                new ArrayList<>(relation.getAttributes().length);

        Tokenizer tokenizer = new Tokenizer(3, true);

        for (int i = 0; i < relation.getAttributes().length; i++) {

            attrSimWeights.add(new AttrSimWeight(
                    i,
                    new Jaccard(tokenizer, false),
                    1.0
            ));
        }

        double threshold = 0.75;

        return new RecordComparator(attrSimWeights, threshold);
    }
}
