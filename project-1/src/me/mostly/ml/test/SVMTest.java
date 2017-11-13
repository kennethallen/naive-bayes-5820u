package me.mostly.ml.test;

import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.SVMClassifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class SVMTest {

    public static void main(final String[] args) {
        final ThreadLocalRandom rand = ThreadLocalRandom.current();

        final List<Point> trainers = Arrays.asList(
                new Point(rand.nextDouble(0.5, 4.5), rand.nextDouble(0.5, 4.5), true),
                new Point(rand.nextDouble(0.5, 4.5), rand.nextDouble(0.5, 4.5), false)
//                new Point(1, 1, false), new Point(2, 1, false), new Point(3, 1, true),
//                new Point(1, 2, false), new Point(2, 2, false), new Point(3, 2, true),
//                new Point(1, 3, false), new Point(2, 3, true), new Point(3, 3, true)
        );

        final SVMClassifier<Point> classifier = new SVMClassifier<Point>(
                (a, b) -> a.x * b.x + a.y * b.y,
                trainers,
                (BinaryClassifier<Point>)(p -> Optional.of(p.cls))
        ) {
            @Override
            public String toString() {
                return super.toString() + "\nEquivalent w: " + data.stream()
                        .map(e -> new Point(e.entry.x*e.signedAlpha(), e.entry.y*e.signedAlpha(), false))
                        .reduce(new Point(0, 0, false), (p1, p2) -> new Point(p1.x + p2.x, p1.y + p2.y, false));
            }
        };

        System.out.println(classifier);
        for (int i = 0; i < 1000000; i++) {
            classifier.step(100);
            if (i % 10000 == 0) {
                System.out.println();
                System.out.println("After " + i + "th iteration: " + classifier);
            }
        }

//        System.out.println();
//        Arrays.asList(
//                new Point(0, 0, false),
//                new Point(3, 3, true),
//                new Point(3, 2, true)
//        ).forEach(p -> System.out.println(p + " classifies as " + classifier.classify(p)));

        // Graph
        final int xMax = 5, yMax = 5, xScale = 10, yScale = 5;

        final char[][] results = new char[xMax * xScale + 1][yMax * yScale + 1];
        for (int x = 0; x <= xMax * xScale; x++) {
            for (int y = 0; y <= yMax * yScale; y++) {
                results[x][y] = classifier
                        .classify(new Point(((double) x)/xScale, ((double) y)/xMax, false))
                        .map(c -> c ? '+' : '-')
                        .orElse(' ');
            }
        }

        trainers.forEach(t -> {
            final int x = (int) Math.round(t.x * xScale), y = (int) Math.round(t.y * yScale);
            if (x >= 0 && y >= 0 && x < results.length && y < results[x].length) {
                results[x][y] = t.cls ? '1' : '0';
            }
        });

        System.out.println();
        for (int y = yMax * yScale; y >= 0; y--) {
            System.out.print((y % yScale == 0 ? y/yScale : "|") + " ");
            for (int x = 0; x <= xMax * xScale; x++) {
                System.out.print(results[x][y]);
            }
            System.out.println();
        }
        System.out.print("\\_");
        for (int x = 0; x <= xMax * xScale; x++) {
            System.out.print((x % xScale == 0 ? x/xScale : "_"));
        }
        System.out.println();
    }

    static class Point {
        final double x, y;
        final boolean cls;

        Point(double x, double y, boolean cls) {
            this.x = x;
            this.y = y;
            this.cls = cls;
        }

        @Override
        public String toString() {
            return (cls ? "+" : "-") + "(" + x + "," + y + ")";
        }
    }

}
