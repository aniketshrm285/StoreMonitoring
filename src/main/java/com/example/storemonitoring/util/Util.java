package com.example.storemonitoring.util;

/**
 * Utility class.
 */
public class Util {
    //"2023-01-25 18:13:22.47922 UTC"
    public static CustomDateObject getInfoFromTimestamp(String timestamp) {
        if(timestamp == null) return null;
        String[] dateAndTime = timestamp.split(" ");
        String date = dateAndTime[0];
        String time = dateAndTime[1];
        String[] dateSplit = date.split("-");
        String[] timeSplit = time.split(":");

        return new CustomDateObject(
                Integer.parseInt(dateSplit[0]),
                Integer.parseInt(dateSplit[1]),
                Integer.parseInt(dateSplit[2]),
                Integer.parseInt(timeSplit[0]),
                Integer.parseInt(timeSplit[1]),
                Integer.parseInt(timeSplit[2].split("\\.")[0])
        );
    }

    public static String getTimestampFromCustomDateObject(CustomDateObject customDateObject) {
        return customDateObject.getYear() + "-" +
                getDoubleDigitsString(customDateObject.getMonth()) + "-" +
                getDoubleDigitsString(customDateObject.getDay()) + " " +
                getDoubleDigitsString(customDateObject.getHr()) + ":" +
                getDoubleDigitsString(customDateObject.getMinute()) + ":" +
                getDoubleDigitsString(customDateObject.getSec()) + "." +
                "0000" + " " +
                "UTC";
    }

    public static String getDateFromCustomDateObject(CustomDateObject customDateObject) {
        return customDateObject.getYear() + "-" +
                getDoubleDigitsString(customDateObject.getMonth()) + "-" +
                getDoubleDigitsString(customDateObject.getDay());
    }

    public static String getTimeFromCustomDateObject(CustomDateObject customDateObject) {
        return getDoubleDigitsString(customDateObject.getHr()) + ":" +
                getDoubleDigitsString(customDateObject.getMinute()) + ":" +
                getDoubleDigitsString(customDateObject.getSec());
    }

    public static int getMinutesFromTimeString(String time) {
        String[] timeSplit = time.split(":");
        int ans = 0;
        ans += (Integer.parseInt(timeSplit[0]) * 60);
        ans += Integer.parseInt(timeSplit[1]);
        if(Integer.parseInt(timeSplit[2]) == 59) ans++;
        return ans;
    }

    private static String getDoubleDigitsString(int num) {
        if(num/10 > 0) {
            return String.valueOf(num);
        }
        else{
            return "0"+num;
        }
    }


}
