package de.di.duplicate_detection;

import de.di.Relation;
import de.di.duplicate_detection.structures.Duplicate;

import java.util.HashSet;
import java.util.Set;

public class TransitiveClosure {

    public Set<Duplicate> calculate(Set<Duplicate> duplicates) {

        if (duplicates.size() <= 1)
            return duplicates;

        Set<Duplicate> closedDuplicates = new HashSet<>();

        Relation relation = duplicates.iterator().next().getRelation();

        int n = relation.getRecords().length;

        boolean[][] connected = new boolean[n][n];

        for (Duplicate d : duplicates) {
            connected[d.getIndex1()][d.getIndex2()] = true;
            connected[d.getIndex2()][d.getIndex1()] = true;
        }

        for (int k = 0; k < n; k++)
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    if (connected[i][k] && connected[k][j])
                        connected[i][j] = true;

        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                if (connected[i][j])
                    closedDuplicates.add(new Duplicate(i, j, 1.0, relation));;

        return closedDuplicates;
    }
}
