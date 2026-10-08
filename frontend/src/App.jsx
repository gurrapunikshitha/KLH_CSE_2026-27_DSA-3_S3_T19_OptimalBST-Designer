import { useState } from 'react';
import InputPanel from './components/InputPanel.jsx';
import ResultSummary from './components/ResultSummary.jsx';
import TreeView from './components/TreeView.jsx';
import DpTable from './components/DpTable.jsx';
import SearchPanel, { SearchOverlay, useTreeSearch } from './components/SearchPanel.jsx';
import CostBreakdown, { HoverOverlay } from './components/CostBreakdown.jsx';
import { NodeTooltipLayer, TooltipPathOverlay, useNodeTooltip } from './components/NodeTooltip.jsx';
import { solveObst } from './api.js';

export default function App() {
  const [rows, setRows] = useState([{ id: 0, key: '', freq: '' }]);
  const [result, setResult] = useState(null);
  const [serverErrors, setServerErrors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showDpTable, setShowDpTable] = useState(false);
  const treeSearch = useTreeSearch(result?.tree ?? null);
  const [hoveredKey, setHoveredKey] = useState(null);
  const nodeTooltip = useNodeTooltip(result?.tree ?? null);

  async function generate() {
    setLoading(true);
    setServerErrors([]);
    try {
      const keys = rows.map((row) => Number(row.key.trim()));
      const frequencies = rows.map((row) => Number(row.freq.trim()));
      setResult(await solveObst(keys, frequencies));
    } catch (error) {
      setServerErrors(error.messages ?? [error.message]);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <header className="page-header">
        <span className="hero-badge">INTERVAL DYNAMIC PROGRAMMING</span>
        <h1>Optimal Binary Search Tree Designer</h1>
        <p>
          Visualize how interval dynamic programming transforms search frequencies into an
          optimal BST.
        </p>
      </header>

      <div className="top-grid">
        <InputPanel
          rows={rows}
          onRowsChange={setRows}
          onGenerate={generate}
          loading={loading}
          serverErrors={serverErrors}
        />
        <ResultSummary result={result} />
      </div>

      {result && (
        <section className="card">
          <div className="section-head">
            <h2>
              <span className="step">3</span> Optimal tree
            </h2>
            <p className="legend">
              <span className="legend-dot small" /> rare
              <span className="legend-dot large" /> frequent
              <span className="muted"> · larger, darker node = searched more often</span>
            </p>
          </div>
          <div className="tree-scroll">
            <div className="search-tree-wrap">
              <TreeView tree={result.tree} />
              <TooltipPathOverlay tree={result.tree} hovered={nodeTooltip.hovered} />
              <SearchOverlay tree={result.tree} result={treeSearch.result} />
              <HoverOverlay tree={result.tree} hoveredKey={hoveredKey} />
              <NodeTooltipLayer
                tree={result.tree}
                hovered={nodeTooltip.hovered}
                setHovered={nodeTooltip.setHovered}
              />
            </div>
          </div>
        </section>
      )}

      {result && <SearchPanel search={treeSearch.search} result={treeSearch.result} />}

      {result && <CostBreakdown result={result} hoveredKey={hoveredKey} onHoverKey={setHoveredKey} />}

      {result && (
        <section className="card">
          <div className="section-head">
            <h2>
              <span className="step">6</span> DP table
            </h2>
            <button type="button" className="secondary" onClick={() => setShowDpTable((open) => !open)}>
              {showDpTable ? 'Hide DP Table' : 'View DP Table'}
            </button>
          </div>
          {showDpTable && <DpTable table={result.courseTable} />}
        </section>
      )}
    </div>
  );
}
