package com.example.bookingservice.service;

import com.example.bookingservice.dto.BookingDetailsDto;
import com.example.bookingservice.dto.SeatDto;
import com.example.bookingservice.exceptions.BadRequestException;
import com.example.bookingservice.model.Booking;
import com.example.bookingservice.model.BookingDetail;
import com.example.bookingservice.model.Seat;
import com.example.bookingservice.repository.BookingDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class BookingDetailService {
    private final BookingDetailRepository bookingDetailRepository;
    private final SeatService seatService;

    public List<BookingDetail> getAllBookingDetails() {
        try {
            return bookingDetailRepository.findAll();
        } catch (Exception e) {
            throw new BadRequestException("Error while fetching all booking details");
        }
    }

    public Long addBookingDetail(BookingDetailsDto bookingDetailsDto) {
        try {
            if (bookingDetailsDto.getNumberOfSeats() <= 0)
                throw new BadRequestException("Number of seats cannot be less than or equal to 0");
            List<SeatDto> seatDto = bookingDetailsDto.getSeat();
            log.info("SeatDto: {}", seatDto);
            List<Seat> seats = new ArrayList<>();
            for (SeatDto seat : seatDto) {
                Seat seatBySeatNumber = seatService.getSeatBySeatNumber(seat.getSeatNumber());
                log.info("Seat by seat number: {}", seatBySeatNumber);
                if (Boolean.TRUE.equals(seatBySeatNumber.getIsBooked())) {
                    throw new BadRequestException("Seat " + seatBySeatNumber.getId().toString() + " is already booked");
                }
                seats.add(seatBySeatNumber);
                seatService.updateIsBooked(seatBySeatNumber.getId(), true);
            }
            log.info("Seats: {}", seats);
            BookingDetail bookingDetail = BookingDetail.builder()
                    .seat(seats)
                    .numberOfSeats(bookingDetailsDto.getNumberOfSeats())
                    .build();
            BookingDetail bd = bookingDetailRepository.saveAndFlush(bookingDetail);
            return bd.getId();
        } catch (Exception e) {
            log.error("Error while adding booking detail: {}", e.getMessage());
            throw new BadRequestException("Error while adding booking detail");
        }
    }

    public void setBookingInBookingDetail(Booking booking, Long bookingDetailId) {
        BookingDetail bookingDetail = bookingDetailRepository.findById(bookingDetailId)
                .orElseThrow(()
                        -> new BadRequestException("Booking detail not found"));
        bookingDetail.setBooking(booking);
        bookingDetailRepository.save(bookingDetail);
    }

    public BookingDetail getById(Long id) {
        return bookingDetailRepository.findById(id)
                .orElseThrow(()
                        -> new BadRequestException("Booking detail not found"));
    }

    public String deleteBookingDetail(Long id) {
        try {
            BookingDetail bookingDetail = bookingDetailRepository.findById(id)
                    .orElseThrow(()
                            -> new BadRequestException("No such booking found"));
            List<Seat> seats = bookingDetail.getSeat();
            seats.forEach(seat -> seat.setIsBooked(false));
            bookingDetailRepository.deleteById(id);
        } catch (Exception e) {
            throw new BadRequestException("Error while deleting booking detail");
        }
        return "deleted successfully";
    }
}
