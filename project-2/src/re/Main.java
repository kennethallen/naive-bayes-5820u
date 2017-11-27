package re;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class Main {

    private static final int ENTRIES = 4075;
    private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);

    public static void main(final String[] args) {
        final ArrayList<Stock> stocks = new ArrayList<>();
        final Date[] dates = new Date[ENTRIES];

        try (final BufferedReader br = new BufferedReader(new FileReader("data/ReturnsFull.csv"))) {
            {
                final Scanner scan = new Scanner(br.readLine()).useDelimiter(",");
                scan.next(); // Skip word "date"
                while (scan.hasNext()) {
                    stocks.add(new Stock(scan.next(), ENTRIES));
                }
            }

            final Calendar cal = Calendar.getInstance();
            int i = 0;
            String line;
            while ((line = br.readLine()) != null) {
                final Scanner scan = new Scanner(line).useDelimiter(",");

                cal.setTime(DATE_FORMAT.parse(scan.next()));
                dates[i] = cal.getTime();

                for (final Stock stock : stocks) {
                    stock.returns[i] = scan.nextDouble();
                }

                i++;
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
