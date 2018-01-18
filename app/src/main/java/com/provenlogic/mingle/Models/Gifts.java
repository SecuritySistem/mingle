package com.provenlogic.mingle.Models;

/**
 * Created by Anurag on 4/6/2017.
 */

public class Gifts {

    //Gift id
    private String id;

    //Gift name
    private String name;

    //Gift icon name
    private  String icon_name;

    //Url for the gift
    private String url;

    //Gift price
    int price;

    /**
     *
     * @param id
     * @param name
     * @param icon_name
     * @param url
     * @param price
     */
    public Gifts(String id, String name, String icon_name, String url, int price){
        this.id = id;
        this.name = name;
        this.icon_name = icon_name;
        this.url = url;
        this.price = price;
    }

    public String getId(){
        return this.id;
    }

    public String getName(){
        return this.name;
    }

    public String getIconName(){
        return this.icon_name;
    }
    public String getUrl(){
        return this.url;
    }

    public int getPrice(){
        return this.price;
    }
}
