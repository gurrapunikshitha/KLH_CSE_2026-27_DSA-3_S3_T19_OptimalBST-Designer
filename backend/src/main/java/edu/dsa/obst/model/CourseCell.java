package edu.dsa.obst.model;

import java.util.List;

/**
 * One cell (i, j) of the course-style table, 0 &le; i &le; j &le; n.
 *
 * @param i     left index
 * @param j     right index
 * @param w     w(i,j) = p[i+1] + ... + p[j]
 * @param c     c(i,j), the minimum cost for keys i+1..j
 * @param r     r(i,j), the k giving the minimum (0 when i = j)
 * @param terms every C[i,k-1] + C[k,j] for i &lt; k &le; j, in increasing k (empty when i = j)
 */
public record CourseCell(
        int i,
        int j,
        long w,
        long c,
        int r,
        List<CourseTerm> terms) {
}
