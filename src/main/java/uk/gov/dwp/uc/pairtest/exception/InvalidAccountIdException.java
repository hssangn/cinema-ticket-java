package uk.gov.dwp.uc.pairtest.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvalidAccountIdException extends InvalidPurchaseException{

    public InvalidAccountIdException(String errMsg){
        super(errMsg);
        log.info(errMsg);
    }
    public InvalidAccountIdException(String errMsg, Long AccountId){
        super(errMsg);
        log.info(errMsg + " : " + AccountId.toString());
    }
}
