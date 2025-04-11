package com.epam.training.gen.ai.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum BookingTypes {

    FLIGHT("Flight"),
    TRAIN("Train"),
    MOVIE("Movie"),
    CONCERT("Concert"),
    HOTEL("Hotel");

    private final String bookingName;

}
