// =============================================================================
// ExportService.java
// Converts generated data (List<Map<String,Object>>) into four export formats.
//
// WHY MULTIPLE FORMATS?
//   Different tools consume data differently:
//
//   JSON  — the default. Used by frontend apps, APIs, Postman, and most modern
//           tools. Human-readable, hierarchical, handles nested objects well.
//
//   CSV   — "Comma-Separated Values". Used by Excel, Google Sheets, pandas,
//           business analysts, and data pipelines. Flat (no nesting), one row
//           per record, first row is the header.
//
//   SQL   — Used to load test data directly into a relational database (MySQL,
//           PostgreSQL, SQLite). The INSERT statements can be run in any SQL
//           client or migration script.
//
//   XML   — Used by older enterprise systems, SOAP web services, Android
//           resources, and tools like Apache Kafka with XML schemas.
//
// LIBRARIES USED:
//   - Jackson ObjectMapper (built into Spring Boot) for JSON serialization
//   - OpenCSV's CSVWriter for safe, RFC-4180 compliant CSV output
//   - Plain StringBuilder for SQL and XML (simple enough to build manually)
// =============================================================================

package com.dataforge.app.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * Spring-managed service responsible for serialising data into export formats.
 */
@Service
public class ExportService {

    // Jackson's ObjectMapper converts Java objects ↔ JSON.
    // We inject the Spring-managed instance so it uses the same config as the rest of the app.
    private final ObjectMapper objectMapper;

    public ExportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // =========================================================================
    // JSON export
    // =========================================================================

    /**
     * Serialises data to a pretty-printed JSON string.
     *
     * "Pretty-printed" means indented and human-readable, not compressed.
     * Example output:
     * [
     *   {
     *     "id": "abc-123",
     *     "firstName": "Jane"
     *   }
     * ]
     *
     * @param data list of records to serialise
     * @return JSON string
     */
    public String toJson(List<Map<String, Object>> data) {
        try {
            // INDENT_OUTPUT adds line breaks and spaces for readability
            return objectMapper
                    .writer()
                    .with(SerializationFeature.INDENT_OUTPUT)
                    .writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise data to JSON: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // CSV export
    // =========================================================================

    /**
     * Converts data to a CSV string using OpenCSV.
     *
     * OpenCSV handles all the tricky edge cases automatically:
     *   - Wrapping values that contain commas in double quotes
     *   - Escaping double quotes inside values
     *   - Correct line endings (CRLF per RFC 4180)
     *
     * Nested objects (e.g. the address map in user data) are serialised as
     * a JSON string so the cell remains readable in a spreadsheet.
     *
     * @param data list of records
     * @return CSV string (first row = header, subsequent rows = values)
     */
    public String toCsv(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        StringWriter stringWriter = new StringWriter();

        try (CSVWriter csvWriter = new CSVWriter(stringWriter)) {
            // --- Header row: use the keys of the first map as column names ---
            String[] headers = data.get(0).keySet().toArray(new String[0]);
            csvWriter.writeNext(headers);

            // --- Data rows: one row per record ---
            for (Map<String, Object> row : data) {
                String[] values = new String[headers.length];
                for (int i = 0; i < headers.length; i++) {
                    Object val = row.get(headers[i]);
                    values[i] = valueToString(val);
                }
                csvWriter.writeNext(values);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialise data to CSV: " + e.getMessage(), e);
        }

        return stringWriter.toString();
    }

    // =========================================================================
    // SQL export
    // =========================================================================

    /**
     * Generates a series of SQL INSERT statements.
     *
     * Example output:
     *   INSERT INTO users (id, firstName, lastName) VALUES ('abc', 'Jane', 'Doe');
     *
     * Rules applied:
     *   - String values are wrapped in single quotes.
     *   - Numbers (Integer, Long, Double, Float) are written without quotes.
     *   - Single quotes inside string values are escaped by doubling them (SQL standard).
     *   - Nested objects are serialised to JSON and treated as strings.
     *   - NULL values are written as SQL NULL.
     *
     * @param data      list of records
     * @param tableName the SQL table name to insert into
     * @return SQL INSERT statements as a single string
     */
    public String toSql(List<Map<String, Object>> data, String tableName) {
        if (data == null || data.isEmpty()) {
            return "";
        }

        // Build the column list from the first record (all records share the same keys)
        String[] columns = data.get(0).keySet().toArray(new String[0]);
        String columnList = String.join(", ", columns);

        StringBuilder sb = new StringBuilder();
        // Add a header comment so the SQL file is self-documenting
        sb.append("-- Generated by DataForge\n");
        sb.append("-- Table: ").append(tableName).append("\n\n");

        for (Map<String, Object> row : data) {
            sb.append("INSERT INTO ").append(tableName)
              .append(" (").append(columnList).append(")")
              .append(" VALUES (");

            for (int i = 0; i < columns.length; i++) {
                Object val = row.get(columns[i]);
                sb.append(valueToSql(val));
                if (i < columns.length - 1) {
                    sb.append(", ");
                }
            }

            sb.append(");\n");
        }

        return sb.toString();
    }

    // =========================================================================
    // XML export
    // =========================================================================

    /**
     * Generates a well-formed XML document from the data.
     *
     * Example output:
     *   <?xml version="1.0" encoding="UTF-8"?>
     *   <users>
     *     <user>
     *       <id>abc-123</id>
     *       <firstName>Jane</firstName>
     *     </user>
     *   </users>
     *
     * XML special characters in values (&, <, >, ', ") are escaped automatically.
     * Nested objects are serialised to a JSON string inside their element.
     * NULL values produce empty elements: <field/>
     *
     * @param data        list of records
     * @param rootElement the outer XML tag (e.g. "users")
     * @param itemElement the per-record XML tag (e.g. "user")
     * @return XML string including the XML declaration
     */
    public String toXml(List<Map<String, Object>> data, String rootElement, String itemElement) {
        if (data == null || data.isEmpty()) {
            return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<" + rootElement + "/>";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<").append(rootElement).append(">\n");

        for (Map<String, Object> row : data) {
            sb.append("  <").append(itemElement).append(">\n");

            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String tag = entry.getKey();
                Object val = entry.getValue();

                if (val == null) {
                    // Empty self-closing element for null values
                    sb.append("    <").append(tag).append("/>\n");
                } else {
                    String text = xmlEscape(valueToString(val));
                    sb.append("    <").append(tag).append(">")
                      .append(text)
                      .append("</").append(tag).append(">\n");
                }
            }

            sb.append("  </").append(itemElement).append(">\n");
        }

        sb.append("</").append(rootElement).append(">");
        return sb.toString();
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    /**
     * Converts any value to a plain string for CSV and XML output.
     * Complex types (Map, List) are serialised as compact JSON.
     */
    private String valueToString(Object val) {
        if (val == null) {
            return "";
        }
        // Nested maps/lists — serialise as compact JSON so the cell is readable
        if (val instanceof Map || val instanceof List) {
            try {
                return objectMapper.writeValueAsString(val);
            } catch (Exception e) {
                return val.toString();
            }
        }
        return String.valueOf(val);
    }

    /**
     * Formats a value for use inside a SQL VALUES clause.
     *   - Numbers → unquoted   (42, 9.99)
     *   - Strings → single-quoted with internal quotes doubled  ('Jane''s')
     *   - null    → SQL NULL keyword
     */
    private String valueToSql(Object val) {
        if (val == null) {
            return "NULL";
        }
        // Numbers are written without quotes in SQL
        if (val instanceof Number) {
            return val.toString();
        }
        // Boolean values map to SQL TRUE / FALSE
        if (val instanceof Boolean) {
            return val.toString().toUpperCase();
        }
        // Everything else (including nested objects serialised to JSON) → quoted string
        String str = valueToString(val);
        // Escape single quotes by doubling them (SQL standard)
        str = str.replace("'", "''");
        return "'" + str + "'";
    }

    /**
     * Escapes the five XML special characters so the document remains valid.
     *   &  →  &amp;
     *   <  →  &lt;
     *   >  →  &gt;
     *   "  →  &quot;
     *   '  →  &apos;
     */
    private String xmlEscape(String text) {
        return text
                .replace("&",  "&amp;")   // must be first to avoid double-escaping
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&apos;");
    }
}
