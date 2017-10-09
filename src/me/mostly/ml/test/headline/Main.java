package me.mostly.ml.test.headline;

import me.mostly.ml.BayesianClassifier;
import me.mostly.ml.WordBag;
import me.mostly.ml.Vocabulary;
import me.mostly.ml.WordBagModel;
import me.mostly.ml.test.BinaryOracle;
import me.mostly.ml.test.Oracle;
import me.mostly.ml.test.WordBagBinaryTest;
import me.mostly.ml.test.WordBagTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static final Pattern DATA_LINE = Pattern.compile("(?<date>\\d{8}),(?<headline>.*)");

    public static void main(final String[] args) throws IOException {
        final Vocabulary vocab = new Vocabulary();
        final ArrayList<Headline> headlines = new ArrayList<>();

        try (final BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            br.readLine(); // Skip column headers

            final Calendar cal = Calendar.getInstance();

            String line;
            while ((line = br.readLine()) != null) {
                final Matcher matcher = DATA_LINE.matcher(line);
                if (matcher.matches()) {
                    final String dateStr = matcher.group("date");
                    cal.set(Integer.parseInt(dateStr.substring(0, 4)),
                            Integer.parseInt(dateStr.substring(4, 6)) - 1,
                            Integer.parseInt(dateStr.substring(6)));

                    headlines.add(new Headline(vocab, cal.getTime(), matcher.group("headline")));
                }
            }
        }

        System.out.println("Data read.");

        // Select test headlines.
        Collections.shuffle(headlines);
        final List<Headline> testHLs = headlines.subList(0,
                Math.min(headlines.size(), Math.max(1, Math.round(headlines.size() * 0.10f)))),
                trainingHLs = headlines.subList(testHLs.size(), headlines.size());

        Arrays.asList(new ByMonth(), new BySeason(), new ByYear(), new ElectionProximate(), new FirstChronoHalf(),
                new LaborPM(), new TopHalfGDPGrowth(), new Weekend()).forEach(oracle -> {
            final WordBagTest<Headline, ?> tester = oracle instanceof BinaryOracle
                    ? new WordBagBinaryTest<>(vocab, (BinaryOracle<Headline>) oracle)
                    : new WordBagTest<>(vocab, oracle);

            trainingHLs.forEach(tester::learn);
            testHLs.parallelStream().forEach(tester::test);

            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println(tester);
        });
    }

}