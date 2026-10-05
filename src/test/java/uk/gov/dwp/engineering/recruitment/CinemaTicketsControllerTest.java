package uk.gov.dwp.engineering.recruitment;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import uk.gov.dwp.engineering.recruitment.domain.Booking;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.BookingException;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;

@WebMvcTest(CinemaTicketsController.class)
class CinemaTicketsControllerTest {

  private static final Long ACCOUNT_ID = 1L;
  private static final Long SEAT_COUNT = 3L;
  private static final BigDecimal TOTAL_COST = new BigDecimal("77.97");

  @Autowired private ObjectMapper objectMapper;
  @Autowired private MockMvc mockMvc;
  @MockitoBean private CinemaTicketsService cinemaTicketsService;

  @Test
  void givenValidBooking_whenMakeBooking_ThenCreatedResponseIsReturned() throws Exception {
    var ticketRequest = new TicketRequest(TicketType.ADULT, SEAT_COUNT.intValue());
    var booking = new Booking(ACCOUNT_ID, ticketRequest);
    var bookingConfirmation = new BookingConfirmation(ACCOUNT_ID, SEAT_COUNT, TOTAL_COST);

    when(cinemaTicketsService.purchaseTickets(ACCOUNT_ID, ticketRequest))
        .thenReturn(bookingConfirmation);

    mockMvc
        .perform(
            post("/cinema/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(booking)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accountId").value(ACCOUNT_ID))
        .andExpect(jsonPath("$.seatCount").value(SEAT_COUNT))
        .andExpect(jsonPath("$.totalCost").value(TOTAL_COST.doubleValue()));

    verify(cinemaTicketsService).purchaseTickets(ACCOUNT_ID, ticketRequest);
  }

  @Test
  void givenInvalidBooking_whenMakeBooking_ThenBadRequestResponseIsReturned() throws Exception {
    var ticketRequest = new TicketRequest(TicketType.ADULT, 0);
    var booking = new Booking(ACCOUNT_ID, ticketRequest);
    var invalidBookingException =
        new InvalidBookingException("adult ticketCount must be greater than zero");

    when(cinemaTicketsService.purchaseTickets(ACCOUNT_ID, ticketRequest))
        .thenThrow(invalidBookingException);

    mockMvc
        .perform(
            post("/cinema/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(booking)))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value(invalidBookingException.getMessage()))
        .andExpect(jsonPath("$.instance").value("/cinema/bookings"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.title").value("Bad Request"));

    verify(cinemaTicketsService).purchaseTickets(ACCOUNT_ID, ticketRequest);
  }

  @Test
  void givenPaymentServiceException_whenMakeBooking_ThenInternalServerErrorResponseIsReturned()
      throws Exception {
    var ticketRequest = new TicketRequest(TicketType.ADULT, SEAT_COUNT.intValue());
    var booking = new Booking(ACCOUNT_ID, ticketRequest);
    var paymentServiceException = new RuntimeException("payment service exception");
    var bookingException =
        new BookingException(
            "exception in purchaseTickets for accountId " + ACCOUNT_ID, paymentServiceException);

    when(cinemaTicketsService.purchaseTickets(ACCOUNT_ID, ticketRequest))
        .thenThrow(bookingException);

    mockMvc
        .perform(
            post("/cinema/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(booking)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.detail").value(bookingException.getMessage()))
        .andExpect(jsonPath("$.instance").value("/cinema/bookings"))
        .andExpect(jsonPath("$.status").value(500))
        .andExpect(jsonPath("$.title").value("Internal Server Error"));

    verify(cinemaTicketsService).purchaseTickets(ACCOUNT_ID, ticketRequest);
  }
}
