// Screen positions for drawing a BST. This is layout geometry only, not part of the algorithm.
//
// Layout rule:
//   x = the node's position in an in-order traversal (0, 1, 2, ...)
//   y = the node's depth
// In a BST, in-order traversal visits keys in sorted order, so every node gets its own column.
// Two nodes can never share an x position, so nodes can never overlap. Every left descendant
// is also left of its ancestor and every right descendant is to the right, so edges never cross.

export const SLOT_WIDTH = 72; // horizontal space per key
export const LEVEL_HEIGHT = 88; // vertical space per depth level
export const MARGIN_LEFT = 84; // room for the "Depth d" labels
export const MARGIN_RIGHT = 24;
export const MARGIN_TOP = 44;
export const MARGIN_BOTTOM = 48;

export const MIN_RADIUS = 17;
export const MAX_RADIUS = 30; // 2 x 30 < SLOT_WIDTH, so neighbouring circles never touch

/**
 * @param tree nested node object from the API: { key, frequency, depth, interval, left, right }
 * @returns { nodes, edges, width, height, maxDepth, maxFrequency }
 */
export function layoutTree(tree) {
  const nodes = [];
  const edges = [];
  let column = 0;
  let maxDepth = 0;

  // In-order traversal: left subtree, node, right subtree.
  function visit(node, parent) {
    if (!node) return;
    visit(node.left, node);
    const placed = {
      key: node.key,
      frequency: node.frequency,
      depth: node.depth,
      interval: node.interval,
      x: MARGIN_LEFT + column * SLOT_WIDTH + SLOT_WIDTH / 2,
      y: MARGIN_TOP + (node.depth - 1) * LEVEL_HEIGHT,
      parentKey: parent ? parent.key : null,
    };
    column += 1;
    maxDepth = Math.max(maxDepth, node.depth);
    nodes.push(placed);
    visit(node.right, node);
  }
  visit(tree, null);

  const byKey = new Map(nodes.map((n) => [n.key, n]));
  for (const node of nodes) {
    if (node.parentKey !== null) edges.push({ from: byKey.get(node.parentKey), to: node });
  }

  const maxFrequency = Math.max(...nodes.map((n) => n.frequency));
  return {
    nodes,
    edges,
    byKey,
    maxDepth,
    maxFrequency,
    width: MARGIN_LEFT + column * SLOT_WIDTH + MARGIN_RIGHT,
    height: MARGIN_TOP + (maxDepth - 1) * LEVEL_HEIGHT + MARGIN_BOTTOM,
  };
}

/** Radius grows with the square root of frequency, so circle AREA is proportional to frequency. */
export function radiusFor(frequency, maxFrequency) {
  return MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * Math.sqrt(frequency / maxFrequency);
}

/** 0 (lightest) to 1 (darkest) colour intensity for a frequency. */
export function intensityFor(frequency, maxFrequency) {
  return 0.15 + 0.85 * (frequency / maxFrequency);
}
