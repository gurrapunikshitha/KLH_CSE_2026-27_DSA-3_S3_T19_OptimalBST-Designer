/**
 * The headline numbers. Every value comes straight from the backend response.
 */
export default function ResultSummary({ result }) {
  if (!result) {
    return (
      <section className="card summary empty">
        <h2>
          <span className="step">2</span> Result
        </h2>
        <p className="muted">Enter keys and frequencies, then press <strong>Generate</strong>.</p>
      </section>
    );
  }

  return (
    <section className="card summary">
      <h2>
        <span className="step">2</span> Result
      </h2>

      <div className="headline">
        <div className="headline-label">Expected search cost</div>
        <div className="headline-value">{result.expectedCost.toFixed(4)}</div>
        <div className="headline-caption">average comparisons per search</div>
        <div className="formula">
          = minimum cost / total frequency = {result.minCost} / {result.totalFrequency}
        </div>
      </div>

      <div className="stat-row">
        <div className="stat">
          <div className="stat-value">{result.minCost}</div>
          <div className="stat-label">Minimum (weighted) cost</div>
          <div className="stat-hint">Σ depth × frequency = cost[1][{result.n}]</div>
        </div>
        <div className="stat">
          <div className="stat-value">{result.totalFrequency}</div>
          <div className="stat-label">Total frequency</div>
          <div className="stat-hint">Σ frequency = w[1][{result.n}]</div>
        </div>
        <div className="stat">
          <div className="stat-value">{result.n}</div>
          <div className="stat-label">Keys</div>
        </div>
      </div>

      {result.notices.map((notice) => (
        <p key={notice} className="notice">{notice}</p>
      ))}
    </section>
  );
}
