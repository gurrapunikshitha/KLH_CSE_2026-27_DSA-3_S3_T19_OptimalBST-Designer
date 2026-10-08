package edu.dsa.obst.service;

import edu.dsa.obst.model.TreeNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks the DP against an exhaustive search: build EVERY possible BST on the keys
 * (Catalan(n) of them, 429 for n = 7), compute each one's cost directly from node depths,
 * and take the minimum. This uses no DP, so it is an independent check.
 */
class BruteForceVerificationTest {

    private final ObstSolver solver = new ObstSolver();
    private final TreeBuilder builder = new TreeBuilder();

    @Test
    @DisplayName("Fixed cases match brute force")
    void fixedCases() {
        check(new int[]{10, 12, 20}, new int[]{34, 8, 50});
        check(new int[]{10, 20, 30, 40, 50}, new int[]{4, 2, 6, 3, 1});
        check(new int[]{1, 2, 3, 4, 5, 6, 7}, new int[]{1, 1, 1, 1, 1, 1, 1});
        check(new int[]{1, 2, 3, 4, 5, 6, 7}, new int[]{100, 1, 1, 1, 1, 1, 1});
        check(new int[]{1, 2, 3, 4, 5, 6, 7}, new int[]{1, 2, 3, 4, 5, 6, 7});
    }

    @Test
    @DisplayName("200 random inputs with n <= 7 match brute force")
    void randomCases() {
        Random random = new Random(20260928L); // fixed seed, so failures are reproducible
        for (int t = 0; t < 200; t++) {
            int n = 1 + random.nextInt(7);
            int[] keys = new int[n];
            int[] freqs = new int[n];
            int key = random.nextInt(10);
            for (int i = 0; i < n; i++) {
                key += 1 + random.nextInt(10);
                keys[i] = key;
                freqs[i] = 1 + random.nextInt(10); // small range, so ties happen often
            }
            check(keys, freqs);
        }
    }

    private void check(int[] keys, int[] freqs) {
        ObstResult dp = solver.solve(keys, freqs);
        List<TreeNode> all = allTrees(keys, freqs, 1, keys.length, 1);

        long bruteMin = Long.MAX_VALUE;
        for (TreeNode t : all) {
            bruteMin = Math.min(bruteMin, TreeBuilder.weightedCost(t));
        }

        // Among all optimal trees, the smallest root key. The DP must pick this one (tie rule).
        int smallestOptimalRoot = Integer.MAX_VALUE;
        for (TreeNode t : all) {
            if (TreeBuilder.weightedCost(t) == bruteMin) {
                smallestOptimalRoot = Math.min(smallestOptimalRoot, t.key());
            }
        }

        String input = java.util.Arrays.toString(keys) + " / " + java.util.Arrays.toString(freqs);
        assertEquals(bruteMin, dp.minCost(), "min cost for " + input);
        assertEquals(smallestOptimalRoot, dp.keys()[dp.root()[1][keys.length] - 1], "root for " + input);

        // The tree actually built from the root table must have that cost and be a valid BST.
        TreeNode built = builder.buildOptimal(dp);
        assertEquals(bruteMin, TreeBuilder.weightedCost(built), "built tree cost for " + input);
        assertTrue(isValidBst(built, Long.MIN_VALUE, Long.MAX_VALUE), "BST property for " + input);
        assertEquals(keys.length, countNodes(built));
    }

    /** Every BST on keys i..j (1-based), with depths starting at the given depth. */
    private static List<TreeNode> allTrees(int[] keys, int[] freqs, int i, int j, int depth) {
        List<TreeNode> result = new ArrayList<>();
        if (i > j) {
            result.add(null); // exactly one empty tree
            return result;
        }
        for (int r = i; r <= j; r++) {
            for (TreeNode left : allTrees(keys, freqs, i, r - 1, depth + 1)) {
                for (TreeNode right : allTrees(keys, freqs, r + 1, j, depth + 1)) {
                    result.add(new TreeNode(keys[r - 1], freqs[r - 1], depth, new int[]{i, j}, left, right));
                }
            }
        }
        return result;
    }

    private static boolean isValidBst(TreeNode node, long low, long high) {
        if (node == null) {
            return true;
        }
        if (node.key() <= low || node.key() >= high) {
            return false;
        }
        return isValidBst(node.left(), low, node.key()) && isValidBst(node.right(), node.key(), high);
    }

    private static int countNodes(TreeNode node) {
        return node == null ? 0 : 1 + countNodes(node.left()) + countNodes(node.right());
    }
}
