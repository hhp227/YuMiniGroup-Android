package com.hhp227.yu_minigroup.dto;

public class MemberItem {
    public String uid, name, value, stuNum, dept, div, regDate;

    public MemberItem(String uid, String name, String value) {
        this.uid = uid;
        this.name = name;
        this.value = value;
    }

    public MemberItem(String uid, String name, String value, String stuNum, String dept, String div, String regDate) {
        this.uid = uid;
        this.name = name;
        this.value = value;
        this.stuNum = stuNum;
        this.dept = dept;
        this.div = div;
        this.regDate = regDate;
    }
}