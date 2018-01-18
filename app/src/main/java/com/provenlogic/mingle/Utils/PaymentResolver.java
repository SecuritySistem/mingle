package com.provenlogic.mingle.Utils;

/**
 * Created by Anurag on 4/7/2017.
 */

public class PaymentResolver {
    //used to identify the mode of payment choosen.
    public enum PAYMENT_MODE {PAYPAL, STRIPE, NONE};

    //Current payment mode opted by user.
    private static PAYMENT_MODE MODE;

    //Amount selected for payment.
    private static String amount;

    //Currency of the amount.
    private static String currency;

    //Package id selected for the payment.
    private static String package_id;

    /**
     * Sets the payment mode.
     * @param NEWMODE payment mode
     */
    public static void setPaymentMode(PAYMENT_MODE NEWMODE){
        MODE = NEWMODE;
    }

    /**
     * Sets the amount of payment
     * @param Amount
     */
    public static void setAmount(String Amount){
        amount = Amount;
    }

    /**
     * Sets the currency.
     * @param Currency
     */
    public static void setCurrency(String Currency){
        currency = Currency;
    }

    /**
     * Returns the current payment mode.
     * @return
     */
    public static PAYMENT_MODE getPaymentMode(){
        return MODE;
    }

    /**
     * Returns the current amount.
     * @return
     */
    public static String getAmount(){
        return amount;
    }

    /**
     * Returns the currency value.
     * @return
     */
    public static String getCurrency(){
        return currency;
    }

    /**
     * Sets the package id of the payment
     * @param Package
     */
    public static void setPackageId(String Package){
        package_id = Package;
    }

    /**
     * Returns the package id of the current payment
     * @return
     */
    public static String getPackageId(){
        return package_id;
    }

}
