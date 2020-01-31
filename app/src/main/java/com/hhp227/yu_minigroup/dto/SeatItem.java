package com.hhp227.yu_minigroup.dto;

public class SeatItem {
    public String id;
    public String name;
    public String count;
    public String occupied;
    public String percentageInteger;
    public String status;

    public SeatItem(String id, String name, String count, String occupied, String percentageInteger, String status) {
        this.id = id;
        this.name = name;
        this.count = count;
        this.occupied = occupied;
        this.percentageInteger = percentageInteger;
        this.status = status;
    }
}
