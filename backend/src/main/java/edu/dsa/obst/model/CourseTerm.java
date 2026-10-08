package edu.dsa.obst.model;

/**
 * One term inside the min of the course recurrence, for a fixed k:
 * C[i,k-1] + C[k,j].
 *
 * @param k     the candidate root index, i &lt; k &le; j
 * @param left  C[i,k-1]
 * @param right C[k,j]
 * @param sum   left + right (w[i,j] is added after the min)
 */
public record CourseTerm(
        int k,
        long left,
        long right,
        long sum) {
}
