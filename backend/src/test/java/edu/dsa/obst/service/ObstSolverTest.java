package edu.dsa.obst.service;

import edu.dsa.obst.model.Candidate;
import edu.dsa.obst.model.CellRef;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ObstSolverTest {

    private final ObstSolver solver = new ObstSolver();

    @Test
    @DisplayName("Textbook case: keys [10,12,20], freqs [34,8,50] -> cost 142, root 20")
    void textbookCase() {
        ObstResult r = solver.solve(new int[]{10, 12, 20}, new int[]{34, 8, 50});

        assertEquals(142, r.minCost());
        assertEquals(92, r.totalFrequency());
        assertEquals(142.0 / 92.0, r.expectedCost(), 1e-12);

        // Root of the whole range is index 3, which is key 20.
        assertEquals(3, r.root()[1][3]);
        assertEquals(20, r.keys()[r.root()[1][3] - 1]);

        // Cost table, checked cell by cell against a hand calculation.
        assertEquals(34, r.cost()[1][1]);
        assertEquals(8, r.cost()[2][2]);
        assertEquals(50, r.cost()[3][3]);
        assertEquals(50, r.cost()[1][2]);   // root 10: 0 + 8 + 42
        assertEquals(66, r.cost()[2][3]);   // root 20: 8 + 0 + 58
        assertEquals(142, r.cost()[1][3]);

        // Every candidate for the full interval: 10 -> 158, 12 -> 176, 20 -> 142 (chosen).
        List<Candidate> full = r.candidates().get("1-3");
        assertEquals(3, full.size());
        assertCandidate(full.get(0), 10, 0, 66, 92, 158, false);
        assertCandidate(full.get(1), 12, 34, 50, 92, 176, false);
        assertCandidate(full.get(2), 20, 50, 0, 92, 142, true);
    }

    @Test
    @DisplayName("Single key: cost = frequency, expected cost = 1 comparison")
    void singleKey() {
        ObstResult r = solver.solve(new int[]{42}, new int[]{7});

        assertEquals(7, r.minCost());
        assertEquals(1.0, r.expectedCost(), 1e-12);
        assertEquals(1, r.root()[1][1]);
        assertEquals(1, r.fillOrder().size());
        assertEquals(1, r.candidates().get("1-1").size());
        assertTrue(r.candidates().get("1-1").get(0).chosen());
    }

    @Test
    @DisplayName("All-equal frequencies (n = 7) give the perfectly balanced tree")
    void allEqualFrequenciesOdd() {
        int[] keys = {1, 2, 3, 4, 5, 6, 7};
        int[] freqs = {1, 1, 1, 1, 1, 1, 1};
        ObstResult r = solver.solve(keys, freqs);

        // Balanced tree of 7 nodes: 1 node at depth 1, 2 at depth 2, 4 at depth 3.
        assertEquals(1 + 2 * 2 + 4 * 3, r.minCost());
        assertEquals(4, r.root()[1][7]); // the middle key
    }

    @Test
    @DisplayName("Ties are broken by the smallest root index")
    void tieBreaksToSmallestRoot() {
        // With 4 equal frequencies, roots 2 and 3 both give cost 8 x 5 = 40.
        ObstResult r = solver.solve(new int[]{1, 2, 3, 4}, new int[]{5, 5, 5, 5});

        assertEquals(40, r.minCost());
        assertEquals(2, r.root()[1][4]);

        List<Candidate> full = r.candidates().get("1-4");
        assertEquals(40, full.get(1).totalCost());
        assertEquals(40, full.get(2).totalCost());
        assertTrue(full.get(1).chosen());
        assertFalse(full.get(2).chosen());
    }

    @Test
    @DisplayName("Five keys: hand-computed tables match")
    void fiveKeys() {
        int[] keys = {10, 20, 30, 40, 50};
        int[] freqs = {4, 2, 6, 3, 1};
        ObstResult r = solver.solve(keys, freqs);

        assertEquals(29, r.minCost());
        assertEquals(16, r.totalFrequency());
        assertEquals(3, r.root()[1][5]); // key 30

        // A few intermediate cells from the hand calculation.
        assertEquals(8, r.cost()[1][2]);
        assertEquals(20, r.cost()[1][3]);
        assertEquals(26, r.cost()[1][4]);
        assertEquals(19, r.cost()[2][5]);
        assertEquals(15, r.cost()[3][5]);
        assertEquals(1, r.root()[1][2]);
        assertEquals(4, r.root()[4][5]);
    }

    @Test
    @DisplayName("Fill order goes by increasing length and covers every cell once")
    void fillOrderIsByIncreasingLength() {
        int n = 6;
        ObstResult r = solver.solve(new int[]{1, 2, 3, 4, 5, 6}, new int[]{3, 1, 4, 1, 5, 9});
        List<CellRef> order = r.fillOrder();

        assertEquals(n * (n + 1) / 2, order.size());

        Set<String> filled = new HashSet<>();
        int previousLength = 0;
        for (CellRef c : order) {
            assertEquals(c.j() - c.i() + 1, c.length());
            assertTrue(c.length() >= previousLength, "length must never decrease");
            previousLength = c.length();

            // Every sub-interval this cell depends on must already be filled.
            for (int root = c.i(); root <= c.j(); root++) {
                if (root > c.i()) {
                    assertTrue(filled.contains(c.i() + "-" + (root - 1)));
                }
                if (root < c.j()) {
                    assertTrue(filled.contains((root + 1) + "-" + c.j()));
                }
            }
            assertTrue(filled.add(c.i() + "-" + c.j()), "cell filled twice");
        }
    }

    @Test
    @DisplayName("Candidates: one per root, exactly one chosen, and it equals the table value")
    void candidatesAreConsistent() {
        ObstResult r = solver.solve(new int[]{2, 4, 6, 8, 10}, new int[]{5, 1, 3, 7, 2});

        for (CellRef c : r.fillOrder()) {
            List<Candidate> list = r.candidates().get(c.i() + "-" + c.j());
            assertEquals(c.length(), list.size());

            long chosenCount = list.stream().filter(Candidate::chosen).count();
            assertEquals(1, chosenCount);

            for (Candidate cand : list) {
                assertEquals(cand.leftCost() + cand.rightCost() + cand.weight(), cand.totalCost());
                assertTrue(cand.totalCost() >= r.cost()[c.i()][c.j()]);
                if (cand.chosen()) {
                    assertEquals(r.cost()[c.i()][c.j()], cand.totalCost());
                    assertEquals(r.root()[c.i()][c.j()], cand.rootIndex());
                }
            }
        }
    }

    @Test
    @DisplayName("Bad input is rejected")
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> solver.solve(new int[]{}, new int[]{}));
        assertThrows(IllegalArgumentException.class, () -> solver.solve(new int[]{1, 2}, new int[]{1}));
        assertThrows(IllegalArgumentException.class, () -> solver.solve(new int[]{2, 1}, new int[]{1, 1}));
        assertThrows(IllegalArgumentException.class, () -> solver.solve(new int[]{1, 1}, new int[]{1, 1}));
        assertThrows(IllegalArgumentException.class, () -> solver.solve(new int[]{1, 2}, new int[]{1, 0}));
    }

    private static void assertCandidate(Candidate c, int key, long left, long right,
                                        long weight, long total, boolean chosen) {
        assertEquals(key, c.rootKey());
        assertEquals(left, c.leftCost());
        assertEquals(right, c.rightCost());
        assertEquals(weight, c.weight());
        assertEquals(total, c.totalCost());
        assertEquals(chosen, c.chosen());
    }
}
