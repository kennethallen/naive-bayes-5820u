package me.mostly.ml.test.sp500.report;

import me.mostly.ml.test.sp500.Data;
import me.mostly.ml.test.sp500.SVMMain;
import me.mostly.ml.test.sp500.Stock;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
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

        final List<Stock> testSet = fromTickers("DTE, PNW, CMI, KLAC, AN, STZ, GE, JEC, KHC, CSX, RF, CVX, K, DGX, TXN, AET, GWW, HOLX, URI, PFG, ORLY, VIAB, ROK, SO, MOS, CTXS, ADS, FITB, NWSA, APA, AAPL, FLR, AKAM, TMO, WDC, DNB, O, WAT, JNPR, CVS, TDC, COG, UNP, ULTA, CL, WFC, DPS, BDX, JWN, FISV", data);
        final List<Stock> picks = fromTickers("AAPL, AKAM, APA, CMI, CSX, CTXS, DGX, DNB, FISV, FITB, FLR, GE, GWW, HOLX, JEC, JNPR, KLAC, PFG, RF, ROK, TXN, ULTA, UNP, URI, WAT, WDC, WFC", data);

        testSet.add(data.sp500);

        testSet.sort(Comparator.comparingDouble(s -> s.calcReturn(finalYearStartIdx)));
        final List<String> entries = testSet.stream().map(s -> {
            String start;
            if (s.ticker.equals("SP500")) {
                start = "\\textit{S\\&P 500}";
            } else if (picks.contains(s)) {
                start = "\\textbf{" + s.ticker + "}";
            } else {
                start = s.ticker;
            }
            if ((testSet.indexOf(s) > testSet.indexOf(data.sp500)) != picks.contains(s))
                start += "*";

            return start + " & " + SVMMain.PERCENT_FORMAT.format(s.calcReturn(finalYearStartIdx) - 1);
        }).collect(Collectors.toList());

        final int rows = (entries.size() + 2)/3;
        for (int i = 0; i < rows; i++) {
            final String line = "    " + entries.get(i) + " & "
                    + entries.get(i + rows) + " & " + entries.get(i + 2*rows) + "\\\\";
            System.out.println(line.replaceAll("%", Matcher.quoteReplacement("\\%")));
        }
    }

    private static List<Stock> fromTickers(final String in, final Data data) {
        final Set<String> tickers = new HashSet<>(Arrays.asList(in.split(", ")));
        return data.stocks.stream().filter(s -> tickers.contains(s.ticker)).collect(Collectors.toList());
    }
}
