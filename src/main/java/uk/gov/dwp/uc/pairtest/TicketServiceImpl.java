package uk.gov.dwp.uc.pairtest;

import lombok.extern.slf4j.Slf4j;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountIdException;
import uk.gov.dwp.uc.pairtest.exception.InvalidNumOfAdultTicketException;
import uk.gov.dwp.uc.pairtest.exception.InvalidNumOfTicket;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException.*;

@Slf4j
public class TicketServiceImpl extends AbstractTicketService {
    /**
     * Should only have private methods other than the one below.
     */

    private final TicketPaymentService ticketPaymentService;
    private final SeatReservationService seatReservationService;

    Map<TicketTypeRequest.Type, Integer> ticketTypeCountMap;

    public TicketServiceImpl(TicketPaymentService ticketPaymentService,
                              SeatReservationService seatReservationService) {
        this.ticketPaymentService = ticketPaymentService;
        this.seatReservationService = seatReservationService;

    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        ticketTypeCountMap = new HashMap<>();

        // Step 1. Parse TicketTypeRequest
        log.info("Parse TicketTypeRequest...");
        ticketTypeRequestParser(accountId, ticketTypeRequests);


        // Step 2. Validation
        // Only proceed if passed validation
        try {
            ticketServiceChecker(accountId, ticketTypeRequests);

            // Step 3. Calculate Total Seat to Reserve and Call totalSeatsToAllocate
            int totalSeatsToAllocate = calculateTotalSeatsToAllocate(ticketTypeRequests);
            log.info("Proceed to seat reservation");
            seatReservationService.reserveSeat(accountId, totalSeatsToAllocate);
            log.info((new StringBuilder()
                    .append("Reserved ")
                    .append(totalSeatsToAllocate)
                    .append(" seat(s) for Account ID: ")
                    .append(accountId).toString()));

            // Step 4. Calculate Total Amount to Pay and Call Payment Service
            int totalAmountToPay = calculateTotalAmountToPay();
            log.info("Proceed to payment");
            ticketPaymentService.makePayment(accountId, totalAmountToPay);
            log.info((new StringBuilder()
                    .append("Account ID: ")
                    .append(accountId)
                    .append(" paid £")
                    .append(totalAmountToPay).toString()));
        }catch (InvalidPurchaseException e){
            log.error(e.getMessage());
            throw e;
        }
    }

    // Parse the TicketRequests to a map of TicketType:NumOfTicket
    private void ticketTypeRequestParser(Long accountId, TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests.length == 0) {
            throw new InvalidNumOfTicket(EXCP_ZERO_TICKET);
        }

        for (TicketTypeRequest ticketTypeRequest : ticketTypeRequests) {
            ticketTypeCountMap.computeIfPresent(ticketTypeRequest.getTicketType(), (type, tickets) -> tickets + ticketTypeRequest.getNoOfTickets());
            ticketTypeCountMap.putIfAbsent(ticketTypeRequest.getTicketType(), ticketTypeRequest.getNoOfTickets());
        }
        log.info((new StringBuilder()
                        .append("Account ID: ")
                        .append(accountId)
                        .append(" purchase request: ")
                        .append(ticketTypeCountMap.toString()).toString()));
    }


    // Perform Purchase Validation
    private void ticketServiceChecker(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException{
        log.info("Checking TicketTypeRequests...");

        // Check 1.  Validate AccountId
        if (accountId <= 0){
            //throw new InvalidAccountIdException(InvalidPurchaseException.INVALID_ACCOUNT_ID);
            throw new InvalidAccountIdException(EXCP_INVALID_ACCOUNT_ID, accountId);
        }

        // Check 2. Total num of purchase ticket should be less than Quota
        if (calculateTotalNumOfTicket(ticketTypeRequests) > MAX_TICKET_QUOTA){
            //throw new InvalidPurchaseException(InvalidPurchaseException.EXCP_EXCEED_PURCHASE_QUOTA);
            throw new InvalidNumOfTicket(EXCP_EXCEED_PURCHASE_QUOTA, MAX_TICKET_QUOTA);
        }

        // Check 3. Num of Adult Ticket should be greater than Minimum Restriction
        if (Optional.ofNullable(ticketTypeCountMap.get(TicketTypeRequest.Type.ADULT)).orElse(0) < MIN_ADULT_TICKET){
            throw new InvalidNumOfAdultTicketException(EXCP_ZERO_ADULT);
        }
        // Check 4. Num of Adult Ticket should be greater than or equal to Num of Infant Ticket
        if (Optional.ofNullable(ticketTypeCountMap.get(TicketTypeRequest.Type.ADULT)).orElse(0) < Optional.ofNullable(ticketTypeCountMap.get(TicketTypeRequest.Type.INFANT)).orElse(0)){
            throw new InvalidNumOfAdultTicketException(EXCP_INFANT_ADULT);
        }
    }


    // Calculate the total number of ticket
    private int calculateTotalNumOfTicket(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    // Calculate the total number of seats to allocate
    private int calculateTotalSeatsToAllocate(TicketTypeRequest... ticketTypeRequests) {
        return Arrays.stream(ticketTypeRequests)
                .filter(ticketTypeRequest -> !TicketTypeRequest.Type.INFANT.equals(ticketTypeRequest.getTicketType()))
                .mapToInt(TicketTypeRequest::getNoOfTickets)
                .sum();
    }

    // Calculate the total amount to pay
    private int calculateTotalAmountToPay() {
        return ticketTypeCountMap.entrySet().stream()
                .mapToInt(ticketTypeCount -> {
                    int numTickets = ticketTypeCount.getValue();
                    int price = ticketTypeCount.getKey().getPrice();
                    return numTickets * price;
                }).sum();
    }


}
