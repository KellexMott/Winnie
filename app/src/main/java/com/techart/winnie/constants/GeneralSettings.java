package com.techart.winnie.constants;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by abednego on 10/13/17.
 */

public class GeneralSettings {

    private static SharedPreferences preferences;
    //    check if its first time launch
    public static void setAddressTipDoNotShow(Context context, boolean status){
        preferences  = context.getSharedPreferences("generalSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("address tip Do not show",status);
        editor.apply();
    }
    public static boolean isAddressTipDoNotShowConfirmed(Context context){
        preferences  = context.getSharedPreferences("generalSettings", Context.MODE_PRIVATE);
        if (preferences.getBoolean("address tip Do not show",false)){
            return true;
        }
        else {
            return false;
        }
    }  public static void setRecipientTipDoNotShow(Context context, boolean status){
        preferences  = context.getSharedPreferences("generalSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("recipient tip Do not show",status);
        editor.apply();
    }
    public static boolean isRecipientTipDoNotShowConfirmed(Context context){
        preferences  = context.getSharedPreferences("generalSettings", Context.MODE_PRIVATE);
        if (preferences.getBoolean("recipient tip Do not show",false)){
            return true;
        }
        else {
            return false;
        }
    }
    public static void removeAddressTipDoNotShow(Context context){
        preferences  = context.getSharedPreferences("generalSettings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("address tip Do not show");
        editor.apply();
    }
}
