package edu.dsa.obst.model;

/**
 * The plain balanced BST used for comparison with the OBST.
 *
 * @param tree         the balanced tree
 * @param minCost      its weighted cost: sum of depth x frequency
 * @param expectedCost minCost / total frequency
 */
public record BalancedResult(TreeNode tree, long minCost, double expectedCost) {
}
