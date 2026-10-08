package edu.dsa.obst.service;

import edu.dsa.obst.model.BalancedResult;
import edu.dsa.obst.model.SolveRequest;
import edu.dsa.obst.model.SolveResponse;
import edu.dsa.obst.model.TreeNode;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Connects the steps behind one API call:
 * validate and sort input, run the DP, build both trees, and pack everything into a response.
 */
@Service
public class ObstService {

    private final InputNormalizer normalizer;
    private final ObstSolver solver = new ObstSolver();
    private final TreeBuilder treeBuilder = new TreeBuilder();
    private final CourseTableBuilder courseTableBuilder = new CourseTableBuilder();

    public ObstService(InputNormalizer normalizer) {
        this.normalizer = normalizer;
    }

    public SolveResponse solve(SolveRequest request) {
        InputNormalizer.NormalizedInput input = normalizer.normalize(request);

        ObstResult result = solver.solve(input.keys(), input.frequencies());
        TreeNode tree = treeBuilder.buildOptimal(result);
        BalancedResult balanced = treeBuilder.buildBalanced(input.keys(), input.frequencies());

        int n = result.n();
        return new SolveResponse(
                Arrays.stream(input.keys()).boxed().toList(),
                Arrays.stream(input.frequencies()).boxed().toList(),
                input.notices(),
                n,
                toResponseTable(result.weight(), n),
                toResponseTable(result.cost(), n),
                toResponseTable(result.root(), n),
                result.minCost(),
                result.totalFrequency(),
                result.expectedCost(),
                tree,
                result.candidates(),
                result.fillOrder(),
                balanced,
                courseTableBuilder.build(input.keys(), input.frequencies()));
    }

    /**
     * Converts a padded 1-based DP table into an n x n table for JSON:
     * out[i-1][j-1] = table[i][j] for i <= j, and null below the diagonal.
     */
    private static Long[][] toResponseTable(long[][] table, int n) {
        Long[][] out = new Long[n][n];
        for (int i = 1; i <= n; i++) {
            for (int j = i; j <= n; j++) {
                out[i - 1][j - 1] = table[i][j];
            }
        }
        return out;
    }

    private static Integer[][] toResponseTable(int[][] table, int n) {
        Integer[][] out = new Integer[n][n];
        for (int i = 1; i <= n; i++) {
            for (int j = i; j <= n; j++) {
                out[i - 1][j - 1] = table[i][j];
            }
        }
        return out;
    }
}
