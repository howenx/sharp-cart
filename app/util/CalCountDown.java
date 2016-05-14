package util;

import com.google.common.base.Throwables;
import play.Logger;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import static util.SysParCom.ORDER_OVER_TIME;
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
            calendar.setTimeInMillis(d.parse(createAt).getTime()+ORDER_OVER_TIME);

            return  calendar.getTimeInMillis()-cal.getTimeInMillis();

        }catch (Exception ex){
            ex.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(ex));
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
            Logger.error(Throwables.getStackTraceAsString(ex));
            return null;
        }
    }
}
