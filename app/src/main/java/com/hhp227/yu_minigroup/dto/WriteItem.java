package com.hhp227.yu_minigroup.dto;

import android.graphics.Bitmap;
import android.net.Uri;

public class WriteItem {
    Uri fileUri;
    Bitmap bitmap;
    String image;

    public WriteItem() {
    }

    public WriteItem(Uri fileUri, Bitmap bitmap, String image) {
        this.fileUri = fileUri;
        this.bitmap = bitmap;
        this.image = image;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}