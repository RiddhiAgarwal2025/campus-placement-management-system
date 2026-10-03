package com.campusplacement.model;

import java.math.BigDecimal;

public record AcademicRecord(int recordId, String studentId, int semester, BigDecimal sgpa, BigDecimal marks) { }
