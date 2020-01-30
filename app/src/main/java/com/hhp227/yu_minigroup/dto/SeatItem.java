package com.hhp227.yu_minigroup.dto;

public class SeatItem {
    public String name;
    public String total;
    public String residual;
    public String rate;
    public String status;

    public SeatItem(String name, String total, String residual, String rate, String status) {
        this.name = name;
        this.total = total;
        this.residual = residual;
        this.rate = rate;
        this.status = status;
    }
}
