package org.example.s3select.sql;

public enum Operator {
  EQ("="),
  NE("!="),
  GT(">"),
  LT("<"),
  GTE(">="),
  LTE("<=");

  private final String sql;

  Operator(String sql) {
    this.sql = sql;
  }

  public String sql() {
    return sql;
  }
}
