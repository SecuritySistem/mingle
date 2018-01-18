package com.provenlogic.mingle.Models;

import android.graphics.Bitmap;

/**
 * Created by himanshusoni on 06/09/15.
 */
public class ChatMessage {
    private boolean isImage, isMine, isGift;
    private String content;

    //Url of the gift
    private String gift_url;

    private  String gift_id;

    private String image_url;

    private Bitmap image;

    public ChatMessage(String message, boolean mine, boolean image, boolean gift) {
        content = message;
        isMine = mine;
        isGift = gift;
        isImage = image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setIsMine(boolean isMine) {
        this.isMine = isMine;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setIsImage(boolean isImage) {
        this.isImage = isImage;
    }

    /**
     *
     * @return
     */
    public boolean isGift(){
        return isGift;
    }

    /**
     *
     * @return the url if the current item is gift type else null
     */
    public String getGiftUrl(){
        if(isGift){
            return gift_url;
        }
        return null;
    }

    /**
     * This function sets the url of the gift
     * @param giftUrl of the gift.
     */
    public void setGiftUrl(String giftUrl){
        if(isGift){
            this.gift_url = giftUrl;
        }
    }

    /**
     * Sets the gift id.
     * @param id
     */
    public void setGiftId(String id){
        if(isGift){
            this.gift_id = id;
        }
    }

    /**
     * Returns the gift id
     * @return
     */
    public String getGiftId(){
        if(isGift){
            return this.gift_id;
        }
        return null;
    }

    /**
     *
     * @return
     */
    public String getImageUrl(){
        if(isImage){
            return this.image_url;
        }
        return null;
    }

    /**
     *
     * @param url
     */
    public void setImageUrl(String url){
        if(isImage){
            this.image_url = url;
        }
    }

    /**
     *
     * @return
     */
    public Bitmap getImageBitmap(){
        if(isImage){
            return this.image;
        }
        return null;
    }

    /**
     *
     * @param newImage
     */
    public void setImageBitmao(Bitmap newImage){
        if(isImage){
            this.image = newImage;
        }
    }
}
