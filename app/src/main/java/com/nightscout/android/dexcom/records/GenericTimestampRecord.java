package com.nightscout.android.dexcom.records;

import com.nightscout.android.dexcom.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class GenericTimestampRecord {

    protected final int OFFSET_SYS_TIME = 0;
    protected final int OFFSET_DISPLAY_TIME = 4;
    protected Date systemTime;
    protected int systemTimeSeconds;
    protected Date displayTime;

    public GenericTimestampRecord(byte[] packet) {
        ByteBuffer wrapped = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN);
        systemTimeSeconds = wrapped.getInt(OFFSET_SYS_TIME);
        systemTime = Utils.receiverTimeToDate(systemTimeSeconds);
        displayTime = Utils.receiverTimeToDate(wrapped.getInt(OFFSET_DISPLAY_TIME));
    }

    public GenericTimestampRecord(Date displayTime, Date systemTime){
        this.displayTime=displayTime;
        this.systemTime=systemTime;
    }

    public Date getSystemTime() {
        return systemTime;
    }

    public int getSystemTimeSeconds() {
        return systemTimeSeconds;
    }

    public Date getDisplayTime() {
        return displayTime;
    }
    public long getDisplayTimeSeconds() {
        return displayTime.getTime();
    }

}
