package edu.dsa.obst.service;

import edu.dsa.obst.model.CourseCell;
import edu.dsa.obst.model.CourseTable;
import edu.dsa.obst.model.CourseTerm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builds the w / c / r table exactly the way the course notes write it.
 *
 * <h2>Indexing</h2>
 * Indices run 0..n. Cell (i, j) covers keys i+1..j, so (i, i) is the empty tree.
 * The app has no unsuccessful searches, so q = 0 everywhere and p = the entered frequencies.
 *
 * <h2>Recurrence</h2>
 * <pre>
 *   w(i,i) = 0,  c(i,i) = 0,  r(i,i) = 0
 *   w(i,j) = w(i,j-1) + p[j]
 *   c(i,j) = min over i &lt; k &le; j of { c(i,k-1) + c(k,j) } + w(i,j)
 *   r(i,j) = the k giving the minimum (the smallest k on ties)
 * </pre>
 * Filled by diagonal: j - i = 0, 1, 2, ..., n.
 *
 * This is the same DP as {@link ObstSolver} shifted by one index:
 * c(i,j) here equals cost[i+1][j] there, and r(i,j) equals root[i+1][j].
 */
public class CourseTableBuilder {

    /**
     * @param keys        strictly increasing keys
     * @param frequencies positive frequencies, same length as keys
     */
    public CourseTable build(int[] keys, int[] frequencies) {
        int n = keys.length;

        // p[k] for k = 1..n; p[0] is unused.
        long[] p = new long[n + 1];
        for (int k = 1; k <= n; k++) {
            p[k] = frequencies[k - 1];
        }

        long[][] w = new long[n + 1][n + 1];
        long[][] c = new long[n + 1][n + 1];
        int[][] r = new int[n + 1][n + 1];

        List<List<CourseCell>> diagonals = new ArrayList<>(n + 1);

        // Diagonal d = 0: the base cases w(i,i) = c(i,i) = r(i,i) = 0 (already 0 in Java).
        List<CourseCell> base = new ArrayList<>(n + 1);
        for (int i = 0; i <= n; i++) {
            base.add(new CourseCell(i, i, 0, 0, 0, List.of()));
        }
        diagonals.add(base);

        for (int d = 1; d <= n; d++) {
            List<CourseCell> row = new ArrayList<>(n + 1 - d);

            for (int i = 0; i + d <= n; i++) {
                int j = i + d;

                w[i][j] = w[i][j - 1] + p[j];

                long best = Long.MAX_VALUE;
                int bestK = -1;
                List<CourseTerm> terms = new ArrayList<>(d);

                for (int k = i + 1; k <= j; k++) {
                    long left = c[i][k - 1];
                    long right = c[k][j];
                    long sum = left + right;
                    terms.add(new CourseTerm(k, left, right, sum));

                    // Strictly less than: on a tie we keep the smaller k.
                    if (sum < best) {
                        best = sum;
                        bestK = k;
                    }
                }

                c[i][j] = best + w[i][j];
                r[i][j] = bestK;
                row.add(new CourseCell(i, j, w[i][j], c[i][j], r[i][j], terms));
            }
            diagonals.add(row);
        }

        return new CourseTable(
                n,
                Arrays.stream(keys).boxed().toList(),
                Arrays.stream(frequencies).boxed().toList(),
                diagonals);
    }
}
