package util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 计算倒计时
 * Created by howen on 15/12/22.
 */
public class CalCountDown {
    public  static Long getTimeSubtract(String createAt){
        try {
            Calendar cal=Calendar.getInstance();
            cal.setTime(new java.util.Date());

            SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 格式化时间
            Calendar calendar=Calendar.getInstance();
            calendar.setTime(d.parse(createAt));

            return  cal.getTimeInMillis()-calendar.getTimeInMillis();

        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    public  static Long getEndTimeSubtract(Timestamp endAt){
        try {
            Calendar cal=Calendar.getInstance();
            cal.setTime(new java.util.Date());

            Calendar calendar=Calendar.getInstance();
            calendar.setTime(endAt);

            return  calendar.getTimeInMillis()-cal.getTimeInMillis();

        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
