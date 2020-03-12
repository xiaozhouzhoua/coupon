package com.gateway.predicates;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeBetweenConfig {
    private LocalTime start;
    private LocalTime end;
}
