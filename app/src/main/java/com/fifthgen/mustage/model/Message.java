package com.fifthgen.mustage.model;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * Created on 06/09/2017.
 * Copyright by oleg
 */

public class Message {

    private String id;
    private Long date;
    private String message;
    private String user_id;

    public Message() {
    }

    public Message(@NotNull String message, @NotNull String user) {
        this.message = message;
        this.user_id = user;
        this.date = new Date().getTime();
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
