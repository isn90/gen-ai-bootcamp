package com.epam.training.gen.ai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LightModel {
    private int id;
    private String name;
    private Boolean isOn;
    private Brightness brightness;
    private String color;


    public enum Brightness {
        LOW,
        MEDIUM,
        HIGH
    }
}
