package uk.gov.dwp.engineering.recruitment.exception;

import java.io.Serial;

public class InvalidBookingException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public InvalidBookingException(String message) {
    super(message);
  }
}
