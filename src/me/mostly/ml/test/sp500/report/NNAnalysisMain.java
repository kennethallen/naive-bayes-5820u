package me.mostly.ml.test.sp500.report;

import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.NeurophNNClassifier;
import me.mostly.ml.test.sp500.Data;
import me.mostly.ml.test.sp500.Stock;
import me.mostly.ml.test.sp500.StockTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NNAnalysisMain {

    static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    static {
        PERCENT_FORMAT.setMinimumFractionDigits(1);
        PERCENT_FORMAT.setMaximumFractionDigits(1);
    }

    public static void main(final String[] args) throws FileNotFoundException {
        // Import CSV.
        final Data data = Data.loadFrom(new FileReader(args[0]));

        // Calculate final year range.
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

        // Our classification: is a stock outperforming index during last year of data?
        final double finalYearIndexReturn = data.sp500.calcReturn(finalYearStartIdx);
        final BinaryClassifier<Stock> oracle = s -> Optional.of(s.calcReturn(finalYearStartIdx) > finalYearIndexReturn);
        final Function<Stock, double[]> featureExtractor = s -> s.returns;

        final NeurophNNClassifier<Stock> classifier = new NeurophNNClassifier<>(featureExtractor, new File(args[1]));

        {
            final StockTest tester = new StockTest(oracle, classifier, finalYearStartIdx, data.dates.size());
            data.stocks.forEach(tester::test);
            System.out.println(tester);
        }

        final Set<String> testTickers = new HashSet<>(Arrays.asList("DTE, PNW, CMI, KLAC, AN, STZ, GE, JEC, KHC, CSX, RF, CVX, K, DGX, TXN, AET, GWW, HOLX, URI, PFG, ORLY, VIAB, ROK, SO, MOS, CTXS, ADS, FITB, NWSA, APA, AAPL, FLR, AKAM, TMO, WDC, DNB, O, WAT, JNPR, CVS, TDC, COG, UNP, ULTA, CL, WFC, DPS, BDX, JWN, FISV".split(", ")));
        final List<Stock> testSet = data.stocks.stream().filter(s -> testTickers.contains(s.ticker)).collect(Collectors.toList());
        System.out.println();
        IntStream.rangeClosed(0, 1000).mapToDouble(i -> i / 1000d).mapToObj(s -> {
            classifier.setStrictness(s);

            final StockTest tester = new StockTest(oracle, classifier, finalYearStartIdx, data.dates.size());
            testSet.forEach(tester::test);

            return "" + s + '\t' + tester.fpr() + '\t' + tester.tpr();
        }).forEachOrdered(System.out::println);
    }
}