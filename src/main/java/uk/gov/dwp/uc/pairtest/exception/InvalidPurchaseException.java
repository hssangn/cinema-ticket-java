package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {

    //Account Related
    public static String EXCP_INVALID_ACCOUNT_ID = "INVALID ACCOUNT ID EXCEPTION";

    // Total num of ticket related
    public static String EXCP_EXCEED_PURCHASE_QUOTA = "EXCEED PURCHASE QUOTA LIMIT EXCEPTION";
    public static String EXCP_ZERO_TICKET = "NO TICKET REQUESTS EXCEPTION";

    // Num of Adult ticket related
    public static String EXCP_ZERO_ADULT = "ZERO ADULT TICKET EXCEPTION";
    public static String EXCP_INFANT_ADULT = "ADULT TICKET LESS THAN INFANT TICKET EXCEPTION";



    public InvalidPurchaseException(String errMsg) {
        super(errMsg);
    }

}
