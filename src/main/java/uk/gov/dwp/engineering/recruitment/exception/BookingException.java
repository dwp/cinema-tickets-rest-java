package uk.gov.dwp.engineering.recruitment.exception;

import java.io.Serial;

public class BookingException extends RuntimeException {

  @Serial private static final long serialVersionUID = 1L;

  public BookingException(String message, Throwable cause) {
    super(message, cause);
  }
}
