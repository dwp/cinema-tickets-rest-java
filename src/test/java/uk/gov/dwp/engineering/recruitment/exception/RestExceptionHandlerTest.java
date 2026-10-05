package uk.gov.dwp.engineering.recruitment.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class RestExceptionHandlerTest {

  private final RestExceptionHandler underTest = new RestExceptionHandler();

  @Test
  void
      givenInvalidBookingException_whenHandleInvalidBookingException_thenReturnsBadRequestWithExceptionMessage() {
    var testException = new InvalidBookingException("test exception");

    var actual = underTest.handleInvalidBookingException(testException);

    assertEquals(HttpStatus.BAD_REQUEST.value(), actual.getStatus());
    assertEquals("test exception", actual.getDetail());
  }

  @Test
  void
      givenBookingException_whenHandleBookingException_thenReturnsInternalServerErrorWithExceptionMessage() {
    var testThrowable = new RuntimeException("test runtime exception");
    var testException = new BookingException("test exception", testThrowable);

    var actual = underTest.handleBookingException(testException);

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), actual.getStatus());
    assertEquals("test exception", actual.getDetail());
  }
}
