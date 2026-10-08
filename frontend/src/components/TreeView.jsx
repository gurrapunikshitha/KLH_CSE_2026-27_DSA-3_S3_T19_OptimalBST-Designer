import { useMemo } from 'react';
import { layoutTree, radiusFor, intensityFor, LEVEL_HEIGHT, MARGIN_TOP } from '../utils/treeLayout.js';

/**
 * Draws a BST as SVG.
 *
 * Node size and colour both scale with frequency, so it is easy to see that
 * frequently searched keys sit near the root.
 *
 * Optional props for interactive features:
 *   highlightKeys: Set of keys to emphasise (all other nodes are dimmed)
 *   selectedKey:   key of the node whose "Why this tree?" panel is open
 *   onNodeClick:   called with the node when a node is clicked
 */
export default function TreeView({ tree, highlightKeys = null, selectedKey = null, onNodeClick = null }) {
  const layout = useMemo(() => layoutTree(tree), [tree]);
  const { nodes, edges, width, height, maxDepth, maxFrequency } = layout;

  const isDimmed = (key) => highlightKeys !== null && !highlightKeys.has(key);

  return (
    <svg
      className="tree-svg"
      viewBox={`0 0 ${width} ${height}`}
      style={{ maxWidth: width }}
      role="img"
      aria-label="Binary search tree"
    >
      {/* Depth guide lines and labels */}
      {Array.from({ length: maxDepth }, (_, d) => {
        const y = MARGIN_TOP + d * LEVEL_HEIGHT;
        return (
          <g key={d} className="depth-guide">
            <line x1={70} x2={width - 8} y1={y} y2={y} />
            <text x={8} y={y + 5}>Depth {d + 1}</text>
          </g>
        );
      })}

      {/* Edges first, so nodes are drawn on top of them */}
      {edges.map(({ from, to }) => (
        <line
          key={`${from.key}-${to.key}`}
          className={`tree-edge ${isDimmed(to.key) ? 'dimmed' : ''}`}
          x1={from.x}
          y1={from.y}
          x2={to.x}
          y2={to.y}
        />
      ))}

      {nodes.map((node) => {
        const r = radiusFor(node.frequency, maxFrequency);
        const intensity = intensityFor(node.frequency, maxFrequency);
        // Light blue for rare keys up to deep blue for the most frequent ones.
        const fill = `hsl(215, 70%, ${92 - intensity * 58}%)`;
        const textColor = intensity > 0.5 ? '#ffffff' : '#10233f';
        const classes = [
          'tree-node',
          isDimmed(node.key) ? 'dimmed' : '',
          highlightKeys?.has(node.key) ? 'highlighted' : '',
          selectedKey === node.key ? 'selected' : '',
          onNodeClick ? 'clickable' : '',
        ].join(' ');

        return (
          <g
            key={node.key}
            className={classes}
            transform={`translate(${node.x}, ${node.y})`}
            onClick={onNodeClick ? () => onNodeClick(node) : undefined}
          >
            <title>
              {`Key ${node.key}, frequency ${node.frequency}, depth ${node.depth}, ` +
                `chosen from interval [${node.interval[0]}..${node.interval[1]}]`}
            </title>
            <circle r={r} fill={fill} />
            <text className="node-key" dy="0.35em" fill={textColor}>{node.key}</text>
            <text className="node-freq" y={r + 15}>f={node.frequency}</text>
          </g>
        );
      })}
    </svg>
  );
}
