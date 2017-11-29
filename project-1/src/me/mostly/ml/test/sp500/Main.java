package me.mostly.ml.test.sp500;

import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.LibSVMClassifier;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

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
        final Function<Stock, svm_node[]> featureExtractor = new Function<>() {

            final HashMap<Stock, svm_node[]> cache = new HashMap<>();

            @Override
            public svm_node[] apply(final Stock stock) {
                return cache.computeIfAbsent(stock, s -> s.libSvmFormat(0, finalYearStartIdx));
            }
        };

        // Choose training and testing sets.
        Collections.shuffle(data.stocks);
        final int numTest = data.stocks.size() * 10 / 100;
        final List<Stock> testSet = data.stocks.subList(0, numTest),
                trainSet = data.stocks.subList(numTest, data.stocks.size());
        System.out.println("Testing " + testSet);

        // Get formatted LibSVM data.
        final svm_problem prob = LibSVMClassifier.createProblem(featureExtractor, trainSet, oracle);

        // For each parameter set, train classifier and test model.
        final Map<svm_parameter, StockTest> results = paramsToTest().parallel()
                .collect(Collectors.toMap(Function.identity(),
                        p -> {
                            System.out.println("Training for parameters: " + p);
                            final LibSVMClassifier<Stock> classifier = new LibSVMClassifier<>(p, featureExtractor, prob);

                            final StockTest tester = new StockTest(oracle, classifier,
                                    finalYearStartIdx, data.dates.size());
                            testSet.forEach(tester::test);
                            return tester;
                        }));

        results.values().stream()
                .sorted(Comparator.comparingDouble(StockTest::rocAuc))
                .forEach(System.out::println);
    }

    private static Stream<? extends svm_parameter> paramsToTest() {
        final List<LabeledParams> params = new ArrayList<>();
        DoubleStream.of(0.25, 0.5, 1, 2, 5, 10, 20, 50, 100, 1000).forEach(C -> {
            params.add(new LabeledParams(C));
            IntStream.rangeClosed(2, 10)
                    .forEach(deg -> params.add(new LabeledParams(C, 1, 1, deg)));
            DoubleStream.of(0.1, 0.2, 0.5, 1, 2)
                    .forEach(gam -> params.add(new LabeledParams(C, gam)));
        });
        return params.stream();
    }

}