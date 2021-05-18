package com.banglalink.toffee.util;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Created by shantanu on 9/25/16.
 */

public class Utils {

    private Utils() {
    }

    public static String getDateTimeText(String dateTime) {

        if (TextUtils.isEmpty(dateTime)) {
            return "";
        }
        String dateTimeNew = "";
        SimpleDateFormat currentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2016-10-20 06:45:29
        Date dateObj;
        try {
            dateObj = currentFormatter.parse(dateTime);
            SimpleDateFormat postFormatter = new SimpleDateFormat("MMM dd, yyyy hh:mm aaa");
            String newDateStr = postFormatter.format(dateObj);
            dateTimeNew = newDateStr;

        } catch (Exception e) {
            e.printStackTrace();
            dateTimeNew = dateTime;
        }
        return dateTimeNew;
    }

    public static String formatValidityText(Date date) {

        SimpleDateFormat currentFormatter = new SimpleDateFormat("dd MMMM hh:mm aa"); //2020-04-25 23:59:59
        return currentFormatter.format(date);
    }

    public static Date getDate(String dateTime) {

        SimpleDateFormat currentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //2020-04-25 23:59:59
        Date dateObj = new Date();
        try {
            dateObj = currentFormatter.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateObj;
    }

    public static String getDateDiffInDayOrHourOrMinute(long stopDate) {
        Date date = new Date(stopDate);
        long diff = new Date().getTime() - date.getTime();
        long diffMinutes = diff / (60000);
        diffMinutes = Math.max(0, diffMinutes);

        if(diffMinutes >= 365*24*60){
            return (diffMinutes / (365*24*60)) + "y";
        }
        if(diffMinutes >= 30*24*60){
            return (diffMinutes / (30*24*60)) + "m";
        }
        if (diffMinutes >= 24*60) {
            return (diffMinutes / (24*60)) +"d";
        }
        if (diffMinutes >= 60){
            return (diffMinutes / 60) + "h";
        }
        /*if (diffMinutes > 0) {
            return diffMinutes + " minutes";
        }*/
        return "Just Now!";
    }
    
    public static String getDateDiffInDayOrHour(Date stopDate) {
        long diff = stopDate.getTime() - new Date().getTime();
        long diffHours = diff / (3600000);
        diffHours = Math.max(0, diffHours);

        if (diffHours >= 24) {
            return (diffHours / 24) +" days";
        }
        return diffHours + " hours";
    }

    public static String getFormattedViewsText(String viewCount) {

        if (TextUtils.isEmpty(viewCount) || !TextUtils.isDigitsOnly(viewCount)) return viewCount;

        long count = Long.parseLong(viewCount);
        if (count < 1000) return viewCount;
        else
            return viewCountFormat(count, 0);
    }

    public static boolean checkWifiOnAndConnected(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr != null && wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if (wifiInfo.getNetworkId() == -1) {
                return false; // Not connected to an access point
            }
            return true; // Connected to an access point
        } else {
            return false; // Wi-Fi adapter is OFF
        }
    }

    public static String getTime(String dateTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        DateFormat formater = new SimpleDateFormat("hh:mm aa");
        Date date = null;// converting String to date
        try {
            date = df.parse(dateTime);
            return formater.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getDay(String dateTime) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
        DateFormat formater = new SimpleDateFormat("EEEE");
        Date date = null;// converting String to date
        try {
            date = df.parse(dateTime);
            return formater.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void setFullScreen(AppCompatActivity activity, boolean visible) {
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        if (!visible) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams windowLayoutParams = activity.getWindow().getAttributes();
                windowLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                activity.getWindow().setAttributes(windowLayoutParams);
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams windowLayoutParams = activity.getWindow().getAttributes();
                windowLayoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                activity.getWindow().setAttributes(windowLayoutParams);
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    public static boolean isFullScreen(Activity activity) {
        return (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
    }


    public static String discardZeroFromDuration(String duration) {
        String retValue;
        if (TextUtils.isEmpty(duration)) {
            retValue = "00:00";
            return retValue;
        }
        if (duration.length() > 5 &&duration.startsWith("00:")) {
            retValue = duration.substring(3);
        } else {
            retValue = duration;
        }

        return retValue;
    }

    private static char[] c = new char[]{'K', 'M', 'B', 'T'};

    /**
     * Recursive implementation, invokes itself for each factor of a thousand, increasing the class on each invokation.
     *
     * @param n         the number to format
     * @param iteration in fact this is the class from the array c
     * @return a String representing the number n formatted in a cool looking way.
     */
    private static String viewCountFormat(double n, int iteration) {
        double d = ((long) n / 100) / 10.0;
        boolean isRound = (d * 10) % 10 == 0;//true if the decimal part is equal to 0 (then it's trimmed anyway)
        return (d < 1000 ? //this determines the class, i.e. 'k', 'm' etc
                ((d > 99.9 || isRound || (!isRound && d > 9.99) ? //this decides whether to trim the decimals
                        (int) d * 10 / 10 : d + "" // (int) d * 10 / 10 drops the decimal
                ) + "" + c[iteration])
                : viewCountFormat(d, iteration + 1));

    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.##").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static String getDateTime(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Dhaka"));
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        Date dateGMT = cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
        return sdf.format(dateGMT);
    }
}
