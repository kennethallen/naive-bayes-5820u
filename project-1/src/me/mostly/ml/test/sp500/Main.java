package me.mostly.ml.test.sp500;

import libsvm.svm_node;
import libsvm.svm_parameter;
import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.LibSVMClassifier;
import me.mostly.ml.test.BinaryTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    static {
        PERCENT_FORMAT.setMaximumFractionDigits(2);
    }

    public static void main(final String[] args) {
        final ArrayList<Stock> stocks = new ArrayList<>();
        final ArrayList<Date> dates = new ArrayList<>();

        // Import CSV.
        try (final BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            {
                final Scanner scan = new Scanner(br.readLine()).useDelimiter(",");
                scan.next(); // Skip word "date"
                while (scan.hasNext()) {
                    dates.add(DATE_FORMAT.parse(scan.next()));
                }
            }

            String line;
            while ((line = br.readLine()) != null) {
                final Scanner scan = new Scanner(line).useDelimiter(",");
                final String ticker = scan.next();
                final double[] entries = new double[dates.size()];
                for (int i = 0; i < entries.length; i++) {
                    entries[i] = scan.nextDouble();

                    // Sanity check
                    if (!(entries[i] > -0.5 && entries[i] < 1)) {
                        System.out.println("Odd val: " + ticker + ", [" + i
                                + " (" + DATE_FORMAT.format(dates.get(i)) + ")] = " + entries[i]);
                    }
                }
                stocks.add(new Stock(ticker, entries));
            }
        } catch (final IOException e) {
            throw new RuntimeException("Error reading input file.", e);
        } catch (final ParseException e) {
            throw new RuntimeException("Error parsing number or date.", e);
        }

        // Calculate final year range.
        System.out.println();
        final Date finalYearStart;
        {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(dates.get(dates.size() - 1));
            cal.roll(Calendar.YEAR, false);
            finalYearStart = cal.getTime();
        }
        final int finalYearStartIdx = IntStream.range(0, dates.size())
                .filter(i -> dates.get(i).compareTo(finalYearStart) >= 0)
                .min().orElse(0);

        // Print stocks sorted by their return in the final year.
//        stocks.sort(Comparator.comparingDouble(s -> s.calcReturn(finalYearStartIdx, dates.size())));
//        System.out.println(stocks);

        // Separate composite index.
        final Stock sp500index = stocks.stream().filter(s -> s.ticker.equals("SP500")).findAny().orElseThrow(
                () -> new IllegalStateException("SP500 index stock not present in data.")
        );
        stocks.remove(sp500index);
        final double finalYearIndexReturn = sp500index.calcReturn(finalYearStartIdx);

        // Our classification: is a stock outperforming index during last year of data?
        final BinaryClassifier<Stock> oracle = s -> Optional.of(s.calcReturn(finalYearStartIdx) > finalYearIndexReturn);
        final Function<Stock, svm_node[]> featureExtractor = new Function<>() {
            final HashMap<Stock, svm_node[]> cache = new HashMap<>();
            @Override
            public svm_node[] apply(final Stock stock) {
                return cache.computeIfAbsent(stock, s -> s.libSvmFormat(0, finalYearStartIdx));
            }
        };

        // Choose training and testing sets.
        Collections.shuffle(stocks);
        final int numTest = stocks.size() * 10 / 100;
        final List<Stock> testSet = stocks.subList(0, numTest), trainSet = stocks.subList(numTest, stocks.size());
        System.out.println("Testing " + testSet);

        // For each parameter set, train classifier and test model.
        final Map<svm_parameter, StockTest> results = paramsToTest().collect(Collectors.toMap(Function.identity(),
                p -> {
                    final LibSVMClassifier<Stock> classifier = new LibSVMClassifier<>(
                            p, featureExtractor, trainSet, oracle);

                    final StockTest tester = new StockTest(oracle, classifier, finalYearStartIdx, dates.size());
                    testSet.forEach(tester::test);
                    return tester;
                }));

        results.entrySet().stream()
                .max(Map.Entry.comparingByValue(Comparator.comparingDouble(StockTest::rocAuc))).ifPresent(best -> {
            System.out.println();
            System.out.println("Best params: " + best.getKey());
            System.out.println();
            System.out.println(best.getValue());
            System.out.println();
            System.out.println("Best picks: " + best.getValue().picks.stream().map(s -> s.toString() + " gives "
                    + PERCENT_FORMAT.format(s.calcReturn(finalYearStartIdx, dates.size()) - 1))
                    .collect(Collectors.toList()));
        });

        System.out.println();
        System.out.println(Arrays.toString(results.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparing(StockTest::rocAuc)))
                .map(e -> "(params=" + e.getKey() + ", AUC=" + e.getValue().rocAuc() + ", average return=" + e.getValue().averageReturn() + ")").toArray()));
    }

    private static class LabeledParams extends svm_parameter {
        String notability;

        protected LabeledParams(final double C, final int kernelType, String kernelName) {
            notability = "C=" + C + ", " + kernelName + " kernel";

            svm_type = C_SVC;
            this.C = C;
            eps = 0.001;

            cache_size = 20000;
            probability = 0;
            shrinking = 0;

            kernel_type = kernelType;
        }

        public LabeledParams(final double C) {
            this(C, LINEAR, "linear");
        }

        public LabeledParams(final double C, final double gamma) {
            this(C, RBF, "RBF");
            this.gamma = gamma;
            this.notability += " (gamma=" + gamma + ")";
        }

        public LabeledParams(final double C, final double gamma, final double coef0) {
            this(C, SIGMOID, "sigmoid");
            this.gamma = gamma;
            this.coef0 = coef0;
            this.notability += " (gamma=" + gamma + ", coef0=" + coef0 + ")";
        }

        public LabeledParams(final double C, final double gamma, final double coef0, final int degree) {
            this(C, POLY, "poly");
            this.gamma = gamma;
            this.coef0 = coef0;
            this.degree = degree;
            this.notability += " (gamma=" + gamma + ", coef0=" + coef0 + ", degree=" + degree + ")";
        }

        @Override
        public String toString() {
            return notability;
        }
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