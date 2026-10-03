package com.campusplacement.model;

import java.util.List;

/** Generic tabular result (column labels and rows) used for reports. */
public record TableData(List<String> columns, List<Object[]> rows) { }
