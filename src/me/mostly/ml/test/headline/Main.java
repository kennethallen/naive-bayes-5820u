package me.mostly.ml.test.headline;

import me.mostly.ml.Vocabulary;
import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.test.WordBagBinaryTest;
import me.mostly.ml.test.WordBagTest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {

    private static final Pattern DATA_LINE = Pattern.compile("(?<date>\\d{8}),(?<headline>.*)");

    public static void main(final String[] args) {
        final Vocabulary vocab = new Vocabulary();
        final List<Headline> headlines = Arrays.stream(args)
                .map(File::new)
//                .filter(f -> f.getName().contains("abc"))
                .flatMap(path -> readHeadlines(vocab, path).stream())
                .collect(Collectors.toList());

        System.out.println("Data read.");

        // Select test headlines.
        Collections.shuffle(headlines);
        final List<Headline> testHLs = headlines.subList(0,
                Math.min(headlines.size(), Math.max(1, Math.round(headlines.size() * 0.10f)))),
                trainingHLs = headlines.subList(testHLs.size(), headlines.size());

        Arrays.asList(
//                new ByMonth(),
//                new BySeason(),
//                new ByYear(),
//                new FirstChronoHalf(headlines),
//                new Weekend(),
//                new AusNatlElectionProximate(),
//                new LaborPM()
//                new TopHalfGDPGrowth(),
                new Crowdsourced()
                ).forEach(oracle -> {
            final WordBagTest<Headline, ?> tester = oracle instanceof BinaryClassifier
                    ? new WordBagBinaryTest<>(vocab, (BinaryClassifier<Headline>) oracle)
                    : new WordBagTest<>(vocab, oracle);

            trainingHLs.forEach(tester::learn);
            testHLs.forEach(tester::test);

            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println(tester);
        });
    }

    private static String sourceForFilename(String filename) {
        String source = "unknown";
        if (filename.contains("abc"))
            source = "Australian Broadcasting Corporation";
        if (filename.contains("examiner"))
            source = "The Examiner";
        return source;
    }

    private static List<Headline> readHeadlines(final Vocabulary vocab, final File file) {
        final ArrayList<Headline> headlines = new ArrayList<>();
        final String source = sourceForFilename(file.getName());



        try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
            final Calendar cal = Calendar.getInstance();

            String line;
            int lineNo = 0;
            while ((line = br.readLine()) != null) {
                final Matcher matcher = DATA_LINE.matcher(line);
                if (matcher.matches()) {
                    final String dateStr = matcher.group("date");
                    cal.set(Integer.parseInt(dateStr.substring(0, 4)),
                            Integer.parseInt(dateStr.substring(4, 6)) - 1,
                            Integer.parseInt(dateStr.substring(6)));

                    headlines.add(new Headline(vocab, cal.getTime(), matcher.group("headline"), source));
                }

                if ((++lineNo % 100000) == 0)
                    System.out.println("Read " + lineNo + " lines");
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Loaded " + file);
        return headlines;
    }

}