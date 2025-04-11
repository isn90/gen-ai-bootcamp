package com.epam.training.gen.ai.plugins;

import com.epam.training.gen.ai.model.BookingTypes;
import com.epam.training.gen.ai.service.BookingService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class BookTicketsPlugin {

    private final BookingService bookingService;

    public BookTicketsPlugin(BookingService bookingService) {
        this.bookingService = bookingService;
        fetchBookingTypes();        //initializing booking type
    }

    @DefineKernelFunction(name = "allowedBookingTypes", description = "Fetch booking types allowed by the system")
    public List<BookingTypes> fetchBookingTypes() {
        log.info("Fetching booking types");
        return bookingService.fetchBookingTypes();
    }
}
