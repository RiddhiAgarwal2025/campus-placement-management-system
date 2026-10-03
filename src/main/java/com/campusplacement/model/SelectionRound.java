package com.campusplacement.model;

import java.time.LocalDate;

public record SelectionRound(int roundId, int driveId, String name, int sequenceNo, LocalDate roundDate, String status,
                             int passCount, int failCount) {
    @Override
    public String toString() { return sequenceNo + ". " + name; }
}
