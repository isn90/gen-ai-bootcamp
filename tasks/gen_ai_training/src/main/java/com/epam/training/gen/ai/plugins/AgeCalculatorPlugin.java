package com.epam.training.gen.ai.plugins;

import com.epam.training.gen.ai.model.LightModel;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.valueOf;

@Slf4j
public class AgeCalculatorPlugin {
    @DefineKernelFunction(name = "calculate_age", description = "Calculates age based on the birth year.")
    public String calculateAge(@KernelFunctionParameter(name = "birthYear", description = "The year of birth", type = String.class) String birthYear) {
        int currentYear = Year.now().getValue();
        int age = currentYear - Integer.parseInt(birthYear);
        log.info("Calculating age for birth year: {} -> Age: {}", birthYear, age);

        if (age < 0) {
            throw new IllegalArgumentException("Birth year cannot be in the future.");
        }

        return String.valueOf(age);
    }
}
