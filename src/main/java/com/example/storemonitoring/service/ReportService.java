package com.example.storemonitoring.service;

import com.example.storemonitoring.common.Constants;
import com.example.storemonitoring.model.ReportModel;
import com.example.storemonitoring.model.store_hours.StoreHours;
import com.example.storemonitoring.model.store_status.StoreStatus;
import com.example.storemonitoring.model.store_timezone.StoreTimezone;
import com.example.storemonitoring.repository.StoreHoursRepository;
import com.example.storemonitoring.repository.StoreStatusRepository;
import com.example.storemonitoring.repository.StoreTimezoneRepository;
import com.example.storemonitoring.util.CustomDateObject;
import com.example.storemonitoring.util.Util;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class will be utilized as a service by Report Controller.
 * Will handle the business logic.
 */
@Service
@RequiredArgsConstructor
public class ReportService {
    private final StoreHoursRepository storeHoursRepository;
    private final StoreStatusRepository storeStatusRepository;
    private final StoreTimezoneRepository storeTimezoneRepository;

    List<StoreStatus> lastWeekData = new ArrayList<>();

    private final HashMap<String, String> storeTimezoneMap = new HashMap<>();
    private final HashMap<String, String> storeCurrentDateLocalTimezoneMap = new HashMap<>();
    private final HashMap<String, HashMap<Integer, StoreHours>> storeHoursMap = new HashMap<>();
    private final HashMap<String, HashMap<String, ArrayList<StoreStatus>>> storeWiseDateWiseData = new HashMap<>();
    private final HashMap<String, HashMap<String, Integer>> storeWiseDateWiseTotalTime = new HashMap<>();
    private final HashMap<String, TreeMap<String, Integer>> storeWiseDateWiseDowntime = new HashMap<>();
    private final HashMap<String, HashMap<String, Integer>> storeWiseDateWiseDayOfTheWeek = new HashMap<>();
    private final HashMap<String, Pair<Integer, Integer>> storeWiseUptimeDownTimeLastDay = new HashMap<>();
    private final HashMap<String, Pair<Integer, Integer>> storeWiseUptimeDownTimeLastWeek = new HashMap<>();

    private final HashMap<String, Integer> storeWiseLastHourActiveBeat = new HashMap<>();
    private final HashMap<String, Integer> storeWiseLastHourInactiveBeat = new HashMap<>();
    private final HashMap<String, Integer> storeWiseLastHourDowntime = new HashMap<>();
    /**May not contain all store ids, we will have to make default value as 0*/


    public void clearAllInMemoryData() {
        lastWeekData.clear();

        storeTimezoneMap.clear();
        storeCurrentDateLocalTimezoneMap.clear();
        storeHoursMap.clear();
        storeWiseDateWiseData.clear();
        storeWiseDateWiseTotalTime.clear();
        storeWiseDateWiseDowntime.clear();
        storeWiseDateWiseDayOfTheWeek.clear();
        storeWiseUptimeDownTimeLastDay.clear();
        storeWiseUptimeDownTimeLastWeek.clear();

        storeWiseLastHourActiveBeat.clear();
        storeWiseLastHourInactiveBeat.clear();
        storeWiseLastHourDowntime.clear();
    }
    
    public List<ReportModel> generateReport() throws Exception{

        String currentTimestamp = Constants.CURRENT_TIME; //can have actual current timestamp here
        String sevenDaysBackTimestamp = getSevenDaysBackTimestamp(currentTimestamp);
        lastWeekData.addAll(getLastWeekData(sevenDaysBackTimestamp));
        populateLastDayAndLastWeekData(currentTimestamp);
        populateLastHoursData(currentTimestamp);

        List<ReportModel> reportArray = new ArrayList<>();

        storeWiseUptimeDownTimeLastWeek.forEach(
                (storeId, uptimeDowntimeLastWeek) -> {
                    ReportModel row = new ReportModel();
                    row.setStoreId(storeId);
                    row.setUptimeLastWeek(uptimeDowntimeLastWeek.getFirst()/60);
                    row.setDowntimeLastWeek(uptimeDowntimeLastWeek.getSecond()/60);

                    Pair<Integer, Integer> uptimeDowntimeLastDay = storeWiseUptimeDownTimeLastDay.get(storeId);
                    row.setUptimeLastDay(uptimeDowntimeLastDay.getFirst()/60);
                    row.setDowntimeLastDay(uptimeDowntimeLastDay.getSecond()/60);

                    int downtimeLastHour = storeWiseLastHourDowntime.getOrDefault(storeId, 0);
                    row.setUptimeLastHour(60-downtimeLastHour);
                    row.setDowntimeLastHour(downtimeLastHour);
                    reportArray.add(row);
                }
        );
        return reportArray;
    }

    

    private void populateLastDayAndLastWeekData(String currentTimestamp) {
        CustomDateObject currentDateAndTimeCustomUTC = Util.getInfoFromTimestamp(currentTimestamp);

        DateTime currentDateAndTimeUTC = new DateTime(
                currentDateAndTimeCustomUTC.getYear(),
                currentDateAndTimeCustomUTC.getMonth(),
                currentDateAndTimeCustomUTC.getDay(),
                currentDateAndTimeCustomUTC.getHr(),
                currentDateAndTimeCustomUTC.getMinute(),
                currentDateAndTimeCustomUTC.getSec(),
                DateTimeZone.UTC
        );
        for(StoreStatus storeStatus: lastWeekData) {

            String storeId = storeStatus.getStoreId();

            storeWiseDateWiseData.computeIfAbsent(storeId, k -> new HashMap<>());

            /**
             * creating Custom date object from current time stamp
             */
            CustomDateObject storeStatusDateAndTimeCustomUTC = Util.getInfoFromTimestamp(storeStatus.getTimestampUTC());

            /**
             * Converting custom date object to Joda date time object
             */
            DateTime storeStatusDateTimeUTC = new DateTime(
                    storeStatusDateAndTimeCustomUTC.getYear(),
                    storeStatusDateAndTimeCustomUTC.getMonth(),
                    storeStatusDateAndTimeCustomUTC.getDay(),
                    storeStatusDateAndTimeCustomUTC.getHr(),
                    storeStatusDateAndTimeCustomUTC.getMinute(),
                    storeStatusDateAndTimeCustomUTC.getSec(),
                    DateTimeZone.UTC
            );

            /**
             * Fetching local timezone from DB
             */
            if(!storeTimezoneMap.containsKey(storeId)){
                StoreTimezone storeTimezone = storeTimezoneRepository.findByStoreId(storeId);
                if(storeTimezone == null) {
                    storeTimezoneMap.put(storeId, Constants.DEFAULT_STORE_TIMEZONE);
                } else {
                    storeTimezoneMap.put(storeId, storeTimezone.getTimezoneStr());
                }
            }
            String storeZone = storeTimezoneMap.get(storeId);

            /**
             * Converting storeStatus's and current timezone to local timezone using Joda
             */
            DateTime storeStatusDateTimeInLocalTimezone = storeStatusDateTimeUTC
                    .withZone(DateTimeZone.forID(storeZone));
            DateTime currentDateTimeInLocalTimezone = currentDateAndTimeUTC
                    .withZone(DateTimeZone.forID(storeZone));

            /**
             * Converting Joda's DateTime objects to custom DateTime object
             */
            CustomDateObject storeStatusDateTimeInLocalTimezoneCustom = new CustomDateObject(
                    storeStatusDateTimeInLocalTimezone.getYear(),
                    storeStatusDateTimeInLocalTimezone.getMonthOfYear(),
                    storeStatusDateTimeInLocalTimezone.getDayOfMonth(),
                    0,
                    0,
                    0
            );

            CustomDateObject currentDateTimeInLocalTimezoneCustom = new CustomDateObject(
                    currentDateTimeInLocalTimezone.getYear(),
                    currentDateTimeInLocalTimezone.getMonthOfYear(),
                    currentDateTimeInLocalTimezone.getDayOfMonth(),
                    0,
                    0,
                    0
            );

            /**
             * Adding current date in local timezone in storeCurrentDateLocalTimezoneMap
             */
            if(!storeCurrentDateLocalTimezoneMap.containsKey(storeId))
                storeCurrentDateLocalTimezoneMap.put(storeId, Util.getDateFromCustomDateObject(currentDateTimeInLocalTimezoneCustom));

            if(currentDateTimeInLocalTimezoneCustom.getDay() - storeStatusDateTimeInLocalTimezoneCustom.getDay() > 7)
                continue; /** Excluding today, I want rest seven-day data only*/

            String localDate = Util.getDateFromCustomDateObject(storeStatusDateTimeInLocalTimezoneCustom);
            String localTime = Util.getTimeFromCustomDateObject(storeStatusDateTimeInLocalTimezoneCustom);

            int dayOfTheWeekFromStoreStatusDateTime = storeStatusDateTimeInLocalTimezone.getDayOfWeek();
            dayOfTheWeekFromStoreStatusDateTime--;

            storeHoursMap.computeIfAbsent(storeId, k -> new HashMap<>());

            /**
             * Fetching store hours from DB if not already in HM
             */
            if(!storeHoursMap.get(storeId).containsKey(dayOfTheWeekFromStoreStatusDateTime)) {
                StoreHours storeHoursFromDB = storeHoursRepository.findByStoreIdAndDay(storeId, dayOfTheWeekFromStoreStatusDateTime);
                if(storeHoursFromDB == null){
                    storeHoursFromDB = new StoreHours(
                            storeId, dayOfTheWeekFromStoreStatusDateTime,
                            Constants.DEFAULT_STORE_START_TIME, Constants.DEFAULT_STORE_END_TIME
                    );
                }
                storeHoursMap.get(storeId).put(dayOfTheWeekFromStoreStatusDateTime, storeHoursFromDB);
            }

            /**
             * Adding date wise total minutes of the day for all stores
             */
            StoreHours storeHoursFromDB = storeHoursMap.get(storeId).get(dayOfTheWeekFromStoreStatusDateTime);

            storeWiseDateWiseTotalTime.computeIfAbsent(storeId, k -> new HashMap<>());
            if(!storeWiseDateWiseTotalTime.get(storeId).containsKey(localDate)) {
                int totalTimeOfDay = Util.getMinutesFromTimeString(storeHoursFromDB.getEndTimeLocal()) -
                        Util.getMinutesFromTimeString(storeHoursFromDB.getStartTimeLocal());

                storeWiseDateWiseTotalTime.get(storeId).put(localDate, totalTimeOfDay);
            }
            /**
             *Adding day for local dates for stores in their respective timezones
             */
            storeWiseDateWiseDayOfTheWeek.computeIfAbsent(storeId, k -> new HashMap<>());
            storeWiseDateWiseDayOfTheWeek.get(storeId).putIfAbsent(localDate, dayOfTheWeekFromStoreStatusDateTime);

            /**
             * Checking if current storeStatus is out of bounds of StoreHours on the particular day
             */
            if(localTime.compareTo(storeHoursFromDB.getStartTimeLocal()) < 0 ||
                    localTime.compareTo(storeHoursFromDB.getEndTimeLocal()) > 0
            ) continue; /** I want local time in the range of restaurants open hours/minutes only*/

            storeWiseDateWiseData.get(storeId).computeIfAbsent(localDate, k -> new ArrayList<>());

            /**
             * Adding current store status to storeWiseDateWiseData
             */
            storeWiseDateWiseData.get(storeId).get(localDate).add(storeStatus);

        }

        findDateWiseDownTimeInLastWeekForAllStores();

        findUptimeAndDowntimeForLastWeekAndLastDay(); // both last day and last week
    }

    private void populateLastHoursData(String currentTimestamp) {
        String lastHourTimestamp = getLastHoursTimestamp(currentTimestamp);
        for(StoreStatus storeStatus : lastWeekData) {
            if(storeStatus.getTimestampUTC().compareTo(lastHourTimestamp) < 0) {
                break; //don't need store statuses before of last hour
            }
            String storeId = storeStatus.getStoreId();
            if(storeStatus.getStatus().equals(Constants.STORE_ACTIVE_STATUS)) {
                if(storeWiseLastHourActiveBeat.get(storeId) == null ||
                        storeWiseLastHourActiveBeat.get(storeId) == 0
                ) {
                    storeWiseLastHourActiveBeat.put(storeId, 1);
                }
                else{
                    storeWiseLastHourActiveBeat.put(storeId,
                            storeWiseLastHourActiveBeat.get(storeId) +1
                            );
                }
            }
            else{
                if(storeWiseLastHourInactiveBeat.get(storeId) == null ||
                        storeWiseLastHourInactiveBeat.get(storeId) == 0
                ) {
                    storeWiseLastHourInactiveBeat.put(storeId, 1);
                }
                else{
                    storeWiseLastHourInactiveBeat.put(storeId,
                            storeWiseLastHourInactiveBeat.get(storeId) +1
                    );
                }
            }
        }
        storeWiseLastHourInactiveBeat.forEach(
                (storeId, inactiveBeatCount) -> {
                    int activeBeatCount = storeWiseLastHourActiveBeat.getOrDefault(storeId, 0);
                    /** Taking percentage of inactive beat count to find average downtime in minutes */
                    int downtime = (int)(((inactiveBeatCount*1.0)/(inactiveBeatCount + activeBeatCount)) * (1.0) * 60);
                    storeWiseLastHourDowntime.put(storeId, downtime);
                }
        );
    }

    /**
     * This method cumulates the result in storeWiseDateWiseDowntime Map into last Week's and last day's standalone data
     * Populates two maps:- storeWiseUptimeDownTimeLastWeek and storeWiseUptimeDownTimeLastDay
     */
    private void findUptimeAndDowntimeForLastWeekAndLastDay() {
        storeWiseDateWiseDowntime.forEach(
                (storeId, dateWiseDowntime) -> {
                    AtomicInteger lastWeeksDownTime = new AtomicInteger();
                    AtomicInteger lastDayDownTime = new AtomicInteger();

                    AtomicInteger lastDayTotalTime = new AtomicInteger();
                    AtomicInteger lastWeeksTotalTime = new AtomicInteger();
                    AtomicInteger counter = new AtomicInteger();
                    dateWiseDowntime.forEach(
                            (localDate, downTime) -> {
                                if(counter.get() > 0 ){
                                    lastWeeksDownTime.addAndGet(downTime);
                                    lastWeeksTotalTime.addAndGet(
                                            storeWiseDateWiseTotalTime.get(storeId).get(localDate)
                                    );
                                    if(counter.get() == 1) {
                                        lastDayDownTime.set(downTime);
                                        lastDayTotalTime.set(storeWiseDateWiseTotalTime.get(storeId).get(localDate));
                                    }
                                }
                                counter.getAndIncrement();
                            }
                    );
                    
                    storeWiseUptimeDownTimeLastDay.put(storeId, 
                            Pair.of(lastDayTotalTime.get()-lastDayDownTime.get(), lastDayDownTime.get())
                    );
                    storeWiseUptimeDownTimeLastWeek.put(storeId, 
                            Pair.of(lastWeeksTotalTime.get()-lastWeeksDownTime.get(), lastWeeksDownTime.get())
                    );
                }
        );
    }

    /**
     * This method populates storeWiseDateWiseDowntime Map
     * which contains downtime for all dates in last week for all stores
     */
    private void findDateWiseDownTimeInLastWeekForAllStores() {
        storeWiseDateWiseData.forEach(
                (storeId, dateWiseData) -> {
                    dateWiseData.forEach(
                            (localDate, storeStatusArray) -> {
                                AtomicInteger downTotalAtomic = new AtomicInteger();
                                AtomicInteger upTotalAtomic = new AtomicInteger();
                                storeStatusArray.forEach(
                                        storeStatus -> {
                                            if(storeStatus.getStatus().equals(Constants.STORE_INACTIVE_STATUS)) {
                                                downTotalAtomic.getAndIncrement();
                                            }
                                            else if(storeStatus.getStatus().equals(Constants.STORE_ACTIVE_STATUS)){
                                                upTotalAtomic.getAndIncrement();
                                            }
                                        }
                                );
                                int downTime = 0;
                                int downTotal = downTotalAtomic.get();
                                int upTotal = upTotalAtomic.get();
                                if(downTotal != 0) {
                                    /** Taking percentage of inactive beat count to find average downtime in minutes */
                                    downTime = (int)(((downTotal*1.0)/(downTotal + upTotal)) * (1.0) * storeWiseDateWiseTotalTime.get(storeId).get(localDate));
                                }
                                storeWiseDateWiseDowntime.computeIfAbsent(storeId, k -> new TreeMap<>());
                                storeWiseDateWiseDowntime.get(storeId).put(localDate, downTime);
                            }
                    );
                }
        );
    }

    /**
     * This method calls DB to fetch the last week's data
     * @param sevenDaysBackTimestamp
     * @return
     */
    private List<StoreStatus> getLastWeekData(String sevenDaysBackTimestamp) {
        return storeStatusRepository.findByTimestampUTCGreaterThanEqualOrderByTimestampUTCDesc(sevenDaysBackTimestamp);
    }

    private String getSevenDaysBackTimestamp(String currentTimestamp) {
        CustomDateObject currentDateAndTimeCustom = Util.getInfoFromTimestamp(currentTimestamp);
        DateTime currentDateTime = new DateTime(
                currentDateAndTimeCustom.getYear(),
                currentDateAndTimeCustom.getMonth(),
                currentDateAndTimeCustom.getDay(),
                currentDateAndTimeCustom.getHr(),
                currentDateAndTimeCustom.getMinute(),
                currentDateAndTimeCustom.getSec(),
                DateTimeZone.UTC
        );

        DateTime sevenDaysBack = currentDateTime.minusDays(9); /**
         Taking 9 days data from DB to accommodate all timezone data for last week.
         Note:- Not considering today's data in last week and last day data.*/

        CustomDateObject sevenDaysBackCustom = new CustomDateObject(
                sevenDaysBack.getYear(),
                sevenDaysBack.getMonthOfYear(),
                sevenDaysBack.getDayOfMonth(),
                sevenDaysBack.getHourOfDay(),
                sevenDaysBack.getMinuteOfDay(),
                sevenDaysBack.getSecondOfDay()
        );

         return Util.getTimestampFromCustomDateObject(sevenDaysBackCustom);
    }
    
    private String getLastHoursTimestamp(String currentTimestamp){
        CustomDateObject currentDateAndTimeCustom = Util.getInfoFromTimestamp(currentTimestamp);
        DateTime currentDateTime = new DateTime(
                currentDateAndTimeCustom.getYear(),
                currentDateAndTimeCustom.getMonth(),
                currentDateAndTimeCustom.getDay(),
                currentDateAndTimeCustom.getHr(),
                currentDateAndTimeCustom.getMinute(),
                currentDateAndTimeCustom.getSec(),
                DateTimeZone.UTC
        );
        
        DateTime oneHourBack = currentDateTime.minusHours(1);

        CustomDateObject oneHourBackCustom = new CustomDateObject(
                oneHourBack.getYear(),
                oneHourBack.getMonthOfYear(),
                oneHourBack.getDayOfMonth(),
                oneHourBack.getHourOfDay(),
                oneHourBack.getMinuteOfDay(),
                oneHourBack.getSecondOfDay()
        );

        return Util.getTimestampFromCustomDateObject(oneHourBackCustom);
    }

}
