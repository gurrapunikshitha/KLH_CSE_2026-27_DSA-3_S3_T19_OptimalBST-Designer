import { useEffect, useMemo, useRef, useState } from 'react';
import { layoutTree, radiusFor } from '../utils/treeLayout.js';

const STEP_MS = 500; // time between two visited nodes lighting up

/**
 * Plain BST search from the root: go left if the target is smaller, right if it is larger,
 * stop when it is found or there is no child. Returns the visited nodes in order.
 */
function searchPath(tree, target) {
  const path = [];
  let node = tree;
  while (node) {
    path.push(node);
    if (target === node.key) break;
    node = target < node.key ? node.left : node.right;
  }
  return path;
}

/**
 * Search state plus the step-by-step animation. The state remembers which tree it belongs to,
 * so generating a new tree clears the search.
 */
export function useTreeSearch(tree) {
  const [state, setState] = useState(null); // { tree, target, keys, found, shown }
  const timer = useRef(null);

  const stop = () => {
    if (timer.current !== null) {
      clearInterval(timer.current);
      timer.current = null;
    }
  };

  // Stop the animation when the tree changes or the component goes away.
  useEffect(() => stop, [tree]);

  function search(target) {
    stop();
    const path = searchPath(tree, target);
    const found = path.length > 0 && path[path.length - 1].key === target;
    setState({ tree, target, keys: path.map((n) => n.key), found, shown: 1 });

    if (path.length > 1) {
      timer.current = setInterval(() => {
        setState((s) => {
          const shown = Math.min(s.shown + 1, s.keys.length);
          if (shown === s.keys.length) stop();
          return { ...s, shown };
        });
      }, STEP_MS);
    }
  }

  const current = state && state.tree === tree ? state : null;
  return { search, result: current };
}

/** The "Search the tree" card. */
export default function SearchPanel({ search, result }) {
  const [text, setText] = useState('');
  const [error, setError] = useState('');

  function submit(event) {
    event.preventDefault();
    const value = text.trim();
    if (value === '') {
      setError('Enter a key to search for.');
      return;
    }
    const target = Number(value);
    if (!Number.isFinite(target)) {
      setError(`"${value}" is not a number.`);
      return;
    }
    setError('');
    search(target);
  }

  const done = result && result.shown === result.keys.length;

  return (
    <section className="card">
      <h2>
        <span className="step">4</span> Search the tree
      </h2>

      <form className="search-form" onSubmit={submit} noValidate>
        <div className="search-field">
          <input
            type="text"
            inputMode="numeric"
            placeholder="Enter key..."
            value={text}
            onChange={(event) => setText(event.target.value)}
            aria-label="Key to search for"
            aria-invalid={error ? 'true' : 'false'}
          />
          {error && <div className="field-error">{error}</div>}
        </div>
        <button type="submit" className="primary">🔍 Search</button>
      </form>

      {result && (
        <dl className="search-results">
          <dt>Result</dt>
          <dd>
            {done ? (
              <span className={result.found ? 'search-found' : 'search-not-found'}>
                Key {result.target} {result.found ? 'found' : 'not found'}
              </span>
            ) : (
              <span className="muted">Searching…</span>
            )}
          </dd>
          <dt>Comparisons</dt>
          <dd>{result.shown}</dd>
          <dt>Search path</dt>
          <dd className="search-path">{result.keys.slice(0, result.shown).join(' → ')}</dd>
        </dl>
      )}
    </section>
  );
}

/**
 * Draws the search highlights on top of the existing tree. It uses the same layout and the
 * same viewBox as TreeView, so every ring and edge lands exactly on the drawn tree.
 */
export function SearchOverlay({ tree, result }) {
  const layout = useMemo(() => layoutTree(tree), [tree]);
  if (!result) return null;

  const { byKey, width, height, maxFrequency } = layout;
  const visited = result.keys.slice(0, result.shown).map((key) => byKey.get(key));
  const done = result.shown === result.keys.length;

  return (
    <svg
      className="search-overlay"
      viewBox={`0 0 ${width} ${height}`}
      aria-hidden="true"
    >
      {visited.slice(1).map((to, index) => {
        const from = visited[index];
        // Shorten the edge so it runs from circle edge to circle edge, not over the nodes.
        const dx = to.x - from.x;
        const dy = to.y - from.y;
        const length = Math.hypot(dx, dy);
        const r1 = radiusFor(from.frequency, maxFrequency) + 4;
        const r2 = radiusFor(to.frequency, maxFrequency) + 4;
        return (
          <line
            key={`${from.key}-${to.key}`}
            className="search-edge"
            x1={from.x + (dx * r1) / length}
            y1={from.y + (dy * r1) / length}
            x2={to.x - (dx * r2) / length}
            y2={to.y - (dy * r2) / length}
          />
        );
      })}

      {visited.map((node, index) => {
        const isLast = done && index === visited.length - 1;
        const state = isLast ? (result.found ? 'found' : 'not-found') : 'visited';
        return (
          <circle
            key={node.key}
            className={`search-ring ${state}`}
            cx={node.x}
            cy={node.y}
            r={radiusFor(node.frequency, maxFrequency) + 4}
          />
        );
      })}
    </svg>
  );
}
