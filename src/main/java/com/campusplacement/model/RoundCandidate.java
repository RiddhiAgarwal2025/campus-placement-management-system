package com.campusplacement.model;

/** A candidate's position for a particular round: previous-round result and this round's result. */
public record RoundCandidate(int applicationId, String studentId, String studentName, String deptCode,
                             String applicationStatus, String previousResult, String currentResult, String remarks) {
    /** True when the candidate may receive a result in this round. */
    public boolean canBeEvaluated(boolean firstRound) {
        return firstRound || "PASS".equals(previousResult);
    }
}
