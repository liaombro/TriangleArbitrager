package org.example.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Util {

    public static String formattedAmount(double amount, int precision) {
        StringBuilder pattern = new StringBuilder("0.");
        int i = 0;
        while (i < precision) {
            pattern.append("0");
            i++;
        }
        DecimalFormat df = new DecimalFormat(new String(pattern));
        df.setRoundingMode(RoundingMode.DOWN);
        return df.format(amount).replace(',', '.');
    }

    public static double amountRoundedDown(double amount, int precision){
        BigDecimal bd = new BigDecimal(Double.toString(amount));
        bd = bd.setScale(precision, RoundingMode.DOWN);
        return bd.doubleValue();
    }
}
