package me.mostly.ml.test.headline;

import me.mostly.ml.BinaryClassifier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class TopHalfGDPGrowth implements BinaryClassifier<Headline> {

    static final HashSet<Integer> goodYears = new HashSet<>();

    static {
        // %YoY GDP growth for Australia from The World Bank, from 2003 to 2016
        double[] from2003 = { 3.068, 4.146, 3.204, 2.978, 3.75, 3.698, 1.812,
                2.006, 2.373, 3.634, 2.57, 2.609, 2.422, 2.766 };

        final List<Integer> years = IntStream.rangeClosed(2003, 2016).boxed().collect(Collectors.toList());
        years.sort(Comparator.comparingDouble(y -> from2003[y - 2003]));
        for (int i = years.size() / 2; i < years.size(); i++) {
            goodYears.add(years.get(i));
        }
    }

    @Override
    public Optional<Boolean> classify(Headline element) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(element.date);
        final int year = cal.get(Calendar.YEAR);
        return (year < 2003 || year > 2016) ? Optional.empty() :
                Optional.of(goodYears.contains(year));
    }
}