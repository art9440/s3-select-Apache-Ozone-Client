package org.example.s3select.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.example.s3select.sql.ParsedQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvSelectEngine {
  public void execute(
      InputStream input,
      OutputStream output,
      ParsedQuery query
  ) throws IOException {
    CSVFormat inputFormat = CSVFormat.DEFAULT.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build();

    CSVFormat outputFormat = CSVFormat.DEFAULT.builder()
        .setRecordSeparator("\n")
        .build();

    try (
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(input, StandardCharsets.UTF_8)
        );
        CSVParser parser = inputFormat.parse(reader);
        BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(output, StandardCharsets.UTF_8)
        );
        CSVPrinter printer = new CSVPrinter(writer, outputFormat)
    ) {
      List<String> headers = parser.getHeaderNames();
      List<String> outputColumns = resolveOutputColumns(query, headers);

      validateColumnsExist(outputColumns, headers);

      query.whereCondition().ifPresent(condition -> {
        if (!headers.contains(condition.column())) {
          throw new IllegalArgumentException(
              "Column from WHERE not found in CSV: " + condition.column()
          );
        }
      });

      printer.printRecord(outputColumns);

      for (CSVRecord record : parser) {
        Map<String, String> row = toRowMap(headers, record);

        boolean matches = query.whereCondition()
            .map(condition -> condition.matches(row))
            .orElse(true);

        if (!matches) {
          continue;
        }

        for (String column : outputColumns) {
          printer.print(row.get(column));
        }

        printer.println();
      }

      printer.flush();
    }
  }

  private List<String> resolveOutputColumns(ParsedQuery query, List<String> headers) {
    if (query.selectAll()) {
      return headers;
    }

    return query.selectedColumns();
  }

  private void validateColumnsExist(List<String> columns, List<String> headers) {
    for (String column : columns) {
      if (!headers.contains(column)) {
        throw new IllegalArgumentException("Column not found in CSV: " + column);
      }
    }
  }

  private Map<String, String> toRowMap(List<String> headers, CSVRecord record) {
    Map<String, String> row = new LinkedHashMap<>();

    for (String header : headers) {
      row.put(header, record.get(header));
    }

    return row;
  }
}
