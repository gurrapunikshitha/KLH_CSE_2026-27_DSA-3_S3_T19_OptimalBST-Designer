package edu.dsa.obst.service;

import edu.dsa.obst.model.CourseCell;
import edu.dsa.obst.model.CourseTable;
import edu.dsa.obst.model.CourseTerm;
import edu.dsa.obst.model.TreeNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CourseTableBuilderTest {

    private final CourseTableBuilder builder = new CourseTableBuilder();
    private final ObstSolver solver = new ObstSolver();
    private final TreeBuilder treeBuilder = new TreeBuilder();

    /** The cell (i, j) sits at diagonal j - i, position i. */
    private static CourseCell cell(CourseTable t, int i, int j) {
        CourseCell c = t.diagonals().get(j - i).get(i);
        assertEquals(i, c.i());
        assertEquals(j, c.j());
        return c;
    }

    @Test
    @DisplayName("Textbook case: keys [10,12,20], p [34,8,50] -> c(0,3) = 142, r(0,3) = 3")
    void textbookCase() {
        CourseTable t = builder.build(new int[]{10, 12, 20}, new int[]{34, 8, 50});

        assertEquals(3, t.n());
        assertEquals(142, cell(t, 0, 3).c());
        assertEquals(3, cell(t, 0, 3).r());
        assertEquals(92, cell(t, 0, 3).w());

        // Diagonal 1: single keys.
        assertEquals(34, cell(t, 0, 1).c());
        assertEquals(8, cell(t, 1, 2).c());
        assertEquals(50, cell(t, 2, 3).c());
        assertEquals(1, cell(t, 0, 1).r());

        // Diagonal 2.
        assertEquals(42, cell(t, 0, 2).w());
        assertEquals(50, cell(t, 0, 2).c());
        assertEquals(1, cell(t, 0, 2).r());
        assertEquals(66, cell(t, 1, 3).c());
        assertEquals(3, cell(t, 1, 3).r());

        // Working for (0,3): k = 1, 2, 3 give 0+66, 34+50, 50+0.
        List<CourseTerm> terms = cell(t, 0, 3).terms();
        assertEquals(3, terms.size());
        assertEquals(new CourseTerm(1, 0, 66, 66), terms.get(0));
        assertEquals(new CourseTerm(2, 34, 50, 84), terms.get(1));
        assertEquals(new CourseTerm(3, 50, 0, 50), terms.get(2));
    }

    @Test
    @DisplayName("Staircase shape: diagonal d has n + 1 - d cells, base cells are all 0")
    void staircaseShape() {
        int n = 5;
        CourseTable t = builder.build(new int[]{10, 20, 30, 40, 50}, new int[]{4, 2, 6, 3, 1});

        assertEquals(n + 1, t.diagonals().size());
        for (int d = 0; d <= n; d++) {
            assertEquals(n + 1 - d, t.diagonals().get(d).size());
        }
        for (CourseCell c : t.diagonals().get(0)) {
            assertEquals(0, c.w());
            assertEquals(0, c.c());
            assertEquals(0, c.r());
            assertTrue(c.terms().isEmpty());
        }
    }

    @Test
    @DisplayName("Ties pick the smallest k")
    void tieBreaksToSmallestK() {
        CourseTable t = builder.build(new int[]{1, 2, 3, 4}, new int[]{5, 5, 5, 5});
        assertEquals(40, cell(t, 0, 4).c());
        assertEquals(2, cell(t, 0, 4).r());
    }

    @Test
    @DisplayName("c(0,n) and r(0,n) match the existing solver and tree, for many inputs")
    void matchesExistingSolver() {
        List<int[][]> inputs = new java.util.ArrayList<>(List.of(
                new int[][]{{42}, {7}},
                new int[][]{{10, 12, 20}, {34, 8, 50}},
                new int[][]{{10, 20, 30, 40, 50}, {4, 2, 6, 3, 1}},
                new int[][]{{1, 2, 3, 4, 5, 6, 7}, {1, 1, 1, 1, 1, 1, 1}},
                new int[][]{{2, 4, 6, 8, 10}, {5, 1, 3, 7, 2}},
                new int[][]{{1, 2, 3, 4, 5, 6}, {3, 1, 4, 1, 5, 9}}));

        Random random = new Random(12345);
        for (int trial = 0; trial < 30; trial++) {
            int n = 1 + random.nextInt(12);
            int[] keys = new int[n];
            int[] freqs = new int[n];
            for (int k = 0; k < n; k++) {
                keys[k] = (k + 1) * 10;
                freqs[k] = 1 + random.nextInt(20);
            }
            inputs.add(new int[][]{keys, freqs});
        }

        for (int[][] input : inputs) {
            int[] keys = input[0];
            int[] freqs = input[1];
            int n = keys.length;

            CourseTable t = builder.build(keys, freqs);
            ObstResult r = solver.solve(keys, freqs);
            TreeNode tree = treeBuilder.buildOptimal(r);

            CourseCell top = cell(t, 0, n);
            assertEquals(r.minCost(), top.c());
            assertEquals(r.totalFrequency(), top.w());
            assertEquals(tree.key(), keys[top.r() - 1]);

            // Every cell agrees with the existing solver shifted by one index.
            for (int d = 1; d <= n; d++) {
                for (CourseCell c : t.diagonals().get(d)) {
                    assertEquals(r.cost()[c.i() + 1][c.j()], c.c());
                    assertEquals(r.root()[c.i() + 1][c.j()], c.r());
                    assertEquals(r.weight()[c.i() + 1][c.j()], c.w());
                }
            }
        }
    }
}
