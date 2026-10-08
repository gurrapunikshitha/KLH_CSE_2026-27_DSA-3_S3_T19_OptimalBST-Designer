package edu.dsa.obst.service;

import edu.dsa.obst.model.BalancedResult;
import edu.dsa.obst.model.TreeNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TreeBuilderTest {

    private final ObstSolver solver = new ObstSolver();
    private final TreeBuilder builder = new TreeBuilder();

    @Test
    void textbookTreeShape() {
        ObstResult r = solver.solve(new int[]{10, 12, 20}, new int[]{34, 8, 50});
        TreeNode root = builder.buildOptimal(r);

        // Expected:      20
        //               /
        //             10
        //               \
        //                12
        assertEquals(20, root.key());
        assertEquals(1, root.depth());
        assertArrayEquals(new int[]{1, 3}, root.interval());
        assertNull(root.right());

        TreeNode ten = root.left();
        assertEquals(10, ten.key());
        assertEquals(2, ten.depth());
        assertArrayEquals(new int[]{1, 2}, ten.interval());
        assertNull(ten.left());

        TreeNode twelve = ten.right();
        assertEquals(12, twelve.key());
        assertEquals(3, twelve.depth());
        assertArrayEquals(new int[]{2, 2}, twelve.interval());

        // The cost of the real tree matches the DP value.
        assertEquals(r.minCost(), TreeBuilder.weightedCost(root));
    }

    @Test
    void balancedTreeForTextbookCase() {
        BalancedResult b = builder.buildBalanced(new int[]{10, 12, 20}, new int[]{34, 8, 50});

        // Middle key 12 is the root: 8x1 + 34x2 + 50x2 = 176.
        assertEquals(12, b.tree().key());
        assertEquals(176, b.minCost());
        assertEquals(176.0 / 92.0, b.expectedCost(), 1e-12);
    }

    @Test
    void balancedTreeUsesLowerMiddleForEvenSizes() {
        BalancedResult b = builder.buildBalanced(new int[]{1, 2, 3, 4}, new int[]{1, 1, 1, 1});
        assertEquals(2, b.tree().key());
        assertEquals(1 + 2 * 2 + 3, b.minCost());
    }

    @Test
    void optimalIsNeverWorseThanBalanced() {
        int[] keys = {10, 20, 30, 40, 50, 60, 70, 80};
        int[] freqs = {1, 30, 2, 2, 25, 1, 1, 40};
        ObstResult r = solver.solve(keys, freqs);
        BalancedResult b = builder.buildBalanced(keys, freqs);
        assertTrue(r.minCost() <= b.minCost());
    }
}
