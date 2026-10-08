package edu.dsa.obst.service;

import edu.dsa.obst.model.Candidate;
import edu.dsa.obst.model.CellRef;

import java.util.List;
import java.util.Map;

/**
 * Everything the DP computes, before it is turned into an API response.
 *
 * The tables use 1-based indices exactly like the recurrence:
 * weight[i][j], cost[i][j] and root[i][j] are valid for 1 <= i <= j <= n.
 * They are sized (n + 2) x (n + 1) so that the empty interval cost[i][i-1] (= 0) can be
 * looked up for every i from 1 to n + 1 without special cases.
 *
 * @param keys           sorted keys, 0-based array (key k_i is keys[i - 1])
 * @param frequencies    frequencies matching keys, 0-based array (f_i is frequencies[i - 1])
 * @param weight         w[i][j] = f_i + ... + f_j
 * @param cost           cost[i][j] = minimum weighted cost of a BST on keys i..j
 * @param root           root[i][j] = the index r that achieves cost[i][j] (smallest r on ties)
 * @param candidates     for every interval "i-j", the cost of every candidate root r in [i..j]
 * @param fillOrder      the cells in the order the DP computed them
 * @param minCost        cost[1][n]
 * @param totalFrequency w[1][n]
 * @param expectedCost   minCost / totalFrequency
 */
public record ObstResult(
        int[] keys,
        int[] frequencies,
        long[][] weight,
        long[][] cost,
        int[][] root,
        Map<String, List<Candidate>> candidates,
        List<CellRef> fillOrder,
        long minCost,
        long totalFrequency,
        double expectedCost) {

    /** Number of keys. */
    public int n() {
        return keys.length;
    }
}
