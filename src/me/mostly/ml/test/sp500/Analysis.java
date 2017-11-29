package me.mostly.ml.test.sp500;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.stream.IntStream;

public class Analysis {

    public static void main(final String[] args) throws FileNotFoundException {
        final Data data = Data.loadFrom(new FileReader(args[0]));

        final Date finalYearStart;
        {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(data.dates.get(data.dates.size() - 1));
            cal.roll(Calendar.YEAR, false);
            finalYearStart = cal.getTime();
        }
        final int finalYearStartIdx = IntStream.range(0, data.dates.size())
                .filter(i -> data.dates.get(i).compareTo(finalYearStart) >= 0)
                .min().orElse(0);

        final double finalYearIndexReturn = data.sp500.calcReturn(finalYearStartIdx);

        final Set<String> tickers = new HashSet<>(Arrays.asList("HOG, MCO, CSX, BEN, FOXA, HRB, ATVI, LRCX, SBUX, KIM, WU, ULTA, MU, GE, LVLT, WDC, AIZ, NEM, CCL, BWA, MAT, HD, PRU, WLTW, COO, BMY, LLY, ZION, HON, NOC, KHC, CELG, KMX, AAP, YHOO, UPS, DISCA, CTXS, DLTR, FBHS, CHRW, KR, CVX, CBG, ES, VRTX, ARNC, MPC, QRVO, FTV".split(", ")));
        final List<Stock> stocks = new ArrayList<>(data.stocks);
        stocks.removeIf(s -> !tickers.contains(s.ticker));

        stocks.add(data.sp500);

        stocks.sort(Comparator.comparingDouble(s -> s.calcReturn(finalYearStartIdx)));
        stocks.forEach(s -> {
            System.out.println(s.ticker + " & " + Main.PERCENT_FORMAT.format(s.calcReturn(finalYearStartIdx) - 1));
        });
    }
}
