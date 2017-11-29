package me.mostly.ml.test.sp500;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class Data {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

    public final List<Date> dates;
    public final List<Stock> stocks;
    public final Stock sp500;

    public Data(final List<Date> dates, final List<Stock> stocks) {
        this.dates = dates;
        this.stocks = stocks;

        sp500 = stocks.stream().filter(s -> s.ticker.equals("SP500")).findAny().orElseThrow(
                () -> new IllegalArgumentException("SP500 index stock not present in data.")
        );
        stocks.remove(sp500);
    }

    public static Data loadFrom(final Reader read) {
        final List<Date> dates = new ArrayList<>();
        final List<Stock> stocks = new ArrayList<>();

        try (final BufferedReader in =
                     read instanceof BufferedReader ? (BufferedReader) read : new BufferedReader(read)) {
            {
                final Scanner scan = new Scanner(in.readLine()).useDelimiter(",");
                scan.next(); // Skip word "date"
                while (scan.hasNext()) {
                    dates.add(DATE_FORMAT.parse(scan.next()));
                }
            }

            String line;
            while ((line = in.readLine()) != null) {
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

        return new Data(dates, stocks);
    }
}
