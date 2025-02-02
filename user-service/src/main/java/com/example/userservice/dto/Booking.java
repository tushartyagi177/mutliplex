package com.example.userservice.dto;

import com.example.userservice.model.Show;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Booking {

    private long bookingId;
    private Show shows;
    private Date bookedDate;
    private Date showDate;
}
