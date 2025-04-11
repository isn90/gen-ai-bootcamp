package com.epam.training.gen.ai.plugins;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class WeatherForecastPlugin {

    private final List<String> weatherConditions = List.of("Hot", "Sunny", "Rainy", "Stormy", "Windy", "Cloudy", "Snowy", "Thunderstorms");

    @DefineKernelFunction(name = "weatherForecast", description = "Method returns the weather forecast of the city with a random conditions and temperature")
    public String fetchWeatherForecast(String city) {
        log.info("Fetching weather forecast for the city of = {}", city);
        double temperature = Math.random() * 50;
        String weatherCondition = weatherConditions.get((int) (Math.random() * weatherConditions.size()));
        return "The weather in the city of " + city + " is " + weatherCondition + " with the average temperatures of " + temperature;
    }

}
