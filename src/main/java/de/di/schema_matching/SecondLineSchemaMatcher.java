package de.di.schema_matching;

import de.di.schema_matching.structures.CorrespondenceMatrix;
import de.di.schema_matching.structures.SimilarityMatrix;

import java.util.Arrays;

public class SecondLineSchemaMatcher {

    /**
     * Translates the provided similarity matrix into a binary correspondence matrix by selecting possibly optimal
     * attribute correspondences from the similarities.
     * @param similarityMatrix A matrix of pair-wise attribute similarities.
     * @return A CorrespondenceMatrix of pair-wise attribute correspondences.
     */
    public CorrespondenceMatrix match(SimilarityMatrix similarityMatrix) {
        double[][] simMatrix = similarityMatrix.getMatrix();

        int[][] corrMatrix = null;

        int sourceCount = simMatrix.length;
        int targetCount = sourceCount == 0 ? 0 : simMatrix[0].length;

        int[] sourceAssignments = new int[sourceCount];
        Arrays.fill(sourceAssignments, -1);

        boolean[] targetAssigned = new boolean[targetCount];

        for (int k = 0; k < Math.min(sourceCount, targetCount); k++) {
            int bestSource = -1;
            int bestTarget = -1;
            double bestSimilarity = -1.0;

            for (int i = 0; i < sourceCount; i++) {
                if (sourceAssignments[i] != -1)
                    continue;

                for (int j = 0; j < targetCount; j++) {
                    if (targetAssigned[j])
                        continue;

                    if (simMatrix[i][j] > bestSimilarity) {
                        bestSimilarity = simMatrix[i][j];
                        bestSource = i;
                        bestTarget = j;
                    }
                }
            }

            if (bestSource >= 0 && bestTarget >= 0) {
                sourceAssignments[bestSource] = bestTarget;
                targetAssigned[bestTarget] = true;
            }
        }

        corrMatrix = assignmentArray2correlationMatrix(sourceAssignments, simMatrix);

        return new CorrespondenceMatrix(
                corrMatrix,
                similarityMatrix.getSourceRelation(),
                similarityMatrix.getTargetRelation()
        );
    }

    /**
     * Translate an array of source assignments into a correlation matrix. For example, [0,3,2] maps 0->1, 1->3, 2->2
     * and, therefore, translates into [[1,0,0,0][0,0,0,1][0,0,1,0]].
     * @param sourceAssignments The list of source assignments.
     * @param simMatrix The original similarity matrix; just used to determine the number of source and target attributes.
     * @return The correlation matrix extracted form the source assignments.
     */
    private int[][] assignmentArray2correlationMatrix(int[] sourceAssignments, double[][] simMatrix) {
        int[][] corrMatrix = new int[simMatrix.length][];
        for (int i = 0; i < simMatrix.length; i++) {
            corrMatrix[i] = new int[simMatrix[i].length];
            for (int j = 0; j < simMatrix[i].length; j++)
                corrMatrix[i][j] = 0;
        }
        for (int i = 0; i < sourceAssignments.length; i++)
            if (sourceAssignments[i] >= 0)
                corrMatrix[i][sourceAssignments[i]] = 1;
        return corrMatrix;
    }
}
