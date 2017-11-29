package re;

import libsvm.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

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
            throw new RuntimeException(e);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
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

        // Choose training and testing sets.
        Collections.shuffle(stocks);
        final int numTest = stocks.size() * 10 / 100;
        final List<Stock> test = stocks.subList(0, numTest), train = stocks.subList(numTest, stocks.size());
        System.out.println("Testing " + test);

        // Our classification: 1 if outperforming index during last year of data, 0 otherwise.
        final ToIntFunction<Stock> oracle = s -> s.calcReturn(finalYearStartIdx) > finalYearIndexReturn ? 1 : 0;

        // Setup LibSVM parameters.
        final svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 1;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.RBF;
        param.cache_size = 20000;
        param.eps = 0.001;

        // Setup training data for LibSVM.
        final svm_problem prob = new svm_problem();
        prob.l = train.size();
        // Feature values:  daily returns before the final year.
        prob.x = train.stream().map(s -> s.libSvmFormat(0, finalYearStartIdx)).toArray(svm_node[][]::new);
        prob.y = train.stream().mapToDouble(s -> (double) oracle.applyAsInt(s)).toArray();

        // Train model.
        System.out.println();
        final svm_model model = svm.svm_train(prob, param);

        // Test model and output results.
        final int[] labels = new int[2];
        svm.svm_get_labels(model, labels);
        final double[] probEstimates = new double[2];
        int[][] results = new int[2][2];

        System.out.println();
        System.out.println("Probs are for classes: " + Arrays.toString(
                Arrays.stream(labels).mapToObj(i -> i == 1 ? "winner" : "loser").toArray()));
        System.out.println("S&P 500 index final year return: " + PERCENT_FORMAT.format(finalYearIndexReturn - 1));
        System.out.println();
        for (final Stock testStock : test) {
            final int prediction = (int) Math.round(svm.svm_predict_probability(model,
                    testStock.libSvmFormat(0, finalYearStartIdx), probEstimates));
            final int actual = oracle.applyAsInt(testStock);

            results[actual][prediction]++;
            final String outcome = (prediction == actual) + " " + (prediction == 1 ? "positive" : "negative");

            System.out.println(testStock.ticker + ": " + outcome + " "
                    + Arrays.toString(Arrays.stream(probEstimates).mapToObj(PERCENT_FORMAT::format).toArray())
                    + " (final year return = "
                    + PERCENT_FORMAT.format(testStock.calcReturn(finalYearStartIdx) - 1) + ")");
        }

        System.out.println();
        System.out.println("TP: " + results[1][1]);
        System.out.println("FP: " + results[0][1]);
        System.out.println("FN: " + results[1][0]);
        System.out.println("TN: " + results[0][0]);
    }

}