// =============================================================================
// App.js — DataForge React Frontend
//
// This is the main (and only) component for the DataForge UI.
// It lets users choose a data type, row count, and format, then calls the
// Spring Boot backend to generate and preview synthetic test data.
//
// REACT CONCEPTS USED:
//   useState  — stores values that can change (form inputs, results, loading state)
//   Axios     — HTTP client that calls the Spring Boot API (simpler than fetch)
//
// DATA FLOW:
//   User fills form → clicks Generate → Axios POSTs to /api/generate
//   → response stored in state → table renders the first 10 rows
//   → Download button fetches /api/export/{format} and saves the file
// =============================================================================

import React, { useState } from 'react';
import axios from 'axios';
import './App.css';

// Base URL of the Spring Boot backend
const API_BASE = 'http://localhost:7070';

function App() {
  // -------------------------------------------------------------------------
  // Form state — what the user has selected
  // -------------------------------------------------------------------------
  const [dataType, setDataType] = useState('users');
  const [count,    setCount]    = useState(10);
  const [format,   setFormat]   = useState('json');

  // -------------------------------------------------------------------------
  // Result state — what came back from the API
  // -------------------------------------------------------------------------
  const [loading,      setLoading]      = useState(false);
  const [error,        setError]        = useState(null);
  const [result,       setResult]       = useState(null);  // full GenerationResponse
  const [tableRows,    setTableRows]    = useState([]);    // first 10 rows for preview
  const [tableColumns, setTableColumns] = useState([]);    // column headers

  // -------------------------------------------------------------------------
  // Generate button handler
  // -------------------------------------------------------------------------
  const handleGenerate = async () => {
    // Reset previous results before starting
    setLoading(true);
    setError(null);
    setResult(null);
    setTableRows([]);
    setTableColumns([]);

    try {
      // POST to Spring Boot — body matches GenerationRequest fields
      const response = await axios.post(`${API_BASE}/api/generate`, {
        type:   dataType,
        count:  Number(count),
        format: format,
      });

      const data = response.data; // GenerationResponse object
      setResult(data);

      // Build the preview table from the data payload.
      // For JSON format the payload is an array of objects.
      // For CSV/SQL/XML it's a string — we show it as raw text instead.
      if (format === 'json' && Array.isArray(data.data) && data.data.length > 0) {
        // Flatten one level: if a field is a nested object (e.g. address),
        // show it as a JSON string so it fits in a table cell.
        const flattenRow = (row) => {
          const flat = {};
          for (const [key, val] of Object.entries(row)) {
            flat[key] = (val !== null && typeof val === 'object')
              ? JSON.stringify(val)
              : String(val ?? '');
          }
          return flat;
        };

        const preview = data.data.slice(0, 10).map(flattenRow);
        setTableColumns(Object.keys(preview[0]));
        setTableRows(preview);
      }

    } catch (err) {
      // Show a friendly message — prefer the API's error field if available
      const msg =
        err.response?.data?.error ||
        err.response?.data?.message ||
        err.message ||
        'An unexpected error occurred.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  // -------------------------------------------------------------------------
  // Download button handler
  // -------------------------------------------------------------------------
  const handleDownload = async () => {
    try {
      // GET /api/export/{format}?type=...&count=...
      const response = await axios.get(`${API_BASE}/api/export/${format}`, {
        params:       { type: dataType, count: Number(count) },
        responseType: 'blob',  // treat the response as a file blob, not JSON
      });

      // Determine the file extension from the chosen format
      const extensions = { json: 'json', csv: 'csv', sql: 'sql', xml: 'xml' };
      const ext      = extensions[format] || 'txt';
      const filename = `dataforge-${dataType}-${count}.${ext}`;

      // Create a temporary <a> element, click it to trigger the browser download,
      // then immediately remove the element — standard browser download trick
      const url  = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href  = url;
      link.setAttribute('download', filename);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setError('Download failed: ' + (err.message || 'Unknown error'));
    }
  };

  // -------------------------------------------------------------------------
  // Render
  // -------------------------------------------------------------------------
  return (
    <div className="app">

      {/* ===== HEADER ===== */}
      <header className="header">
        <h1 className="header-title">DataForge</h1>
        <p className="header-subtitle">Synthetic Test Data Generator</p>
      </header>

      {/* ===== MAIN CONTENT ===== */}
      <main className="main">

        {/* --- Generation Form --- */}
        <section className="card form-card">
          <h2 className="card-title">Generate Data</h2>

          <div className="form-grid">

            {/* Data Type */}
            <div className="form-group">
              <label className="form-label">Data Type</label>
              <select
                className="form-control"
                value={dataType}
                onChange={(e) => setDataType(e.target.value)}
              >
                <option value="users">Users</option>
                <option value="transactions">Transactions</option>
                <option value="logs">Logs</option>
                <option value="iot">IoT Events</option>
                <option value="ecommerce">Ecommerce Orders</option>
              </select>
            </div>

            {/* Row Count */}
            <div className="form-group">
              <label className="form-label">Row Count</label>
              <input
                type="number"
                className="form-control"
                value={count}
                min="1"
                max="1000"
                onChange={(e) => setCount(e.target.value)}
              />
            </div>

            {/* Format */}
            <div className="form-group">
              <label className="form-label">Format</label>
              <select
                className="form-control"
                value={format}
                onChange={(e) => setFormat(e.target.value)}
              >
                <option value="json">JSON</option>
                <option value="csv">CSV</option>
                <option value="sql">SQL</option>
                <option value="xml">XML</option>
              </select>
            </div>

          </div>

          {/* Generate Button */}
          <button
            className="btn-generate"
            onClick={handleGenerate}
            disabled={loading}
          >
            {loading ? (
              <span className="spinner-row">
                <span className="spinner" /> Generating…
              </span>
            ) : (
              '⚡ Generate'
            )}
          </button>
        </section>

        {/* --- Error Message --- */}
        {error && (
          <div className="error-box">
            <strong>Error:</strong> {error}
          </div>
        )}

        {/* --- Results Section --- */}
        {result && (
          <section className="card results-card">

            {/* Stats Bar */}
            <div className="stats-bar">
              <div className="stat">
                <span className="stat-label">Type</span>
                <span className="stat-value">{result.type}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Rows</span>
                <span className="stat-value">{result.count}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Format</span>
                <span className="stat-value">{result.format?.toUpperCase()}</span>
              </div>
              <div className="stat">
                <span className="stat-label">Time</span>
                <span className="stat-value">{result.generationTimeMs} ms</span>
              </div>
            </div>

            {/* Download Button */}
            <div className="download-row">
              <button className="btn-download" onClick={handleDownload}>
                ⬇ Download {format.toUpperCase()} ({count} rows)
              </button>
            </div>

            {/* Table Preview — only shown for JSON format */}
            {format === 'json' && tableRows.length > 0 && (
              <div className="table-section">
                <h3 className="table-heading">
                  Preview — first {tableRows.length} of {result.count} rows
                </h3>
                <div className="table-wrapper">
                  <table className="data-table">
                    <thead>
                      <tr>
                        {tableColumns.map((col) => (
                          <th key={col}>{col}</th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {tableRows.map((row, i) => (
                        <tr key={i}>
                          {tableColumns.map((col) => (
                            <td key={col} title={row[col]}>
                              {/* Truncate long values so the table stays readable */}
                              {row[col]?.length > 40
                                ? row[col].substring(0, 40) + '…'
                                : row[col]}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            )}

            {/* Raw text preview for CSV / SQL / XML */}
            {format !== 'json' && typeof result.data === 'string' && (
              <div className="raw-section">
                <h3 className="table-heading">Preview</h3>
                <pre className="raw-preview">
                  {/* Show first 2000 characters so we don't flood the page */}
                  {result.data.substring(0, 2000)}
                  {result.data.length > 2000 && '\n… (truncated — download for full output)'}
                </pre>
              </div>
            )}

          </section>
        )}

      </main>

      {/* ===== FOOTER ===== */}
      <footer className="footer">
        <p>DataForge — CSE3253 DevOps Project &nbsp;|&nbsp; Aashika M &nbsp;|&nbsp; 23FE10CSE00482</p>
      </footer>

    </div>
  );
}

export default App;
