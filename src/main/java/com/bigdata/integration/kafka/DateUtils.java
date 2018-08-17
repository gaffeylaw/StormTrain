package com.bigdata.integration.kafka;

import org.apache.commons.lang3.time.FastDateFormat;

/**
 * Created by luozhenfei1 on 2018/7/12.
 * 时间解析工具类
 */
public class DateUtils {
    private DateUtils() {
    }

    private static DateUtils instance;

    public static DateUtils getInstance() {
        if (instance == null) {
            instance = new DateUtils();
        }
        return instance;
    }

    FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    public long getTime(String time) throws Exception {
        return format.parse(time.substring(1, time.length() - 1)).getTime();
    }
}
