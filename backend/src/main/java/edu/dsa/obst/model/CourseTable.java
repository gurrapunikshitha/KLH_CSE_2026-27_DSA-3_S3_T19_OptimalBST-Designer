package edu.dsa.obst.model;

import java.util.List;

/**
 * The w / c / r table in the format of the course notes (indices 0..n, q = 0).
 *
 * @param n         number of keys
 * @param keys      the keys in sorted order; keys.get(k - 1) is key k
 * @param p         the frequencies; p.get(k - 1) is p[k]
 * @param diagonals diagonals.get(d) holds the cells with j - i = d, for i = 0..n-d
 */
public record CourseTable(
        int n,
        List<Integer> keys,
        List<Integer> p,
        List<List<CourseCell>> diagonals) {
}
