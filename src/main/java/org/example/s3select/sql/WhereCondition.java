package org.example.s3select.sql;

import java.util.Map;

public class WhereCondition {
  private final String column;
  private final Operator operator;
  private final String expectedValue;

  public WhereCondition(String column, Operator operator, String expectedValue) {
    this.column = column;
    this.operator = operator;
    this.expectedValue = expectedValue;
  }

  public String column() {
    return column;
  }

  public Operator operator() {
    return operator;
  }

  public String expectedValue() {
    return expectedValue;
  }

  public boolean matches(Map<String, String> row) {
    String actualValue = row.get(column);

    if (actualValue == null) {
      throw new IllegalArgumentException("Column not found in CSV: " + column);
    }

    return switch (operator) {
      case EQ -> compareAsString(actualValue) == 0;
      case NE -> compareAsString(actualValue) != 0;
      case GT -> compareAsNumber(actualValue) > 0;
      case LT -> compareAsNumber(actualValue) < 0;
      case GTE -> compareAsNumber(actualValue) >= 0;
      case LTE -> compareAsNumber(actualValue) <= 0;
    };
  }

  private int compareAsString(String actualValue) {
    return actualValue.compareTo(expectedValue);
  }

  private int compareAsNumber(String actualValue) {
    double actual = Double.parseDouble(actualValue);
    double expected = Double.parseDouble(expectedValue);
    return Double.compare(actual, expected);
  }
}
