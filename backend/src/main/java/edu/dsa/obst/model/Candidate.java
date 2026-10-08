package edu.dsa.obst.model;

/**
 * The cost of choosing one particular key as the root of an interval [i..j].
 * The DP evaluates one of these for every r in [i..j]; the "Why this tree?" panel shows them all.
 *
 * totalCost = leftCost + rightCost + weight
 *
 * @param rootIndex 1-based index r of the candidate root
 * @param rootKey   the key at index r
 * @param leftCost  cost[i][r-1], the optimal cost of the left subtree (0 if empty)
 * @param rightCost cost[r+1][j], the optimal cost of the right subtree (0 if empty)
 * @param weight    w[i][j], the sum of frequencies in the interval
 * @param totalCost the cost of the interval if r is the root
 * @param chosen    true for the candidate the DP picked (the minimum, smallest r on ties)
 */
public record Candidate(
        int rootIndex,
        int rootKey,
        long leftCost,
        long rightCost,
        long weight,
        long totalCost,
        boolean chosen) {
}
