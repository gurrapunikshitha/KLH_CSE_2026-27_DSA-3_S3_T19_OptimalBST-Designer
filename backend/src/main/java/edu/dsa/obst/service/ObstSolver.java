package edu.dsa.obst.service;

import edu.dsa.obst.model.Candidate;
import edu.dsa.obst.model.CellRef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds an Optimal Binary Search Tree using interval dynamic programming.
 *
 * <h2>Problem</h2>
 * Given sorted keys k_1 < k_2 < ... < k_n with search frequencies f_1 ... f_n, find the BST
 * that minimises  sum over all keys of (depth(k_i) x f_i), where the root has depth 1.
 * The depth of a key is the number of comparisons needed to find it, so this sum is the
 * total number of comparisons over all searches. Only successful searches are counted
 * (no dummy keys).
 *
 * <h2>Recurrence</h2>
 * <pre>
 *   w[i][j]    = f_i + f_(i+1) + ... + f_j
 *
 *   cost[i][j] = 0                                                    if j = i - 1 (empty interval)
 *   cost[i][j] = min over r in [i..j] of ( cost[i][r-1] + cost[r+1][j] ) + w[i][j]
 *   root[i][j] = the r achieving that minimum (the smallest r if several tie)
 * </pre>
 * Why "+ w[i][j]"? When the subtrees on keys i..r-1 and r+1..j are hung below root r, every
 * key in them moves one level deeper, so each of their searches costs one more comparison.
 * The root itself costs 1 comparison per search. Together that is exactly one extra
 * f for every key in i..j, which is w[i][j]. For a single key, cost[i][i] = 0 + 0 + f_i = f_i.
 *
 * <h2>Why interval DP and why this fill order</h2>
 * cost[i][j] only depends on cost of strictly shorter intervals (i..r-1 and r+1..j both have
 * length &lt; j - i + 1). So if we fill all intervals of length 1, then all of length 2, ...,
 * then length n, every value we need has already been computed. The answer is cost[1][n].
 *
 * <h2>Complexity</h2>
 * O(n^2) intervals, each trying O(n) roots: O(n^3) time and O(n^2) space
 * (plus O(n^3) space for the recorded candidates, which exist only for the UI).
 *
 * This class has no Spring dependencies so it can be unit tested directly.
 */
public class ObstSolver {

    /**
     * Runs the DP.
     *
     * @param keys        strictly increasing keys (validated and sorted beforehand)
     * @param frequencies positive frequencies, same length as keys
     * @return all DP tables, the candidate costs, the fill order and the final costs
     * @throws IllegalArgumentException if the input breaks the preconditions above
     */
    public ObstResult solve(int[] keys, int[] frequencies) {
        checkPreconditions(keys, frequencies);

        int n = keys.length;

        // f[i] is the frequency of the i-th key using 1-based indices, to match the recurrence.
        long[] f = new long[n + 1];
        for (int i = 1; i <= n; i++) {
            f[i] = frequencies[i - 1];
        }

        // Tables are (n + 2) x (n + 1). Row n + 1 exists so that cost[r+1][j] with r = j = n
        // (an empty right subtree) is a valid lookup. Java initialises every cell to 0, which
        // is exactly the base case: cost of an empty interval cost[i][i-1] = 0.
        long[][] weight = new long[n + 2][n + 1];
        long[][] cost = new long[n + 2][n + 1];
        int[][] root = new int[n + 2][n + 1];

        Map<String, List<Candidate>> candidates = new LinkedHashMap<>();
        List<CellRef> fillOrder = new ArrayList<>();

        // ---------------------------------------------------------------------------------
        // INTERVAL DP: the outer loop is the interval LENGTH, from 1 up to n.
        // All intervals of one length are finished before any longer interval is started,
        // so every sub-interval an interval needs is already in the table.
        // ---------------------------------------------------------------------------------
        for (int length = 1; length <= n; length++) {

            // All intervals [i..j] of this length, from left to right.
            // The last valid start is i = n - length + 1 (so that j = n).
            for (int i = 1; i <= n - length + 1; i++) {
                int j = i + length - 1;

                // w[i][j] = w[i][j-1] + f[j]. For length 1, w[i][i-1] is 0 so w[i][i] = f[i].
                // w[i][j-1] has length - 1, so it was filled in the previous pass.
                weight[i][j] = weight[i][j - 1] + f[j];

                // Try every key r in [i..j] as the root of this interval.
                long best = Long.MAX_VALUE;
                int bestRoot = -1;
                long[] leftCosts = new long[length];
                long[] rightCosts = new long[length];
                long[] totals = new long[length];

                for (int r = i; r <= j; r++) {
                    long left = cost[i][r - 1];   // keys i..r-1; 0 when r = i (empty)
                    long right = cost[r + 1][j];  // keys r+1..j; 0 when r = j (empty)
                    long total = left + right + weight[i][j];

                    leftCosts[r - i] = left;
                    rightCosts[r - i] = right;
                    totals[r - i] = total;

                    // Strictly less than: on a tie we keep the earlier (smaller) r.
                    // This makes the result deterministic.
                    if (total < best) {
                        best = total;
                        bestRoot = r;
                    }
                }

                cost[i][j] = best;
                root[i][j] = bestRoot;
                fillOrder.add(new CellRef(i, j, length));

                List<Candidate> list = new ArrayList<>(length);
                for (int r = i; r <= j; r++) {
                    list.add(new Candidate(
                            r,
                            keys[r - 1],
                            leftCosts[r - i],
                            rightCosts[r - i],
                            weight[i][j],
                            totals[r - i],
                            r == bestRoot));
                }
                candidates.put(intervalKey(i, j), list);
            }
        }

        long minCost = cost[1][n];
        long totalFrequency = weight[1][n];
        double expectedCost = (double) minCost / totalFrequency;

        return new ObstResult(
                keys.clone(),
                frequencies.clone(),
                weight,
                cost,
                root,
                candidates,
                fillOrder,
                minCost,
                totalFrequency,
                expectedCost);
    }

    /** The map key used for an interval in the candidates map, e.g. "1-3". */
    public static String intervalKey(int i, int j) {
        return i + "-" + j;
    }

    /**
     * The solver assumes clean input. User-facing validation (with friendly messages and
     * sorting) happens before this in the API layer; this is only a safety net.
     */
    private static void checkPreconditions(int[] keys, int[] frequencies) {
        if (keys == null || frequencies == null) {
            throw new IllegalArgumentException("keys and frequencies must not be null");
        }
        if (keys.length != frequencies.length) {
            throw new IllegalArgumentException("keys and frequencies must have the same length");
        }
        if (keys.length == 0) {
            throw new IllegalArgumentException("at least one key is required");
        }
        for (int i = 0; i < keys.length; i++) {
            if (frequencies[i] <= 0) {
                throw new IllegalArgumentException("frequencies must be positive");
            }
            if (i > 0 && keys[i] <= keys[i - 1]) {
                throw new IllegalArgumentException("keys must be strictly increasing");
            }
        }
    }
}
