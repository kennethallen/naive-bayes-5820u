package me.mostly.ml.test.headline;

import me.mostly.ml.test.BinaryOracle;
import me.mostly.ml.test.Oracle;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

class LaborPM implements BinaryOracle<Headline> {

    private static final long laborIn, laborOut;
    static {
        final Calendar cal = Calendar.getInstance();
        cal.set(2007, Calendar.DECEMBER, 3);
        laborIn = cal.getTimeInMillis();
        cal.set(2013, Calendar.SEPTEMBER, 18);
        laborOut = cal.getTimeInMillis();
    }

    @Override
    public Optional<Boolean> classify(Headline element) {
        final long hlDate = element.date.getTime();
        return Optional.of(hlDate >= laborIn && hlDate < laborOut);
    }
}