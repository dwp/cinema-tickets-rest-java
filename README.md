# Cinema Tickets Code Test

## Objective

This is a coding exercise which will allow you to demonstrate how you code and your approach to a given problem.

You will be assessed on:

- Your ability to write clean, well-tested and reusable code.
- How you have understood, interpreted the business rules and ensured they are correctly met.
- Identifying the ambiguity within the business rules and handle accordingly

### Business Rules

- There are 3 types of tickets i.e. INFANT, CHILD, and ADULT.
- The ticket prices are based on the type of ticket (see table below).
- The ticket purchaser declares how many and what type of tickets they want to buy.
- Multiple tickets can be purchased at any given time.
- Only a maximum of 25 tickets that can be purchased at a time.
- An INFANT does not pay for a ticket and are not allocated a seat. They will be sitting on an ADULT lap.
- CHILD and INFANT tickets cannot be purchased without purchasing an ADULT ticket.

| Ticket Type | Price  |
|-------------|--------|
| INFANT      | £0     |
| CHILD       | £17.50 |
| ADULT       | £25.99 |

- There is an existing `PaymentService` responsible for taking payments.
- There is an existing `SeatReservationService` responsible for reserving seats.

### Constraints

- The `CinemaTicketsService` interface MUST not be modified.
- The code in `uk.gov.dwp.engineering.recruitment.domain` package MUST not be modified with the exception of
  BookingConfirmation, which may be extended in a non-breaking manner if you choose to do so. ##TODO - shame as I could
  have added validation to the domain object

### Assumptions

You can assume:

- All accounts with an id greater than zero are valid. They also have sufficient funds to pay for any number of tickets.
- The `PaymentService` implementation is an external provider with no defects.
- You do not need to worry about how the actual payment happens or integrating that service.
- The payment will always go through once a payment request has been made to the `PaymentService`.
- The `SeatReservationService` implementation is an external provider with no defects.
- You do not need to worry about how the seat reservation algorithm works or integrating that service.
- The seats will always be reserved once a reservation request has been made to the `SeatReservationService`.
- Any assumptions outside of the list above should be correctly and thoroughly documented.

### Your Task

Provide a working implementation of `CinemaTicketsServiceImpl` that:

- Considers the above objective, business rules, constraints & assumptions.
- Calculates the correct amount for the requested tickets and makes a payment request to the `PaymentService`.
- Calculates the correct number of seats to reserve and makes a seat reservation request to the
  `SeatReservationService`.
- Rejects any invalid ticket purchase requests.
- It is up to you to identify what should be deemed as an invalid purchase request.
- Please ensure you record your approach and any assumptions you make in the `README.md` of your final solution and
  within your submitted repository.
- Ensure appropriate error handling and testing is implemented for the ‘CinemaTicketsController’ referring to all
  business rules and rejected ticket requests.

## Solution

### Assumptions

- A `TicketRequest` is only valid if the `TicketType` is not `null` and the `ticketCount` is greater than `0`.
- An ADULT cannot have more than one INFANT sitting on their lap.
- Multiple CHILD tickets can be purchased with one ADULT.
- Payment must be before reservation???

### Implementation

- Explain why not externalising prices
- SpotBugs identified that Booking.ticketRequests() exposes its internal array.