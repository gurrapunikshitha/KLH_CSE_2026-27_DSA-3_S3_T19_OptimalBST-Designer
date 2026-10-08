package edu.dsa.obst.model;

/**
 * One node of a binary search tree, in the shape the frontend draws.
 *
 * @param key       the search key stored at this node
 * @param frequency how often this key is searched for
 * @param depth     1 for the root, 2 for its children, and so on (depth = number of comparisons to find it)
 * @param interval  [i, j], the 1-based range of keys (in sorted order) that this subtree contains
 * @param left      left subtree (smaller keys), or null
 * @param right     right subtree (larger keys), or null
 */
public record TreeNode(
        int key,
        int frequency,
        int depth,
        int[] interval,
        TreeNode left,
        TreeNode right) {
}
