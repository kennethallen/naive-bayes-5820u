package me.mostly.ml.test.shakespeare;

import me.mostly.ml.BayesianClassifier;
import me.mostly.ml.WordBag;
import me.mostly.ml.Vocabulary;
import me.mostly.ml.WordBagModel;
import me.mostly.ml.test.WordBagTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static final Pattern DIALOGUE_LINE = Pattern.compile(".*,\"(?<title>.+)\",.*,\".+\",.*,\"(?<dialogue>.+)\"");

    public static void main(final String[] args) throws IOException {
        final Vocabulary vocab = new Vocabulary();
        final Map<String, Play> plays = new TreeMap<>();

        try (final BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            br.readLine(); // Skip column headers

            String line;
            while ((line = br.readLine()) != null) {
                final Matcher matcher = DIALOGUE_LINE.matcher(line);
                if (matcher.matches()) {
                    final String title = matcher.group("title");
                    plays.computeIfAbsent(title, name -> new Play(vocab, title))
                            .extractAndAddWords(matcher.group("dialogue"));
                }
            }
        }

        System.out.println("Data read.");

        // Test that data titles and genre list titles match up.
        plays.keySet().stream()
                .filter(t -> !Genre.forTitle(t).isPresent())
                .findAny()
                .ifPresent(t -> {
                    throw new IllegalStateException(t + " has no matching genre");
                });
        Arrays.stream(Genre.values())
                .flatMap(g -> g.titles.stream())
                .filter(t -> !plays.containsKey(t))
                .findAny()
                .ifPresent(t -> {
                    throw new IllegalStateException(t + " has no matching genre");
                });

        // Create and train models.
        WordBagTest<Play, Genre> tester = new WordBagTest<>(vocab, new ByGenre());
        plays.forEach((title, play) -> tester.learn(play));

        // Try removing each and classifying it.
        plays.forEach((title, play) -> {
            tester.unlearn(play);
            tester.test(play);
            tester.learn(play);
        });

        System.out.println();
        System.out.println(tester);
    }

}