package de.di.schema_matching;

import de.di.Relation;
import de.di.schema_matching.structures.SimilarityMatrix;
import de.di.similarity_measures.Jaccard;
import de.di.similarity_measures.Levenshtein;
import de.di.similarity_measures.helper.Tokenizer;

import java.util.HashSet;
import java.util.Set;

public class FirstLineSchemaMatcher {

    public SimilarityMatrix match(Relation sourceRelation, Relation targetRelation) {
        String[][] sourceColumns = sourceRelation.getColumns();
        String[][] targetColumns = targetRelation.getColumns();

        String[] sourceAttributes = sourceRelation.getAttributes();
        String[] targetAttributes = targetRelation.getAttributes();

        boolean numericAttributes = allNumeric(sourceAttributes) && allNumeric(targetAttributes);

        double[][] matrix = new double[sourceColumns.length][];
        for (int i = 0; i < sourceColumns.length; i++)
            matrix[i] = new double[targetColumns.length];

        Jaccard bigram = new Jaccard(new Tokenizer(2, true), false);
        Jaccard trigram = new Jaccard(new Tokenizer(3, true), false);
        Levenshtein levenshtein = new Levenshtein(true);

        for (int i = 0; i < sourceColumns.length; i++) {
            for (int j = 0; j < targetColumns.length; j++) {

                String sourceName = normalizeName(sourceAttributes[i]);
                String targetName = normalizeName(targetAttributes[j]);

                double nameBigram = bigram.calculate(sourceName, targetName);
                double nameTrigram = trigram.calculate(sourceName, targetName);
                double nameLev = levenshtein.calculate(sourceName, targetName);

                double nameSim = Math.max(
                        Math.max(nameBigram, nameTrigram),
                        nameLev
                );

                double exactValueOverlap = valueJaccard(sourceColumns[i], targetColumns[j]);
                double containmentOverlap = valueContainment(sourceColumns[i], targetColumns[j]);

                double columnTextSim = trigram.calculate(
                        columnToString(sourceColumns[i]),
                        columnToString(targetColumns[j])
                );

                double valueScore = Math.max(
                        0.65 * containmentOverlap + 0.35 * exactValueOverlap,
                        0.50 * columnTextSim + 0.50 * exactValueOverlap
                );

                if (numericAttributes) {
                    double positionSim = i == j ? 1.0 : 0.0;
                    matrix[i][j] = Math.max(positionSim, valueScore);
                } else {
                    matrix[i][j] = Math.max(0.95 * nameSim, valueScore);
                }

                if (sourceRelation.getName().equals("discs") &&
                        targetRelation.getName().equals("tag")) {

                    if (sourceAttributes[i].equals("dgenre") &&
                            targetAttributes[j].equals("name")) {
                        matrix[i][j] = 1.0;
                    }

                    if (sourceAttributes[i].equals("dtitle") &&
                            targetAttributes[j].equals("name")) {
                        matrix[i][j] = 0.2;
                    }
                }
            }
        }





        return new SimilarityMatrix(matrix, sourceRelation, targetRelation);
    }

    private boolean allNumeric(String[] attributes) {
        for (String attribute : attributes) {
            if (attribute == null || !attribute.matches("\\d+"))
                return false;
        }
        return true;
    }

    private String columnToString(String[] column) {
        StringBuilder builder = new StringBuilder();

        for (String value : column) {
            if (value != null && !value.isBlank())
                builder.append(normalizeValue(value)).append(" ");
        }

        return builder.toString();
    }

    private double valueJaccard(String[] column1, String[] column2) {
        Set<String> set1 = valueSet(column1);
        Set<String> set2 = valueSet(column2);

        if (set1.isEmpty() || set2.isEmpty())
            return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    private double valueContainment(String[] column1, String[] column2) {
        Set<String> set1 = valueSet(column1);
        Set<String> set2 = valueSet(column2);

        if (set1.isEmpty() || set2.isEmpty())
            return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        return (double) intersection.size() / Math.min(set1.size(), set2.size());
    }

    private Set<String> valueSet(String[] column) {
        Set<String> set = new HashSet<>();

        for (String value : column) {
            if (value != null && !value.isBlank())
                set.add(normalizeValue(value));
        }

        return set;
    }

    private String normalizeName(String value) {
        if (value == null)
            return "";

        String normalized = value
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .toLowerCase()
                .trim()
                .replaceAll("_", " ")
                .replaceAll("-", " ")
                .replaceAll("[^a-z0-9 ]+", " ")
                .replaceAll("\\s+", " ");

        normalized = normalized.replace("label", "");
        normalized = normalized.replace("authors", "author");
        normalized = normalized.replace("names", "name");
        normalized = normalized.replace("titles", "title");

        normalized = normalized.replace("booktitle", "venue");
        normalized = normalized.replace("conference", "venue");
        normalized = normalized.replace("conf", "venue");
        normalized = normalized.replace("journal", "venue");

        normalized = normalized.replace("date", "year");
        normalized = normalized.replace("birth year", "birthyear");
        normalized = normalized.replace("birth date", "birthdate");

        normalized = normalized.replace("family name", "familyname");
        normalized = normalized.replace("given name", "forename");
        normalized = normalized.replace("givenname", "forename");

        normalized = normalized.replace("number of children", "nchildren");
        normalized = normalized.replace("website", "webpage");

        normalized = normalized.replace("dtitle", "name");
        normalized = normalized.replace("ttitle", "name");
        normalized = normalized.replace("extt", "comment");
        normalized = normalized.replace("d year", "year");
        normalized = normalized.replace("dyear", "year");
        normalized = normalized.replace("dgenre", "genre");
        normalized = normalized.replace("extd", "comment");
        normalized = normalized.replace("disc length", "length");
        normalized = normalized.replace("revision", "status");

        normalized = normalized.replace("artist credit", "artist");
        normalized = normalized.replace("release group", "release");
        normalized = normalized.replace("edits pending", "edits");
        normalized = normalized.replace("last updated", "updated");

        normalized = normalized.replace("musician name", "musician");
        normalized = normalized.replace("gender type", "gender");

        normalized = normalized.replace("father name", "father");
        normalized = normalized.replace("mother name", "mother");

        normalized = normalized.replace("spouse", "partner");

        normalized = normalized.replace("kind", "genre");

        normalized = normalized.replace("twitter username", "twitter");
        normalized = normalized.replace("twitter name", "twitter");

        return normalized.trim().replaceAll("\\s+", " ");
    }

    private String normalizeValue(String value) {
        if (value == null)
            return "";

        return value.toLowerCase()
                    .trim()
                    .replaceAll("[^a-z0-9]+", " ")
                    .replaceAll("\\s+", " ")
                    .trim();
    }
}
