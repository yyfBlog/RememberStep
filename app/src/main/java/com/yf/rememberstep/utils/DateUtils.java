package com.yf.rememberstep.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yyf on 2017/11/10.
 */

public class DateUtils {

    /**
     * 获取当前的日期
     * @return 例如：2017-11-11
     */
    public static String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
}
