import { useState } from 'react';

/**
 * The w / c / r table in the format of the course notes: indices 0..n, one staircase row per
 * diagonal j - i. Every number comes from result.courseTable; nothing is computed here except
 * picking out which cells to highlight.
 */
export default function DpTable({ table }) {
  const { n, keys, p, diagonals } = table;
  // The selection remembers which table it belongs to. A new result can have fewer keys,
  // so for a new table we start again at the top cell c(0,n).
  const [selection, setSelection] = useState({ table, i: 0, j: n });
  const selected = selection.table === table ? selection : { i: 0, j: n };
  const setSelected = ({ i, j }) => setSelection({ table, i, j });

  const cellAt = (i, j) => diagonals[j - i][i];
  const current = cellAt(selected.i, selected.j);

  // The C[i,k-1] and C[k,j] cells that the working of the selected cell uses.
  const used = new Set();
  for (const term of current.terms) {
    used.add(`${current.i},${term.k - 1}`);
    used.add(`${term.k},${current.j}`);
  }

  const columns = Array.from({ length: n + 1 }, (_, i) => i);

  return (
    <div className="dp-panel">
      <div className="dp-formulas">
        <div>
          w[i,j] = w[i,j−1] + p<sub>j</sub>
        </div>
        <div>
          C[i,j] = min<sub>i&lt;k≤j</sub> {'{'} C[i,k−1] + C[k,j] {'}'} + w[i,j]
        </div>
      </div>

      <div className="dp-scroll">
        <table className="dp-input-table">
          <tbody>
            <tr>
              <th />
              {columns.map((i) => (
                <th key={i}>{i}</th>
              ))}
            </tr>
            <tr>
              <th>Keys</th>
              {columns.map((i) => (
                <td key={i}>{i === 0 ? '' : keys[i - 1]}</td>
              ))}
            </tr>
            <tr>
              <th>
                p<sub>i</sub>
              </th>
              {columns.map((i) => (
                <td key={i}>{i === 0 ? '' : p[i - 1]}</td>
              ))}
            </tr>
          </tbody>
        </table>
      </div>

      <div className="dp-body">
        <div className="dp-scroll dp-main-scroll">
          <table className="dp-table">
            <thead>
              <tr>
                <th className="dp-corner">i →</th>
                {columns.map((i) => (
                  <th key={i}>{i}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {diagonals.map((row, d) => (
                <tr key={d}>
                  <th className="dp-row-label">j − i = {d}</th>
                  {row.map((cell) => {
                    const isSelected = cell.i === selected.i && cell.j === selected.j;
                    const isUsed = used.has(`${cell.i},${cell.j}`);
                    const classes = ['dp-cell', isSelected ? 'selected' : '', isUsed ? 'used' : ''].join(' ');
                    const select = () => setSelected({ i: cell.i, j: cell.j });
                    return (
                      <td
                        key={cell.i}
                        className={classes}
                        role="button"
                        tabIndex={0}
                        aria-pressed={isSelected}
                        onClick={select}
                        onKeyDown={(event) => {
                          if (event.key === 'Enter' || event.key === ' ') {
                            event.preventDefault();
                            select();
                          }
                        }}
                      >
                        <div>
                          w<sub>{sub(cell.i, cell.j, n)}</sub> = {cell.w}
                        </div>
                        <div>
                          c<sub>{sub(cell.i, cell.j, n)}</sub> = {cell.c}
                        </div>
                        <div>
                          r<sub>{sub(cell.i, cell.j, n)}</sub> = {cell.r}
                        </div>
                      </td>
                    );
                  })}
                  {/* Empty space to the right of the staircase. */}
                  {Array.from({ length: d }, (_, e) => (
                    <td key={`empty-${e}`} className="dp-empty" />
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <Working cell={current} n={n} keys={keys} p={p} cellAt={cellAt} />
      </div>

      <p className="dp-legend">
        <span className="dp-swatch selected" /> selected cell
        <span className="dp-swatch used" /> cells used in its working
        <span className="muted"> · click any cell to see how it was computed</span>
      </p>
    </div>
  );
}

/** Subscript text for (i, j): "02" normally, "1,12" once indices can have two digits. */
function sub(i, j, n) {
  return n >= 10 ? `${i},${j}` : `${i}${j}`;
}

/** The working for one cell, written the way the course notes write it. */
function Working({ cell, n, keys, p, cellAt }) {
  const { i, j } = cell;
  const s = sub(i, j, n);

  if (i === j) {
    return (
      <div className="dp-working">
        <h3>
          Cell ({i},{j})
        </h3>
        <p>
          w<sub>{s}</sub> = 0, c<sub>{s}</sub> = 0, r<sub>{s}</sub> = 0
        </p>
        <p className="muted">j − i = 0: an empty tree, so everything is 0.</p>
      </div>
    );
  }

  const prev = cellAt(i, j - 1);
  const minSum = cell.c - cell.w;

  return (
    <div className="dp-working">
      <h3>
        Cell ({i},{j})
      </h3>

      <p>
        w<sub>{s}</sub> = w<sub>{sub(i, j - 1, n)}</sub> + p<sub>{j}</sub> = {prev.w} + {p[j - 1]} ={' '}
        {cell.w}
      </p>

      <p>
        C[{i},{j}] = min<sub>{i}&lt;k≤{j}</sub> {'{ '}
        {cell.terms.map((t, index) => (
          <span key={t.k} className="dp-term">
            C[{i},{t.k - 1}] + C[{t.k},{j}]
            {index < cell.terms.length - 1 ? ', ' : ''}
          </span>
        ))}
        {' }'} + w[{i},{j}]
      </p>

      <p>
        = min{'{ '}
        {cell.terms.map((t, index) => (
          <span key={t.k} className={`dp-term ${t.k === cell.r ? 'chosen' : ''}`}>
            {t.left} + {t.right}
            {index < cell.terms.length - 1 ? ', ' : ''}
          </span>
        ))}
        {' }'} + {cell.w} = {minSum} + {cell.w} = {cell.c}, so r<sub>{s}</sub> = {cell.r}
      </p>

      <p className="muted">
        k = {cell.r} gives the minimum, so key {keys[cell.r - 1]} is the root of keys {i + 1}..{j}.
      </p>
    </div>
  );
}
