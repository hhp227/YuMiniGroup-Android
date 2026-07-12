package com.hhp227.yu_minigroup.adapter;

import com.hhp227.yu_minigroup.dto.SeatItem;

public class BindingUtils {
    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static String getSeatCountText(SeatItem item) {
        return "[" + parseInt(item.occupied) + "/" + item.count + "]";
    }
}
