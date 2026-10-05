package uk.gov.dwp.engineering.recruitment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.exception.BookingException;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

class CinemaTicketsServiceImplTest {

  private static final Long ACCOUNT_ID = 1L;

  private final PaymentService paymentService = mock(PaymentService.class);
  private final SeatReservationService seatReservationService = mock(SeatReservationService.class);

  private final CinemaTicketsServiceImpl underTest =
      new CinemaTicketsServiceImpl(paymentService, seatReservationService);

  @Test
  void givenInvalidAccountId_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(0L, new TicketRequest[] {}));

    assertEquals("accountId must be greater than zero", actual.getMessage());
  }

  @Test
  void givenZeroTicketRequests_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(ACCOUNT_ID, new TicketRequest[] {}));

    assertEquals("ticketRequests must be greater than zero", actual.getMessage());
  }

  @Test
  void
      givenTicketRequestsWithNullTicketRequest_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var ticketRequests = new TicketRequest[] {null};

    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("ticketRequest must not be null", actual.getMessage());
  }

  @Test
  void
      givenTicketRequestsWithATicketRequestWithTypeNull_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var ticketRequests =
        new TicketRequest[] {new TicketRequest(TicketType.ADULT, 1), new TicketRequest(null, 1)};

    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("ticketRequest type must not be null", actual.getMessage());
  }

  @Test
  void
      givenTicketRequestsWithATicketRequestWithTicketCountZero_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var ticketRequests =
        new TicketRequest[] {
          new TicketRequest(TicketType.ADULT, 1), new TicketRequest(TicketType.CHILD, 0)
        };

    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("ticketRequest ticketCount must be greater than zero", actual.getMessage());
  }

  @Test
  void givenZeroAdultTicketCount_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.CHILD, 1)};

    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("adult ticketCount must be greater than zero", actual.getMessage());
  }

  @Test
  void
      givenTotalTicketCountIsGreaterThanTwentyFive_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.ADULT, 26)};

    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("total ticketCount must be less than twenty six", actual.getMessage());
  }

  @Test
  void
      givenInfantTicketCountExceedsAdultTicketCount_whenPurchaseTickets_thenInvalidBookingExceptionIsThrown() {
    var ticketRequests =
        new TicketRequest[] {
          new TicketRequest(TicketType.ADULT, 1), new TicketRequest(TicketType.INFANT, 2)
        };

    var actual =
        assertThrows(
            InvalidBookingException.class,
            () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("infant ticketCount must not exceed adult ticketCount", actual.getMessage());
  }

  @ParameterizedTest
  @MethodSource("givenValidTicketRequests")
  void
      givenValidTicketRequests_whenPurchaseTickets_thenPaymentServiceDebitAccountIsInvokedWithAccountIdAndAmount(
          BigDecimal expectedPaymentAmount, TicketRequest[] ticketRequests) {
    underTest.purchaseTickets(ACCOUNT_ID, ticketRequests);

    verify(paymentService).debitAccount(ACCOUNT_ID, expectedPaymentAmount);
  }

  @Test
  void givenAdultAndInfantTickets_whenPurchaseTickets_thenInfantTicketsAreNotCharged() {
    var ticketRequests =
        new TicketRequest[] {
          new TicketRequest(TicketType.ADULT, 1), new TicketRequest(TicketType.INFANT, 1)
        };

    underTest.purchaseTickets(ACCOUNT_ID, ticketRequests);

    verify(paymentService).debitAccount(ACCOUNT_ID, new BigDecimal("25.99"));
  }

  @Test
  void
      givenValidTicketRequests_whenPurchaseTickets_thenSeatReservationServiceReserveSeatsIsInvokedWithAccountIdAndSeatCount() {
    var ticketRequests =
        new TicketRequest[] {
          new TicketRequest(TicketType.ADULT, 3), new TicketRequest(TicketType.CHILD, 2)
        };

    underTest.purchaseTickets(ACCOUNT_ID, ticketRequests);

    verify(seatReservationService).reserveSeats(ACCOUNT_ID, 5L);
  }

  @Test
  void givenAdultAndInfantTickets_whenPurchaseTickets_thenSeatReservationExcludesInfantTickets() {
    var ticketRequests =
        new TicketRequest[] {
          new TicketRequest(TicketType.ADULT, 3), new TicketRequest(TicketType.INFANT, 3)
        };

    underTest.purchaseTickets(ACCOUNT_ID, ticketRequests);

    verify(seatReservationService).reserveSeats(ACCOUNT_ID, 3L);
  }

  @Test
  void
      givenValidTicketRequests_whenPurchaseTickets_thenPaymentServiceIsInvokedBeforeSeatReservationService() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.ADULT, 1)};

    underTest.purchaseTickets(ACCOUNT_ID, ticketRequests);

    var inOrder = inOrder(paymentService, seatReservationService);
    inOrder.verify(paymentService).debitAccount(ACCOUNT_ID, new BigDecimal("25.99"));
    inOrder.verify(seatReservationService).reserveSeats(ACCOUNT_ID, 1L);
  }

  @Test
  void givenValidTicketRequestsWithTwentyFiveTickets_whenPurchaseTickets_thenBookingIsSuccessful() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.ADULT, 25)};

    underTest.purchaseTickets(ACCOUNT_ID, ticketRequests);

    verify(paymentService).debitAccount(ACCOUNT_ID, new BigDecimal("649.75"));
    verify(seatReservationService).reserveSeats(ACCOUNT_ID, 25L);
  }

  @Test
  void
      givenValidTicketRequests_whenPurchaseTickets_thenReturnsBookingConfirmationWithAccountIdSeatCountAndPaymentAmount() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.ADULT, 1)};

    var actual = underTest.purchaseTickets(ACCOUNT_ID, ticketRequests);

    assertEquals(ACCOUNT_ID, actual.accountId());
    assertEquals(1L, actual.seatCount());
    assertEquals(new BigDecimal("25.99"), actual.totalCost());
  }

  @Test
  void
      givenValidTicketRequests_whenPurchaseTicketsPaymentServiceThrowsException_ThenBookingExceptionIsThrown() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.ADULT, 1)};
    var testException = new RuntimeException("test exception");

    when(paymentService.debitAccount(anyLong(), any(BigDecimal.class))).thenThrow(testException);

    var actual =
        assertThrows(
            BookingException.class, () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("exception in purchaseTickets for accountId " + ACCOUNT_ID, actual.getMessage());
    assertEquals(testException, actual.getCause());
  }

  @Test
  void
      givenValidTicketRequests_whenPurchaseTicketsPaymentServiceThrowsException_thenSeatReservationIsNotInvoked() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.ADULT, 1)};
    var testException = new RuntimeException("test exception");

    when(paymentService.debitAccount(anyLong(), any(BigDecimal.class))).thenThrow(testException);

    assertThrows(
        BookingException.class, () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    verifyNoInteractions(seatReservationService);
  }

  @Test
  void
      givenValidTicketRequests_whenPurchaseTicketsSeatReservationServiceThrowsException_ThenBookingExceptionIsThrown() {
    var ticketRequests = new TicketRequest[] {new TicketRequest(TicketType.ADULT, 1)};
    var testException = new RuntimeException("test exception");

    when(seatReservationService.reserveSeats(anyLong(), anyLong())).thenThrow(testException);

    var actual =
        assertThrows(
            BookingException.class, () -> underTest.purchaseTickets(ACCOUNT_ID, ticketRequests));

    assertEquals("exception in purchaseTickets for accountId " + ACCOUNT_ID, actual.getMessage());
    assertEquals(testException, actual.getCause());
  }

  private static Stream<Arguments> givenValidTicketRequests() {
    return Stream.of(
        Arguments.of(
            new BigDecimal("25.99"), new TicketRequest[] {new TicketRequest(TicketType.ADULT, 1)}),
        Arguments.of(
            new BigDecimal("77.97"), new TicketRequest[] {new TicketRequest(TicketType.ADULT, 3)}),
        Arguments.of(
            new BigDecimal("78.49"),
            new TicketRequest[] {
              new TicketRequest(TicketType.ADULT, 1), new TicketRequest(TicketType.CHILD, 3)
            }),
        Arguments.of(
            new BigDecimal("51.98"),
            new TicketRequest[] {
              new TicketRequest(TicketType.ADULT, 2), new TicketRequest(TicketType.INFANT, 1)
            }),
        Arguments.of(
            new BigDecimal("112.97"),
            new TicketRequest[] {
              new TicketRequest(TicketType.ADULT, 3),
              new TicketRequest(TicketType.CHILD, 2),
              new TicketRequest(TicketType.INFANT, 2)
            }));
  }
}
