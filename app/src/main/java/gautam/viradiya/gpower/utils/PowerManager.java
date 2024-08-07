package gautam.viradiya.gpower.utils;

import java.time.LocalTime;


public class PowerManager {
    private static final int HOURS_PER_DAY = 8;
    private static final int SECONDS_PER_HOUR = 3600;
    private LocalTime powerOnTime;
    private LocalTime powerOffTime;
     LocalTime totalPower;

    public PowerManager(LocalTime powerOnTime, LocalTime powerOffTime) {
        this.powerOnTime = powerOnTime;
        this.powerOffTime = powerOffTime;
        this.totalPower = LocalTime.of(8,0,0);
    }

    public void onPowerStatusChange(boolean isPowerOn) {
        if (isPowerOn) {

        } else {
            powerOffTime = LocalTime.now();
        }
    }
}