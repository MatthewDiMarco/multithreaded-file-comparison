package sec.multithreadedfilecomparison.helper;

import java.util.Arrays;

public class Helpers {

    /**
     * Given two file contents, compute the longest common sub-sequence (LCS)
     * on a char-by-char basis.
     * (i.e. The longest sequence of characters occurring in both files, in
     * the same order, gaps allowed).
     * @param file1Str File 1 Contents
     * @param file2Str File 2 Contents
     * @return Similarity Score Percentage
     */
    public static double calcSimilarity(String file1Str, String file2Str) {
        if (file1Str.isEmpty() && file2Str.isEmpty()) return 1.0;
        if (file1Str.isEmpty() || file2Str.isEmpty()) return 0.0;

        // Init sub-solution
        char[] file1 = file1Str.toCharArray();
        char[] file2 = file2Str.toCharArray();
        boolean[][] directionLeft = new boolean[file1.length+1][file2.length+1];
        int[][] subSolutions = new int[file1.length+1][file2.length+1];

        // Fill first row and first column with zeros
        Arrays.fill(subSolutions[0], 0);
        for (int ii=1; ii<subSolutions.length; ii++) {
             subSolutions[ii][0] = 0;
        }

        // Build sub-solution
        for (int ii=1; ii<=file1.length; ii++) {
            for (int jj=1; jj<=file2.length; jj++) {

                if (file1[ii-1] == file2[jj-1]) {
                    subSolutions[ii][jj] = subSolutions[ii-1][jj-1] + 1;

                } else if (subSolutions[ii-1][jj] > subSolutions[ii][jj-1]) {
                    subSolutions[ii][jj] = subSolutions[ii-1][jj];
                    directionLeft[ii][jj] = true;

                } else {
                    subSolutions[ii][jj] = subSolutions[ii][jj-1];
                    directionLeft[ii][jj] = false;
                }
            }
        }

        // Init comparison
        int matches = 0;
        int ii = file1.length;
        int jj = file2.length;

        // Begin comparison
        while (ii>0 && jj>0) {

            if (file1[ii-1] == file2[jj-1]) {
                matches++;
                ii--;
                jj--;

            } else if (directionLeft[ii][jj]) {
                ii--;

            } else {
                jj--;
            }
        }

        // Export final result
        double result = (double)(matches * 2) / (file1.length + file2.length);
        return result;
    }
}
