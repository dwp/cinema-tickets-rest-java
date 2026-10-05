package uk.gov.dwp.engineering.recruitment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class BookingConfirmationTest {

  private static final Long ACCOUNT_ID = 1L;
  private static final Long SEAT_COUNT = 3L;
  private static final BigDecimal TOTAL_COST = new BigDecimal("77.97");

  private final BookingConfirmation underTest =
      new BookingConfirmation(ACCOUNT_ID, SEAT_COUNT, TOTAL_COST);

  @Test
  void givenBookingConfirmation_whenAccountId_thenExpectedValueReturned() {
    var actual = underTest.accountId();

    assertEquals(ACCOUNT_ID, actual);
  }

  @Test
  void givenBookingConfirmation_whenSeatCount_thenExpectedValueReturned() {
    var actual = underTest.seatCount();

    assertEquals(SEAT_COUNT, actual);
  }

  @Test
  void givenBookingConfirmation_whenTotalCost_thenExpectedValueReturned() {
    var actual = underTest.totalCost();

    assertEquals(TOTAL_COST, actual);
  }
}
