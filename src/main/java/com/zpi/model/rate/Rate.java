package com.zpi.model.rate;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rate {

    private String no;

    private String effectiveDate;

    private double mid;
}
