package com.newisland.reservation.model;

import java.util.Optional;

public enum ValidationResult {
    VALID,
    PASS_MAX_RANGE_NON_VALID,
    AHEAD_MAX_RANGE_NON_VALID,
    ALREADY_BOOKED_NON_VALID
}
