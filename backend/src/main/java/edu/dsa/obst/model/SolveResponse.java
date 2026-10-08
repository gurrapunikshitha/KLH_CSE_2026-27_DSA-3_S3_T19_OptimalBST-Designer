package edu.dsa.obst.model;

import java.util.List;
import java.util.Map;

/**
 * Response of POST /api/obst/solve: everything the UI needs, in one object.
 *
 * <b>Indexing of the 2D tables:</b> they are n x n, and table[row][col] holds the DP value
 * for the interval [i..j] with i = row + 1 and j = col + 1 (the DP itself is 1-based).
 * Cells below the diagonal (j &lt; i) are not valid intervals and are null.
 *
 * @param keys           the keys in sorted order; every index in this response refers to this order
 * @param frequencies    frequencies matching keys
 * @param notices        informational messages, e.g. that the input was sorted
 * @param n              number of keys
 * @param weightTable    w[i][j], the sum of frequencies of keys i..j
 * @param costTable      cost[i][j], the minimum weighted cost of a BST on keys i..j
 * @param rootTable      root[i][j], the 1-based index of the chosen root (the key is keys[root - 1])
 * @param minCost        cost[1][n] = sum of depth x frequency for the optimal tree
 * @param totalFrequency w[1][n]
 * @param expectedCost   minCost / totalFrequency = average comparisons per search
 * @param tree           the optimal tree
 * @param candidates     for each interval "i-j", every candidate root and its cost breakdown
 * @param fillOrder      the cells in the order the DP filled them
 * @param balanced       the plain balanced BST, for comparison
 * @param courseTable    the w / c / r table in the course-notes format (indices 0..n)
 */
public record SolveResponse(
        List<Integer> keys,
        List<Integer> frequencies,
        List<String> notices,
        int n,
        Long[][] weightTable,
        Long[][] costTable,
        Integer[][] rootTable,
        long minCost,
        long totalFrequency,
        double expectedCost,
        TreeNode tree,
        Map<String, List<Candidate>> candidates,
        List<CellRef> fillOrder,
        BalancedResult balanced,
        CourseTable courseTable) {
}
