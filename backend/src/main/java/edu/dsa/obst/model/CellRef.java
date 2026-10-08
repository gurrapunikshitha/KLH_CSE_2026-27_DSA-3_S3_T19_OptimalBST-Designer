package edu.dsa.obst.model;

/**
 * One DP cell, used to record the order in which cells were filled.
 *
 * @param i      start of the interval (1-based)
 * @param j      end of the interval (1-based)
 * @param length interval length, j - i + 1
 */
public record CellRef(int i, int j, int length) {
}
