
# Cinema-tickets

This is a code test for Software Engineer.
The program is written by Java 17, built by Maven 11 and tested by JUnit 4 and Mockito.
The business logic is implemented under TicketServiceImpl Class according to the business rules.



## Task

Part1 Validation
Validation Requirements:
1. A valid Account ID should be greater than 0
2. Only a maximum of 20 tickets that can be purchased at a time.
3. Child and Infant tickets cannot be purchased without purchasing an Adult ticket.
4. Number of adult should be greater than or equal to number of infant


Part2 Proceed to seat reservation
5. Calculate number of seat to reserve
6. Call 3rd party seat reservation service

Part3 Proceed to payment 
7. Caculate total amount to pay
8. Call 3rd party payment service


## Design Approach

The solution is implemented to aim at code-reuse, easy-modification of program parameters and easy supporting and debugging.
To Achieve these goals:
1. An Abstrat Layer "AbstractTicketService.class" is introduced to store program parameters

    - MAX_TICKET_QUOTA : The maximum number of ticket can buy in 1 purchase
    - MIN_ADULT_TICKET : The minimum number of Adult Ticket  
   
   Price for each Ticket Type is stored in TicketTypeRequest.class.
   

   In case of change/new business requirement, the system parameter can be easily updated here instead of updating the code.

2. "TicketType to Price Mapping" and "TicketType to NumOfTicket Mapping" are used to store ticket requests.
The benefit is that, in case of introducing a new TicketType, there is no need to update business logic.
For example, if a new TicketType - ELDERLY is introduced, the only thing needed to update is adding the Type - "ELDERLY" and Price to the TicketTypeRequest class
Limitation: if the new TicketType does not occupy a seat, then the logic of Seat Allacation has to be udpated accordingly, because the filter is done on INFANT only.

3. TicketTypeRequest.class is made final because it should be immutable 

4. Various of Exception Classes are introduced to extend from INVALID_PURCHASE_EXCEPTION to give a precise error message for each type of exception. It will help system monitoring and support and debugging.

5. Logging is done at every critical action for easy tracing of flow in case of application support on log level.



## Assumption

1. In the same purchase, the type in TicketTypeRequests can be dulplicated.
   For example, there can be 1 TicketTypeRequest for 5 Adult tickets and 1 TicketTypeRequest for 3 Adult ticket in the same purchase.
   This situation is handled in purchaseTickets().

2. At most 1 infant can sit on 1 adult's lap.
   Therefore, number of adult should be greater than or equal to number of infant
## Methods


purchaseTickets()
- This is the main logic method.
- This method processes a purchase request in 4 steps:

        1. Parse TicketTypeRequest to Map for easy handling
        2. Validation
        3. Call 3rd party service for Seat Reservation
        4. Call 3rd party service for Payment



ticketTypeRequestParser()
- This method parses the TicketTypeRequest and store in a Map


ticketServiceChecker()
- This method handles all the checking on business requirements
- It validates: 

        1. Account ID 
        2. Whether number of ticket exceed the limit
        3. Whether there is at least 1 Adult ticket 
        4. Whether number of adult ticket greater than or equal to number of infant ticket


calculateTotalNumOfTicket()
- This method calculates the total number of ticket, which is used for validation


calculateTotalSeatsToAllocate()
- This method calculates the total number of seat to allocate, which is used for seat Reservation


calculateTotalAmountToPay()
- this method calculates the total amount to pay, which is used for payment




## Testing

- JUnit and Mockito are both used for unit testing.
- Mockito Verify is used for assertion on number of time for a function being called.
- Both assertion for exception class and assertion for particular exception message are used for testing different exception cases accordingly.
- All Invalid Purchase cases are tested on expected exception
- Various of valid cases are tested to cover different kind of ticket type combination.


## Improvement / Enhancement
1. Turn ticketServiceChecker() into a checker class for validation purpose. It can keep the code in TicketService simplier and the validation can be reused by other modeules.

2. Handle Seat Reservation Failure and Payment Failure. In this project, Seat Reservation Service and Payment Service are called by simply calling their void method.
In case of payment failure, or seats reservation can not be done, we do not know.
If the 3rd party can provide a return to TicketService, we can handle these problem accordingly.


3. Turn it into a Spring Restful API webservice.
   Call 3rd party services SeatReservationService and TicketPaymentService through RESTful API

Pros:

    1. Decoupling of modules increase the flexibility of different services
    2. Smaller module increase product update lifecycle
    3. Easy to transform into microservices architecture


4. Deploy into docker container and then into kubernetes cluster 
For exmaple, use Jib-Maven-Plugin to build docker image for the TicketService Application.

Pros: 

    1. High Scalability
    2. High resilience
    3. Easy health checking
    4. Auto-rescale according to demand 
    5. Auto load balancing
