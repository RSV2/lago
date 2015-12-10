package com.thirdchannel.rabbitmq.mock;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Steve Pember
 */
public class Widget {

    @JsonIgnore
    private int id = 1;

    private int count = 0;
    private boolean active = true;

    public Widget() {

    }

    public Widget(int count, boolean active, String name) {
        this.count = count;
        this.active = active;
        this.name = name;
    }

    private String name;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
