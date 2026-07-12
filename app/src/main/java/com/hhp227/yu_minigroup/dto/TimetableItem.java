package com.hhp227.yu_minigroup.dto;

public class TimetableItem {
    public int id;

    public String subject;

    public String classroom;

    public TimetableItem(int id, String subject, String classroom) {
        this.id = id;
        this.subject = subject;
        this.classroom = classroom;
    }
}
