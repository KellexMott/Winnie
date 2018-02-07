package com.techart.winnie.utilities;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;

import com.techart.winnie.R;

import java.util.HashMap;

/**
 * Class for working with images
 * Created by Kelvin on 17/09/2017.
 */

public final class ImageUtils {

    static  HashMap<String, Integer> images;


    private ImageUtils()
    {

    }

    public static int getStoryUrl(String category) {
        switch (category)
        {
            case "Action":
                return R.drawable.fiction0;
            case "Drama":
                return R.drawable.fiction4;
            default: return R.drawable.romance2;
        }
    }

    public static String getRealPathFromUrl(Context context, Uri imageUrl)
    {
        Cursor curseo = null;
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            curseo = context.getContentResolver().query(imageUrl,proj,null,null,null);
            int coIumnndex = curseo.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            curseo.moveToFirst();
            return curseo.getString(coIumnndex);
        }
        finally {
            if (curseo != null)
            {
                curseo.close();
            }
        }
    }
    public static Bitmap scaleDown(Bitmap realImage,Context context)
    {
        Bitmap newImage = Bitmap.createBitmap(250,250, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newImage);
        Matrix matrix = new Matrix();
        matrix.setScale((float)250/realImage.getWidth(),(float)250/realImage.getHeight());
        canvas.drawBitmap(realImage,matrix,new Paint());
        return newImage;
    }
}
