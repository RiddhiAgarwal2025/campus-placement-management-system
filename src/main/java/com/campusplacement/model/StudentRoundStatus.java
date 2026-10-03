package com.campusplacement.model;

import java.time.LocalDate;

public record StudentRoundStatus(int applicationId, String companyName, String position, int sequenceNo,
                                 String roundName, LocalDate roundDate, String roundStatus, String result) { }
