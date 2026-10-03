package com.campusplacement.model;

import java.util.List;

/** Outcome of the eligibility engine, with one human-readable reason per failed criterion. */
public record EligibilityResult(Student student, boolean eligible, List<String> reasons) {
    public String reasonText() {
        return eligible ? "Meets all criteria" : String.join("; ", reasons);
    }
}
