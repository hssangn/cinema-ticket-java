package uk.gov.dwp.uc.pairtest.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidNumOfTicket extends InvalidPurchaseException{

    public InvalidNumOfTicket(String errMsg) {
        super(errMsg);
        log.info(errMsg);
    }

    public InvalidNumOfTicket(String errMsg, int quota) {
        super(errMsg + " Total number of ticket should be less than " + quota);
        log.info(errMsg + " Total number of ticket should be less than " + quota);
    }

}
