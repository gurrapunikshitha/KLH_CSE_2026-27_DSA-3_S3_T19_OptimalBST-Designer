import { useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { layoutTree, radiusFor } from '../utils/treeLayout.js';

const GAP = 10; // space between the node and the tooltip
const EDGE = 8; // minimum space between the tooltip and the window edge

/** The nodes from the root down to the node with this key, found by normal BST search. */
function pathTo(tree, key) {
  const path = [];
  let node = tree;
  while (node) {
    path.push(node);
    if (key === node.key) return path;
    node = key < node.key ? node.left : node.right;
  }
  return [];
}

/**
 * Which node's tooltip is showing. The state remembers which tree it belongs to, so a new
 * tree hides the tooltip.
 * hovered = { key, rect, pinned } where rect is the node's position on screen and
 * pinned is true when it was opened by a tap (touch screens).
 */
export function useNodeTooltip(tree) {
  const [state, setState] = useState(null);
  const hovered = state && state.tree === tree ? state : null;
  const setHovered = (value) => setState(value ? { ...value, tree } : null);
  return { hovered, setHovered };
}

/**
 * Lightly highlights the path from the root to the hovered node. Rendered below the search
 * overlay, so search highlights always stay on top.
 */
export function TooltipPathOverlay({ tree, hovered }) {
  const layout = useMemo(() => layoutTree(tree), [tree]);
  if (!hovered) return null;

  const { byKey, width, height, maxFrequency } = layout;
  const visited = pathTo(tree, hovered.key).map((node) => byKey.get(node.key));

  return (
    <svg className="tooltip-path-overlay" viewBox={`0 0 ${width} ${height}`} aria-hidden="true">
      {visited.slice(1).map((to, index) => {
        const from = visited[index];
        // From circle edge to circle edge, so the line never covers a node.
        const dx = to.x - from.x;
        const dy = to.y - from.y;
        const length = Math.hypot(dx, dy);
        const r1 = radiusFor(from.frequency, maxFrequency) + 4;
        const r2 = radiusFor(to.frequency, maxFrequency) + 4;
        return (
          <line
            key={`${from.key}-${to.key}`}
            className="tooltip-path-edge"
            x1={from.x + (dx * r1) / length}
            y1={from.y + (dy * r1) / length}
            x2={to.x - (dx * r2) / length}
            y2={to.y - (dy * r2) / length}
          />
        );
      })}
      {visited.map((node) => (
        <circle
          key={node.key}
          className="tooltip-path-ring"
          cx={node.x}
          cy={node.y}
          r={radiusFor(node.frequency, maxFrequency) + 4}
        />
      ))}
    </svg>
  );
}

/**
 * Invisible circles on top of every node that detect hover (mouse) and tap (touch),
 * plus the tooltip card itself.
 */
export function NodeTooltipLayer({ tree, hovered, setHovered }) {
  const layout = useMemo(() => layoutTree(tree), [tree]);
  const { nodes, width, height, maxFrequency } = layout;
  const lastPointer = useRef('mouse');

  // Tap elsewhere hides a tapped tooltip; scrolling or resizing hides any tooltip,
  // because its screen position would be out of date.
  useEffect(() => {
    if (!hovered) return undefined;
    const onPointerDown = (event) => {
      if (!event.target.closest?.('.tooltip-hit')) setHovered(null);
    };
    const hide = () => setHovered(null);
    document.addEventListener('pointerdown', onPointerDown);
    window.addEventListener('scroll', hide, true);
    window.addEventListener('resize', hide);
    return () => {
      document.removeEventListener('pointerdown', onPointerDown);
      window.removeEventListener('scroll', hide, true);
      window.removeEventListener('resize', hide);
    };
  });

  const show = (node, target, pinned) =>
    setHovered({ key: node.key, rect: target.getBoundingClientRect(), pinned });

  return (
    <>
      <svg className="tooltip-hit-overlay" viewBox={`0 0 ${width} ${height}`}>
        {nodes.map((node) => (
          <circle
            key={node.key}
            className="tooltip-hit"
            cx={node.x}
            cy={node.y}
            r={radiusFor(node.frequency, maxFrequency)}
            onPointerDown={(event) => {
              lastPointer.current = event.pointerType;
            }}
            onPointerEnter={(event) => {
              if (event.pointerType !== 'touch') show(node, event.currentTarget, false);
            }}
            onPointerLeave={(event) => {
              if (event.pointerType !== 'touch' && !hovered?.pinned) setHovered(null);
            }}
            onClick={(event) => {
              if (lastPointer.current === 'touch') show(node, event.currentTarget, true);
            }}
          />
        ))}
      </svg>
      {hovered && <Tooltip tree={tree} hovered={hovered} />}
    </>
  );
}

/** The tooltip card, placed next to the node and flipped to stay fully on screen. */
function Tooltip({ tree, hovered }) {
  const ref = useRef(null);
  const path = pathTo(tree, hovered.key);
  const node = path[path.length - 1];

  // Measure the card, then place it: right of the node (or left if it would overflow),
  // top-aligned with the node (or bottom-aligned if it would overflow).
  useLayoutEffect(() => {
    const card = ref.current;
    if (!card) return;
    const { rect } = hovered;
    const w = card.offsetWidth;
    const h = card.offsetHeight;
    const maxX = window.innerWidth - EDGE;
    const maxY = window.innerHeight - EDGE;

    let left = rect.right + GAP;
    if (left + w > maxX) left = rect.left - GAP - w;
    let top = rect.top;
    if (top + h > maxY) top = rect.bottom - h;

    card.style.left = `${Math.max(EDGE, Math.min(left, maxX - w))}px`;
    card.style.top = `${Math.max(EDGE, Math.min(top, maxY - h))}px`;
    card.style.visibility = 'visible';
  });

  if (!node) return null;
  const comparisons = path.length; // = depth, the root is 1

  return createPortal(
    <div ref={ref} className="node-tooltip" role="tooltip" style={{ visibility: 'hidden' }}>
      <div className="node-tooltip-title">KEY: {node.key}</div>
      <div>Frequency: {node.frequency}</div>
      <div>Comparisons: {comparisons}</div>
      <div>
        Cost Contribution: {node.frequency} × {comparisons} = {node.frequency * comparisons}
      </div>
      <div>Search Path: {path.map((n) => n.key).join(' → ')}</div>
    </div>,
    document.body,
  );
}
