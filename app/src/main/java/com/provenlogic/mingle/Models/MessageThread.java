package com.provenlogic.mingle.Models;

import org.joda.time.DateTime;

/**
 * Created by amal on 21/12/16.
 */
public class MessageThread {
    private String Id;
    private String userId;
    private String lastMessage;
    private DateTime lastSent;
    private String name;
    private String picture;
    private String contactId;


    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public DateTime getLastSent() {
        return lastSent;
    }

    public void setLastSent(DateTime lastSent) {
        this.lastSent = lastSent;
    }


    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
}
