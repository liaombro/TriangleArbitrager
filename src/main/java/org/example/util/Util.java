package org.example.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

public class Util {

    public static String formattedAmount(double amount, int precision) {
        StringBuilder pattern = new StringBuilder("0.");
        int i = 0;
        while (i < precision) {
            pattern.append("0");
            i++;
        }
        DecimalFormat df = new DecimalFormat(new String(pattern));
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(amount).replace(',', '.');
    }

    public static double amountRoundedDown(double amount, int precision){
        BigDecimal bd = new BigDecimal(Double.toString(amount));
        bd = bd.setScale(precision, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static void log(String data){
        System.out.printf("%s; %s\n", new Date(), data);
    }
}
