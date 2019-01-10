package com.zpi.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimePeriod {

    WEEK("Ostatni tydzień"),

    TWO_WEEKS("Ostatnie 2 tygodnie"),

    MONTH("Ostatni miesiąc"),

    QUARTER("Ostatni kwartał"),

    HALF_A_YEAR("Ostatnie półrocze"),

    YEAR("Ostatni rok");

    private String label;

    @Override
    public String toString() {
        return label;
    }
}
