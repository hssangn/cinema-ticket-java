import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountIdException;
import uk.gov.dwp.uc.pairtest.exception.InvalidNumOfAdultTicketException;
import uk.gov.dwp.uc.pairtest.exception.InvalidNumOfTicket;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceTest {
    private TicketServiceImpl ticketServiceTester;

    @Mock
    private SeatReservationServiceImpl seatReservationServiceMock;

    @Mock
    TicketPaymentServiceImpl ticketPaymentServiceMock;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Before
    public void testerSetUp(){
        ticketServiceTester = new TicketServiceImpl(ticketPaymentServiceMock, seatReservationServiceMock);
    }


    // Part 1 : test on valid test cases
    @Test
    public void testPurchaseTicketsValid() {
        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        };

        Long accountId = 1001L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(1)).reserveSeat(accountId, 15);
        verify(ticketPaymentServiceMock, times(1)).makePayment(accountId, 250);
    }

    @Test
    public void testPurchaseTicketsValidMULTIPLE() {
        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 3),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        Long accountId = 1001L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(1)).reserveSeat(accountId, 8);
        verify(ticketPaymentServiceMock, times(1)).makePayment(accountId, 130);
    }

    @Test
    public void testPurchaseTicketsValidNoCHILD() {
        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2)
        };

        Long accountId = 1002L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(1)).reserveSeat(accountId, 10);
        verify(ticketPaymentServiceMock, times(1)).makePayment(accountId, 200);
    }

    @Test
    public void testPurchaseTicketsValidNoINFANT() {
        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5)
        };

        Long accountId = 1003L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(1)).reserveSeat(accountId, 15);
        verify(ticketPaymentServiceMock, times(1)).makePayment(accountId, 250);
    }

    @Test
    public void testPurchaseTicketsValidNoChildENoInfant() {
        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10)
        };

        Long accountId = 1004L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(1)).reserveSeat(accountId, 10);
        verify(ticketPaymentServiceMock, times(1)).makePayment(accountId, 200);
    }


    // Part 2 : test on invalid test cases

    @Test(expected = InvalidAccountIdException.class)
    public void testPurchaseTicketsInvalidAccountId() {

        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 6),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 3)

        };

        Long accountId = -1005L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(0)).reserveSeat(accountId, 7);
        verify(ticketPaymentServiceMock, times(0)).makePayment(accountId, 130);
    }

    @Test
    public void testPurchaseTicketsInvalidEmptyRequest() {

        exceptionRule.expect(InvalidNumOfTicket.class);
        exceptionRule.expectMessage(InvalidPurchaseException.EXCP_ZERO_TICKET);

        TicketTypeRequest[] trList = new TicketTypeRequest[]{};

        Long accountId = 1006L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(0)).reserveSeat(accountId, 0);
        verify(ticketPaymentServiceMock, times(0)).makePayment(accountId, 0);
    }

    @Test
    public void testPurchaseTicketsInvalidNoAdult() {

        exceptionRule.expect(InvalidNumOfAdultTicketException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.EXCP_ZERO_ADULT);

        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2)
        };

        Long accountId = 1007L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(0)).reserveSeat(accountId, 0);
        verify(ticketPaymentServiceMock, times(0)).makePayment(accountId, 0);
    }

    @Test
    public void testPurchaseTicketsInvalidAdultInftant() {

        exceptionRule.expect(InvalidNumOfAdultTicketException.class);
        exceptionRule.expectMessage(InvalidPurchaseException.EXCP_INFANT_ADULT);

        TicketTypeRequest[] trList = new TicketTypeRequest[]{
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 7),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 10)
        };

        Long accountId = 1008L;
        ticketServiceTester.purchaseTickets(accountId, trList);
        verify(seatReservationServiceMock, times(0)).reserveSeat(accountId, 0);
        verify(ticketPaymentServiceMock, times(0)).makePayment(accountId, 0);
    }





}
