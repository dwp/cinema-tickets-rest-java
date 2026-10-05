package uk.gov.dwp.engineering.recruitment.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BookingExceptionTest {

  @Test
  void givenBookingException_whenGetMessage_thenExpectedMessageReturned() {
    var testException = new Exception("test exception");

    var underTest = new BookingException("test message", testException);

    assertEquals("test message", underTest.getMessage());
  }

  @Test
  void givenBookingException_whenGetCause_thenExpectedCauseReturned() {
    var testException = new Exception("test exception");

    var underTest = new BookingException("test message", testException);

    assertEquals(testException, underTest.getCause());
  }
}
