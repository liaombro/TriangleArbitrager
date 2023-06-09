package org.example;

import org.example.util.Constants;
import org.example.util.Util;

import java.util.Collections;
import java.util.List;

import static org.example.util.Constants.*;
import static org.example.util.Util.log;

public class Triangle {


    private final OrientedPair first;

    private final OrientedPair second;

    private final OrientedPair third;


    private final Asset asset1;
    private final Asset asset2;
    private final Asset asset3;
    private double amountToTrade1; // Amount of BTC to sell
    private double amountToTrade2; // Amount of X to sell
    private double amountToTrade3; // Amount of Y to sell

    private BookEntry entry1;
    private BookEntry entry2;
    private BookEntry entry3;

    private double price1;
    private double price2;
    private double price3;
    private MarketData marketData = MarketData.INSTANCE;
    private double amountOfBTCToUse;


    public Triangle(MarketData marketData, TradingPair first, TradingPair second, TradingPair third) {
        this(first, second, third);
        this.marketData = marketData;
        entry1 = marketData.getBookEntryByPair(first);
        entry2 = marketData.getBookEntryByPair(second);
        entry3 = marketData.getBookEntryByPair(third);
    }

    public Triangle(TradingPair first, TradingPair second, TradingPair third) {

        OrientedPair oriented1 = new OrientedPair(first);
        OrientedPair oriented2 = new OrientedPair(second);
        OrientedPair oriented3 = new OrientedPair(third);


        boolean isReverseNeeded = first.getBase().equals(second.getBase()) || first.getBase().equals(third.getBase()) || second.getBase().equals(third.getBase());
        if (isReverseNeeded) {
            if (isReverseNeeded(first, second, third) || isReverseNeeded(first, third, second)) {
                oriented1.reverse();
            } else if (isReverseNeeded(second, first, third) || isReverseNeeded(second, third, first)) {
                oriented2.reverse();
            } else if (isReverseNeeded(third, second, first) || isReverseNeeded(third, first, second)) {
                oriented3.reverse();
            }
        }

        if (!oriented1.getSource().equals(Constants.BTC)) {
            oriented1.reverse();
            oriented2.reverse();
            oriented3.reverse();
        }
        this.first = oriented1;
        this.second = oriented2;
        this.third = oriented3;

        asset1 = this.first.getSource(); // BTC
        asset2 = this.second.getSource();
        asset3 = this.third.getSource();

        entry1 = marketData.getBookEntryByPair(first);
        entry2 = marketData.getBookEntryByPair(second);
        entry3 = marketData.getBookEntryByPair(third);
    }

    public boolean isReverseOfPairNotNeeded(TradingPair one, TradingPair other) {
        return one.getBase().equals(other.getQuote()) || one.getQuote().equals(other.getBase());
    }

    public boolean isReverseNeeded(TradingPair checkedPair, TradingPair decomposeA, TradingPair decomposeB) {
        return checkedPair.getBase().equals(decomposeA.getBase()) && checkedPair.getQuote().equals(decomposeB.getQuote());
    }

    public OrientedPair getFirst() {
        return first;
    }

    public OrientedPair getSecond() {
        return second;
    }

    public OrientedPair getThird() {
        return third;
    }

    public String toString() {
        return first.toString() + " " + second.toString() + " " + third.toString();
    }

    public boolean triangleIsNew() {
        long timestamp1 = entry1.getTimestampWhenUpdated();
        long timestamp2 = entry2.getTimestampWhenUpdated();
        long timestamp3 = entry3.getTimestampWhenUpdated();
        long timestamp = System.currentTimeMillis();

        return (timestamp - timestamp1 <= ACCEPTABLE_TRIANGLE_AGE_IN_MILLIS)
                && (timestamp - timestamp2 <= ACCEPTABLE_TRIANGLE_AGE_IN_MILLIS)
                && (timestamp - timestamp3 <= ACCEPTABLE_TRIANGLE_AGE_IN_MILLIS);
    }


    public boolean trianglePricesAreProfitable() {
        price1 = first.isReversed() ? 1 / marketData.getAskPrice(first.getPair()) : marketData.getBidPrice(first.getPair());
        price2 = second.isReversed() ? 1 / marketData.getAskPrice(second.getPair()) : marketData.getBidPrice(second.getPair());
        price3 = third.isReversed() ? 1 / marketData.getAskPrice(third.getPair()) : marketData.getBidPrice(third.getPair());

        return !(price1 * price2 * price3 * Math.pow(1 - FeeSchedule.getMultiplicatorFee(), 3) < 1);
    }

    public double setAmount(OrientedPair orientedPair, Asset firstAsset, Asset secondAsset) {
        boolean reversed = orientedPair.isReversed();
        double amount = reversed ? marketData.getAskAmount(orientedPair.getPair()) * marketData.getAskPrice(orientedPair.getPair()) : marketData.getBidAmount(orientedPair.getPair());
        amount = roundAmount(orientedPair, amount);
        return amount;
    }

    public double roundAmount(OrientedPair orientedPair, double amount) {
        int scale = orientedPair.getScale();
        amount = Util.amountRoundedDown(amount, scale);
        return amount;
    }

    public boolean triangleAmountsAreGreaterThanMinimum() {
        double amountOfAsset1 = setAmount(first, asset1, asset2);
        double amountOfAsset2 = setAmount(second, asset2, asset3);
        double amountOfAsset3 = setAmount(third, asset3, asset1);
        List<Double> amounts = List.of(amountOfAsset1, amountOfAsset2 / price1, amountOfAsset3 / (price1 * price2), AMOUNT_OF_BTC_TO_TRADE);

        amountOfBTCToUse = Collections.min(amounts);
        double amount1 = amountOfBTCToUse;
        double amount2 = amountOfBTCToUse * price1;
        double amount3 = amountOfBTCToUse * (price1 * price2);

        System.out.printf("Amolunts of assets: %s BTC, %s FUND, %s TRX\n", amount1, amount2, amount3);

        boolean amountsOfAssetsAreGood = amount1 > 0 &&
                amount1 > asset1.getMinAmount() &&
                amount2 > asset2.getMinAmount() &&
                amount3 > asset3.getMinAmount();

        boolean amountInPairsAreGood =
                amountsInPairAreGood(first, amount1, amount2) &&
                        amountsInPairAreGood(second, amount2, amount3) &&
                        amountsInPairAreGood(third, amount3, amount1);

        return amountsOfAssetsAreGood && amountInPairsAreGood;
    }


    public boolean amountsInPairAreGood(OrientedPair pair, double amount1, double amount2) {
        if (pair.isReversed()) {
            return amount1 > pair.getPair().getMinAmount() && amount2 > pair.getPair().getMinQuantity();
        }
        return amount1 > pair.getPair().getMinQuantity() && amount2 > pair.getPair().getMinAmount();

    }


    public boolean isProfitable() {

        if (!triangleIsNew()) {
            return false;
        }

        if (!trianglePricesAreProfitable()) {
            return false;
        }
        if (!triangleAmountsAreGreaterThanMinimum()) {
            return false;
        }

        setAmountsToTrade();
        if (!areAmountsToTradePositive()) {
            return false;
        }
        log(String.format("<TRIANGLE profitable>\n %s \n Prices %s, %s, %s \n Prices of triangle %s, %s, %s \n Amounts to trade %s, %s, %s \n </TRIANGLE>\n", this,
                first.getPair().logPrices(), second.getPair().logPrices(), third.getPair().logPrices(),
                price1, price2, price3,
                amountToTrade1, amountToTrade2, amountToTrade3));
        return true;

    }


    private boolean areAmountsToTradePositive() {
        return amountToTrade1 > 0 && amountToTrade2 > 0 && amountToTrade3 > 0;
    }

    private void setAmountsToTrade() {
        double fee = (1 - FeeSchedule.getMultiplicatorFee());
//        double am1 = first.isReversed() ? amountOfBTCToUse * fee * price1 : amountOfBTCToUse * fee;
//        double am2 = second.isReversed() ? amountOfBTCToUse * price1 * fee * price2 * fee : am1 * fee;
//        double am3 = third.isReversed() ? amountOfBTCToUse * price1 * fee * price2 * fee * price3 * fee : am2 * fee;

        //old above

        boolean condition1 = first.getBase().equals(BTC) && first.isReversed() || first.getQuote().equals(BTC) && !first.isReversed();
        boolean condition2 = second.getBase().equals(asset2) && second.isReversed() || second.getQuote().equals(asset2) && !second.isReversed();
        boolean condition3 = third.getBase().equals(asset3) && third.isReversed() || third.getQuote().equals(asset3) && !third.isReversed();

        System.out.printf("Assets: %s, %s, %s, Bases: %s, %s, %s, Conditions: %s, %s, %s\n", asset1.getName(), asset2.getName(), asset3.getName(),
                first.getBase().getName(), second.getBase().getName(), third.getBase().getName(),
                condition1, condition2, condition3);
        double am1 = condition1 ? amountOfBTCToUse * fee * price1 : amountOfBTCToUse * fee;
        double am2 = condition2 ? amountOfBTCToUse * price1 * fee * price2 * fee : am1 * price1 * fee;
        double am3 = condition3 ? amountOfBTCToUse * price1 * fee * price2 * fee * price3 * fee : am2 * price2 * fee;

        amountToTrade1 = roundAmount(first, am1);
        amountToTrade2 = roundAmount(second, am2);
        amountToTrade3 = roundAmount(third, am3);

    }

    public boolean isProfitableWhenReversed() {
        // needs implementation
        return false;
    }

    public double getAmountToTrade1() {
        return amountToTrade1;
    }

    public double getAmountToTrade2() {
        return amountToTrade2;
    }

    public double getAmountToTrade3() {
        return amountToTrade3;
    }
}
