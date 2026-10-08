package edu.dsa.obst.service;

import edu.dsa.obst.model.BalancedResult;
import edu.dsa.obst.model.TreeNode;

/**
 * Turns index ranges into actual {@link TreeNode} trees.
 *
 * <ul>
 *   <li>{@link #buildOptimal} reads the DP root table: the root of [i..j] is root[i][j], its
 *       left subtree is built from [i..root-1] and its right subtree from [root+1..j].</li>
 *   <li>{@link #buildBalanced} builds the plain balanced BST used for comparison.</li>
 * </ul>
 */
public class TreeBuilder {

    /** Builds the optimal tree for all keys, [1..n], from the DP result. */
    public TreeNode buildOptimal(ObstResult result) {
        return buildOptimal(result, 1, result.n(), 1);
    }

    /**
     * Recursively builds the optimal subtree for keys i..j.
     * Each call makes one node, so the whole tree takes O(n) time.
     */
    private TreeNode buildOptimal(ObstResult result, int i, int j, int depth) {
        if (i > j) {
            return null; // empty interval, no subtree
        }
        int r = result.root()[i][j];
        TreeNode left = buildOptimal(result, i, r - 1, depth + 1);
        TreeNode right = buildOptimal(result, r + 1, j, depth + 1);
        return new TreeNode(
                result.keys()[r - 1],
                result.frequencies()[r - 1],
                depth,
                new int[]{i, j},
                left,
                right);
    }

    /**
     * Builds the BST you get by inserting keys in "sorted-median" order: first the middle
     * key, then the middle of each half, and so on. Building it recursively around the
     * middle index gives the same tree as those insertions. Frequencies are ignored when
     * choosing the shape, and that is the point of the comparison.
     * For an even-sized range the lower middle is used.
     */
    public BalancedResult buildBalanced(int[] keys, int[] frequencies) {
        TreeNode tree = buildBalanced(keys, frequencies, 1, keys.length, 1);
        long cost = weightedCost(tree);
        long total = 0;
        for (int f : frequencies) {
            total += f;
        }
        return new BalancedResult(tree, cost, (double) cost / total);
    }

    private TreeNode buildBalanced(int[] keys, int[] frequencies, int i, int j, int depth) {
        if (i > j) {
            return null;
        }
        int mid = (i + j) / 2;
        return new TreeNode(
                keys[mid - 1],
                frequencies[mid - 1],
                depth,
                new int[]{i, j},
                buildBalanced(keys, frequencies, i, mid - 1, depth + 1),
                buildBalanced(keys, frequencies, mid + 1, j, depth + 1));
    }

    /**
     * Sum of depth x frequency over every node, computed directly from a tree.
     * Used for the balanced tree and in tests to check the DP's cost against the real tree.
     */
    public static long weightedCost(TreeNode node) {
        if (node == null) {
            return 0;
        }
        return (long) node.depth() * node.frequency()
                + weightedCost(node.left())
                + weightedCost(node.right());
    }
}
