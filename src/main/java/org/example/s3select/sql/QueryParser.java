package org.example.s3select.sql;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
  private static final Pattern SELECT_PATTERN = Pattern.compile(
      "^\\s*SELECT\\s+(.+?)\\s+FROM\\s+S3Object(?:\\s+WHERE\\s+(.+))?\\s*$",
      Pattern.CASE_INSENSITIVE
  );

  private static final Pattern WHERE_PATTERN = Pattern.compile(
      "^\\s*([a-zA-Z_][a-zA-Z0-9_]*)\\s*(>=|<=|!=|=|>|<)\\s*(.+?)\\s*$",
      Pattern.CASE_INSENSITIVE
  );

  public ParsedQuery parse(String sql) {
    if (sql == null || sql.isBlank()) {
      throw new IllegalArgumentException("SQL query must not be empty");
    }

    Matcher selectMatcher = SELECT_PATTERN.matcher(sql);

    if (!selectMatcher.matches()) {
      throw new IllegalArgumentException(
          "Unsupported SQL. Expected: SELECT * FROM S3Object [WHERE column operator value]"
      );
    }

    String selectPart = selectMatcher.group(1).trim();
    String wherePart = selectMatcher.group(2);

    boolean selectAll = selectPart.equals("*");

    List<String> selectedColumns = selectAll
        ? List.of()
        : parseSelectedColumns(selectPart);

    Optional<WhereCondition> whereCondition = wherePart == null
        ? Optional.empty()
        : Optional.of(parseWhere(wherePart));

    return new ParsedQuery(selectAll, selectedColumns, whereCondition);
  }

  private List<String> parseSelectedColumns(String selectPart) {
    return Arrays.stream(selectPart.split(","))
        .map(String::trim)
        .filter(column -> !column.isBlank())
        .toList();
  }

  private WhereCondition parseWhere(String wherePart) {
    Matcher whereMatcher = WHERE_PATTERN.matcher(wherePart);

    if (!whereMatcher.matches()) {
      throw new IllegalArgumentException(
          "Unsupported WHERE. Expected: WHERE column = value, !=, >, <, >=, <="
      );
    }

    String column = whereMatcher.group(1).trim();
    Operator operator = parseOperator(whereMatcher.group(2).trim());
    String value = normalizeValue(whereMatcher.group(3).trim());

    return new WhereCondition(column, operator, value);
  }

  private Operator parseOperator(String operator) {
    return switch (operator) {
      case "=" -> Operator.EQ;
      case "!=" -> Operator.NE;
      case ">" -> Operator.GT;
      case "<" -> Operator.LT;
      case ">=" -> Operator.GTE;
      case "<=" -> Operator.LTE;
      default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
    };
  }

  private String normalizeValue(String value) {
    if (
        (value.startsWith("'") && value.endsWith("'"))
            || (value.startsWith("\"") && value.endsWith("\""))
    ) {
      return value.substring(1, value.length() - 1);
    }

    return value;
  }
}
