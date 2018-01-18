package com.provenlogic.mingle.Models;

/**
 * Created by amal on 24/02/17.
 */
public class InterestInfo {
    private String interestId;
    private String interestName;
    private boolean isCommon;

    public InterestInfo() {
        isCommon = false;
    }

    public String getInterestId() {
        return interestId;
    }

    public void setInterestId(String interestId) {
        this.interestId = interestId;
    }

    public String getInterestName() {
        return interestName;
    }

    public void setInterestName(String interestName) {
        this.interestName = interestName;
    }

    public boolean isCommon() {
        return isCommon;
    }

    public void setCommon(boolean common) {
        isCommon = common;
    }
}
