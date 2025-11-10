package com.logicleaf.invplatform.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyMetric {
    private String month;
    private double value;
}
