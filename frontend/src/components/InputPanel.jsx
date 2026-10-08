import { useState } from 'react';

export const MAX_KEYS = 15;

// Row ids start at 1; App's starting empty row uses id 0.
let nextRowId = 1;

const INTEGER = /^-?\d+$/;

/**
 * Client-side checks so the user sees problems while typing.
 * The backend repeats all of these checks, so this is only for convenience.
 * Returns { byRow: { [id]: { key?, freq? } }, general: [string], valid: boolean }.
 */
export function validateRows(rows) {
  const byRow = {};
  const general = [];
  const setError = (id, field, message) => {
    byRow[id] = { ...byRow[id], [field]: message };
  };

  if (rows.length === 0) general.push('Add at least one key.');
  if (rows.length > MAX_KEYS) general.push(`At most ${MAX_KEYS} keys are allowed.`);

  const seen = new Map();
  for (const row of rows) {
    const key = row.key.trim();
    const freq = row.freq.trim();

    if (key === '') setError(row.id, 'key', 'Required');
    else if (!INTEGER.test(key)) setError(row.id, 'key', 'Whole number');
    else if (seen.has(Number(key))) setError(row.id, 'key', 'Duplicate');
    else seen.set(Number(key), row.id);

    if (freq === '') setError(row.id, 'freq', 'Required');
    else if (!INTEGER.test(freq) || Number(freq) <= 0) setError(row.id, 'freq', 'Positive integer');
  }

  return { byRow, general, valid: general.length === 0 && Object.keys(byRow).length === 0 };
}

export default function InputPanel({ rows, onRowsChange, onGenerate, loading, serverErrors }) {
  // Errors for empty fields are only shown after the first Generate attempt,
  // so a freshly added row is not immediately red.
  const [attempted, setAttempted] = useState(false);
  const validation = validateRows(rows);

  const updateRow = (id, field, value) =>
    onRowsChange(rows.map((row) => (row.id === id ? { ...row, [field]: value } : row)));

  const removeRow = (id) => onRowsChange(rows.filter((row) => row.id !== id));

  const addRow = () => onRowsChange([...rows, { id: nextRowId++, key: '', freq: '' }]);

  const handleGenerate = (event) => {
    event.preventDefault();
    setAttempted(true);
    if (validation.valid) onGenerate();
  };

  const visibleError = (row, field) => {
    const message = validation.byRow[row.id]?.[field];
    if (!message) return null;
    const isEmpty = row[field].trim() === '';
    return isEmpty && !attempted ? null : message;
  };

  return (
    <form className="card input-panel" onSubmit={handleGenerate} noValidate>
      <h2>
        <span className="step">1</span> Keys &amp; frequencies
      </h2>

      <div className="rows-scroll">
        <table className="input-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Key</th>
              <th>Frequency</th>
              <th aria-label="Remove" />
            </tr>
          </thead>
          <tbody>
            {rows.map((row, index) => {
              const keyError = visibleError(row, 'key');
              const freqError = visibleError(row, 'freq');
              return (
                <tr key={row.id}>
                  <td className="row-number">{index + 1}</td>
                  <td>
                    <input
                      inputMode="numeric"
                      value={row.key}
                      aria-label={`Key ${index + 1}`}
                      aria-invalid={Boolean(keyError)}
                      onChange={(e) => updateRow(row.id, 'key', e.target.value)}
                    />
                    {keyError && <div className="field-error">{keyError}</div>}
                  </td>
                  <td>
                    <input
                      inputMode="numeric"
                      value={row.freq}
                      aria-label={`Frequency ${index + 1}`}
                      aria-invalid={Boolean(freqError)}
                      onChange={(e) => updateRow(row.id, 'freq', e.target.value)}
                    />
                    {freqError && <div className="field-error">{freqError}</div>}
                  </td>
                  <td>
                    <button
                      type="button"
                      className="icon-button"
                      aria-label={`Remove row ${index + 1}`}
                      title="Remove row"
                      onClick={() => removeRow(row.id)}
                    >
                      ×
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      <div className="input-footer">
        <button type="button" className="secondary" onClick={addRow} disabled={rows.length >= MAX_KEYS}>
          + Add key
        </button>
        <button type="submit" className="primary" disabled={loading}>
          {loading ? 'Generating…' : 'Generate'}
        </button>
      </div>

      {attempted && validation.general.map((message) => (
        <p key={message} className="form-error">{message}</p>
      ))}
      {serverErrors.length > 0 && (
        <ul className="error-box" role="alert">
          {serverErrors.map((message) => <li key={message}>{message}</li>)}
        </ul>
      )}
    </form>
  );
}
