import { useMemo } from 'react';
import { layoutTree, radiusFor } from '../utils/treeLayout.js';

/**
 * Walks the generated tree and returns one row per key: its frequency, its depth
 * (root = 1, counted during the walk) and its cost contribution frequency x depth.
 * Rows are sorted by depth, then by key.
 */
function breakdownRows(tree) {
  const rows = [];
  function walk(node, depth) {
    if (!node) return;
    rows.push({ key: node.key, frequency: node.frequency, depth, cost: node.frequency * depth });
    walk(node.left, depth + 1);
    walk(node.right, depth + 1);
  }
  walk(tree, 1);
  return rows.sort((a, b) => a.depth - b.depth || a.key - b.key);
}

/** The "Minimum cost breakdown" card. */
export default function CostBreakdown({ result, hoveredKey, onHoverKey }) {
  const rows = useMemo(() => breakdownRows(result.tree), [result.tree]);

  const totalFrequency = rows.reduce((sum, row) => sum + row.frequency, 0);
  const totalCost = rows.reduce((sum, row) => sum + row.cost, 0);
  const matches = totalCost === result.minCost;

  return (
    <section className="card">
      <h2>
        <span className="step">5</span> Minimum cost breakdown
      </h2>

      <div className="cost-scroll">
        <table className="cost-table">
          <thead>
            <tr>
              <th>Key</th>
              <th>Frequency</th>
              <th>Depth</th>
              <th>Comparisons</th>
              <th>Cost Contribution</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr
                key={row.key}
                className={hoveredKey === row.key ? 'hovered' : ''}
                tabIndex={0}
                onMouseEnter={() => onHoverKey(row.key)}
                onMouseLeave={() => onHoverKey(null)}
                onFocus={() => onHoverKey(row.key)}
                onBlur={() => onHoverKey(null)}
              >
                <td>{row.key}</td>
                <td>{row.frequency}</td>
                <td>{row.depth}</td>
                <td>{row.depth}</td>
                <td className="cost-working">
                  {row.frequency} × {row.depth} = {row.cost}
                </td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td>Total</td>
              <td>{totalFrequency}</td>
              <td />
              <td />
              <td className="cost-working">{totalCost}</td>
            </tr>
          </tfoot>
        </table>
      </div>

      <div className="cost-summary">
        <p>
          <strong>Total Frequency</strong> = {rows.map((row) => row.frequency).join(' + ')} ={' '}
          {totalFrequency}
        </p>
        <p>
          <strong>Minimum Weighted Search Cost</strong> = {rows.map((row) => row.cost).join(' + ')} ={' '}
          {totalCost}
        </p>
        <p>
          <strong>Expected Search Cost</strong> = {totalCost} / {totalFrequency} ={' '}
          {(totalCost / totalFrequency).toFixed(4)}
        </p>
      </div>

      {!matches && (
        <p className="cost-warning">
          Warning: the table total ({totalCost}) does not match the minimum cost in the Result card (
          {result.minCost}).
        </p>
      )}
    </section>
  );
}

/**
 * Draws a ring around the hovered key's node on top of the existing tree, using the same
 * layout and viewBox as TreeView.
 */
export function HoverOverlay({ tree, hoveredKey }) {
  const layout = useMemo(() => layoutTree(tree), [tree]);
  const node = hoveredKey === null ? undefined : layout.byKey.get(hoveredKey);
  if (!node) return null;

  return (
    <svg className="hover-overlay" viewBox={`0 0 ${layout.width} ${layout.height}`} aria-hidden="true">
      <circle
        className="hover-ring"
        cx={node.x}
        cy={node.y}
        r={radiusFor(node.frequency, layout.maxFrequency) + 9}
      />
    </svg>
  );
}
