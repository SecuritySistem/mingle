package com.provenlogic.mingle.Models;

/**
 * Created by amal on 20/01/17.
 */
public class userDetail {
    private String id;
    private String name;
    private String picture;
    private String age;
    private String description;
    private String distance;
    private boolean should_show;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public boolean isShould_show() {
        return should_show;
    }

    public void setShould_show(boolean should_show) {
        this.should_show = should_show;
    }
}
