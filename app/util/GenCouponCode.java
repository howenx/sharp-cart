package util;

import java.util.Random;

/**
 * 产生优惠券码
 * Created by howen on 15/12/3.
 */
public class GenCouponCode {

    private static final String uCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String intChar = "0123456789";

    public static String GetCode(int code,int length){
        Random r = new Random();
        String pass = "";
        while (pass.length () != length){
            if(pass.length()==0){
                int spot = r.nextInt (25);
                pass += uCase.charAt(spot);
            }
            int rPick = r.nextInt(4);
            if (rPick == 1 || rPick == 0) {
                int spot = r.nextInt (25);
                pass += uCase.charAt(spot);
            } else if (rPick == 2 || rPick ==3) {
                int spot = r.nextInt (9);
                pass += intChar.charAt (spot);
            }
        }
        return code+pass;
    }

    public static String GetCode(int length){
        Random r = new Random();
        String pass = "";
        while (pass.length () != length){
            if(pass.length()==0){
                int spot = r.nextInt (25);
                pass += uCase.charAt(spot);
            }
            int rPick = r.nextInt(4);
            if (rPick == 1 || rPick == 0) {
                int spot = r.nextInt (25);
                pass += uCase.charAt(spot);
            } else if (rPick == 2 || rPick ==3) {
                int spot = r.nextInt (9);
                pass += intChar.charAt (spot);
            }
        }
        return pass;
    }


    public enum CouponClassCode{

        COSMETICS("化妆品类商品适用券", 153),
        ACCESSORIES("配饰类商品适用券", 172),
        CLOTHES("服饰类商品适用券", 165),
        ALL_FREE("全场通用券", 555),
        REGISTER_PUBLIC("新人优惠券", 777),
        PART_GOODS("指定商品适用券", 211),
        SHIP_FREE("免邮券", 999);

        // 成员变量
        private String name;
        private int index;

        // 构造方法
        private CouponClassCode(String name, int index) {
            this.name = name;
            this.index = index;
        }

        // 普通方法
        public static String getName(int index) {
            for (CouponClassCode c : CouponClassCode.values()) {
                if (c.getIndex() == index) {
                    return c.name;
                }
            }
            return null;
        }
        // get set 方法
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }
}
