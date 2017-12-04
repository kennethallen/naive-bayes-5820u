package me.mostly.ml.test.sp500;

import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.NeurophNNClassifier;
import org.neuroph.core.data.DataSet;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class NNMain {

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

        // Choose training and testing sets.
        Collections.shuffle(data.stocks);
        final int numTest = data.stocks.size() * 10 / 100;
        final List<Stock> testSet = data.stocks.subList(0, numTest),
                trainSet = data.stocks.subList(numTest, data.stocks.size());
        System.out.println("Testing " + testSet);

        // Get formatted Neuroph data.
        final DataSet dataSet = NeurophNNClassifier.createDataSet(data.dates.size(), featureExtractor, trainSet, oracle);

        // Train classifier and test model.
        final NeurophNNClassifier<Stock> classifier = new NeurophNNClassifier<>(
                featureExtractor, dataSet, data.dates.size(), 50, 20, 1);

        classifier.save(args[1]);

        final StockTest tester = new StockTest(oracle, classifier,
                finalYearStartIdx, data.dates.size());
        testSet.forEach(tester::test);
        System.out.println(tester);
    }
}
