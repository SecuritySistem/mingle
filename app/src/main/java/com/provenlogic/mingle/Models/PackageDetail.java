package com.provenlogic.mingle.Models;

import java.io.Serializable;

/**
 * Created by amal on 22/03/17.
 */
public class PackageDetail implements Serializable{

    private String id;
    private String amount;
    private String credits;
    private String currency;
    private String packname_name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPackname_name() {
        return packname_name;
    }

    public void setPackname_name(String packname_name) {
        this.packname_name = packname_name;
    }
}
