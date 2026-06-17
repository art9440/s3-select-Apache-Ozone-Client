package org.example.s3select.sql;

import java.util.List;
import java.util.Optional;

public class ParsedQuery {
  private final boolean selectAll;
  private final List<String> selectedColumns;
  private final Optional<WhereCondition> whereCondition;

  public ParsedQuery(
      boolean selectAll,
      List<String> selectedColumns,
      Optional<WhereCondition> whereCondition
  ) {
    this.selectAll = selectAll;
    this.selectedColumns = selectedColumns;
    this.whereCondition = whereCondition;
  }

  public boolean selectAll() {
    return selectAll;
  }

  public List<String> selectedColumns() {
    return selectedColumns;
  }

  public Optional<WhereCondition> whereCondition() {
    return whereCondition;
  }
}
