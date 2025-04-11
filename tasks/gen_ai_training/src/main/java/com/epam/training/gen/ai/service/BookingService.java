package com.epam.training.gen.ai.service;

import com.epam.training.gen.ai.model.BookingTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BookingService {

    //private static final List<BookingTypes> bookingTypes = List.of()

    public List<BookingTypes> fetchBookingTypes() {
        return List.of(BookingTypes.values());
    }

}
