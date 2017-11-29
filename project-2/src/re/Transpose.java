package re;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class Transpose {

    public static void main(final String[] args) {
        final ArrayList<ArrayList<String>> inRows = new ArrayList<>();

        try (final BufferedReader br = new BufferedReader(new FileReader(args[0]))) {
            String line;
            while ((line = br.readLine()) != null) {
                final Scanner scan = new Scanner(line).useDelimiter(",");
                final ArrayList<String> inRow = new ArrayList<>();
                while (scan.hasNext())
                    inRow.add(scan.next());
                inRows.add(inRow);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Error reading input file", e);
        }

        if (inRows.isEmpty()) {
            try {
                final File out = new File(args[1]);
                out.delete();
                out.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Error creating output file", e);
            }
        }

        final int nOutRows = inRows.stream().mapToInt(List::size).max().orElse(0);

        try (final BufferedWriter bw = new BufferedWriter(new FileWriter(args[1]))) {
            for (int i = 0; i < nOutRows; i++) {
                if (i > 0)
                    bw.write('\n');

                for (int j = 0; j < inRows.size(); j++) {
                    if (j > 0)
                        bw.write(',');

                    final ArrayList<String> inRow = inRows.get(j);
                    if (i < inRow.size())
                        bw.write(inRow.get(i));
                }
            }
        } catch (final IOException e) {
            throw new RuntimeException("Error writing to output file", e);
        }
    }

}