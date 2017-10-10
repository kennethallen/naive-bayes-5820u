package me.mostly.ml.test.headline;

import me.mostly.ml.BinaryClassifier;

import java.util.*;

class FirstChronoHalf implements BinaryClassifier<Headline> {

    private static final Date MIDPOINT;
    static {
        final Calendar cal = Calendar.getInstance();
        cal.set(2003, Calendar.FEBRUARY, 19);
        final long start = cal.getTimeInMillis();
        cal.set(2017, Calendar.JUNE, 30);
        final long end = cal.getTimeInMillis();
        MIDPOINT = new Date(start/2 + end/2);
    }

    @Override
    public Optional<Boolean> classify(Headline element) {
        return Optional.of(element.date.compareTo(MIDPOINT) < 0);
    }
}
