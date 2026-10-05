package uk.gov.dwp.engineering.recruitment;

import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import uk.gov.dwp.engineering.recruitment.domain.BookingConfirmation;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.BookingException;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

@Service
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

  private static final int MAX_TOTAL_TICKET_COUNT = 25;
  private static final BigDecimal ADULT_PRICE = new BigDecimal("25.99");
  private static final BigDecimal CHILD_PRICE = new BigDecimal("17.50");
  private static final BigDecimal INFANT_PRICE = new BigDecimal("0");

  private final PaymentService paymentService;
  private final SeatReservationService seatReservationService;

  public CinemaTicketsServiceImpl(
      PaymentService paymentService, SeatReservationService seatReservationService) {
    this.paymentService = paymentService;
    this.seatReservationService = seatReservationService;
  }

  @Override
  public BookingConfirmation purchaseTickets(
      final Long accountId, final TicketRequest... ticketRequests) throws InvalidBookingException {
    validatePurchaseTickets(accountId, ticketRequests);

    try {
      var paymentAmount = calculatePaymentAmount(ticketRequests);
      paymentService.debitAccount(accountId, paymentAmount);

      var seatCount = calculateSeatCount(ticketRequests);
      seatReservationService.reserveSeats(accountId, seatCount);

      return new BookingConfirmation(accountId, seatCount, paymentAmount);
    } catch (Exception e) {
      throw new BookingException("exception in purchaseTickets for accountId " + accountId, e);
    }
  }

  private void validatePurchaseTickets(final Long accountId, final TicketRequest... ticketRequests)
      throws InvalidBookingException {
    if (accountId <= 0) {
      throw new InvalidBookingException("accountId must be greater than zero");
    }

    if (ticketRequests.length == 0) {
      throw new InvalidBookingException("ticketRequests must be greater than zero");
    }

    var totalTicketCount = 0;
    var adultTicketCount = 0;
    var infantTicketCount = 0;
    for (var ticketRequest : ticketRequests) {
      validateTicketRequest(ticketRequest);

      totalTicketCount += ticketRequest.ticketCount();
      switch (ticketRequest.type()) {
        case ADULT -> adultTicketCount += ticketRequest.ticketCount();
        case INFANT -> infantTicketCount += ticketRequest.ticketCount();
        case CHILD -> {
          // child ticketCount is not required for validation logic
        }
      }
    }

    if (adultTicketCount <= 0) {
      throw new InvalidBookingException("adult ticketCount must be greater than zero");
    }

    if (totalTicketCount > MAX_TOTAL_TICKET_COUNT) {
      throw new InvalidBookingException("total ticketCount must be less than twenty six");
    }

    if (infantTicketCount > adultTicketCount) {
      throw new InvalidBookingException("infant ticketCount must not exceed adult ticketCount");
    }
  }

  private void validateTicketRequest(TicketRequest ticketRequest) {
    if (ticketRequest == null) {
      throw new InvalidBookingException("ticketRequest must not be null");
    }

    if (ticketRequest.type() == null) {
      throw new InvalidBookingException("ticketRequest type must not be null");
    }

    if (ticketRequest.ticketCount() <= 0) {
      throw new InvalidBookingException("ticketRequest ticketCount must be greater than zero");
    }
  }

  private BigDecimal calculatePaymentAmount(TicketRequest... ticketRequests) {
    var paymentAmount = BigDecimal.ZERO;
    for (var ticketRequest : ticketRequests) {
      switch (ticketRequest.type()) {
        case ADULT ->
            paymentAmount =
                paymentAmount.add(
                    ADULT_PRICE.multiply(BigDecimal.valueOf(ticketRequest.ticketCount())));
        case CHILD ->
            paymentAmount =
                paymentAmount.add(
                    CHILD_PRICE.multiply(BigDecimal.valueOf(ticketRequest.ticketCount())));
        case INFANT ->
            paymentAmount =
                paymentAmount.add(
                    INFANT_PRICE.multiply(
                        BigDecimal.valueOf(
                            ticketRequest
                                .ticketCount()))); // TODO - should this be replaced with a noop and
                                                   // comment - thining it's quicker to make a
                                                   // change by changing infant price, but perhaps
                                                   // not.
      }
    }

    return paymentAmount;
  }

  private Long calculateSeatCount(TicketRequest... ticketRequests) {
    return Arrays.stream(ticketRequests)
        .filter(t -> t.type() != TicketType.INFANT)
        .mapToLong(TicketRequest::ticketCount)
        .sum();
  }
}
