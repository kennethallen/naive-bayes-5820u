package me.mostly.ml.test.sp500.report;

import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

public class ROCUniquePoints {

    public static void main(final String[] args) throws IOException {
        final List<Point2D.Double> points = loadFrom(new FileReader(new File(args[0])));

        points.stream().distinct().forEachOrdered(p -> System.out.println("" + p.x + '\t' + p.y));
    }

    public static List<Point2D.Double> loadFrom(final Reader read) {
        final List<Date> dates = new ArrayList<>();
        final List<ForkJoinTask<Point2D.Double>> futures = new ArrayList<>();

        try (final BufferedReader in =
                     read instanceof BufferedReader ? (BufferedReader) read : new BufferedReader(read)) {
            String inStr;
            while ((inStr = in.readLine()) != null) {
                final String line = inStr;
                futures.add(ForkJoinPool.commonPool().submit(() -> {
                    final Scanner scan = new Scanner(line).useDelimiter("\t");
                    scan.nextDouble();
                    return new Point2D.Double(scan.nextDouble(), scan.nextDouble());
                }));
            }
        } catch (final IOException e) {
            throw new RuntimeException("Error reading input file.", e);
        }

        return futures.stream().map(f -> {
            try {
                return f.get();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }
}
