package com.hhp227.yu_minigroup.helper;

public interface Callback {
    <T> void onSuccess(T data);
    void onFailure(Throwable throwable);
    void onLoading();
}
