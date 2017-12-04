package me.mostly.ml.test.sp500;

import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.NeurophNNClassifier;
import org.neuroph.core.data.DataSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
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

        final StockTest tester = new StockTest(oracle, classifier, finalYearStartIdx, data.dates.size());
        data.stocks.forEach(tester::test);
        System.out.println(tester);
    }
}