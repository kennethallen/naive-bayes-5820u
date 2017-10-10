package me.mostly.ml.test.headline;

import me.mostly.ml.BinaryClassifier;

import java.util.*;
import java.util.concurrent.TimeUnit;

class ElectionProximate implements BinaryClassifier<Headline> {

    static final List<Long> pollingDays = new ArrayList<>(4);
    static {
        final Calendar cal = Calendar.getInstance();
        cal.set(2004, Calendar.OCTOBER, 9);
        pollingDays.add(cal.getTimeInMillis());
        cal.set(2007, Calendar.NOVEMBER, 24);
        pollingDays.add(cal.getTimeInMillis());
        cal.set(2010, Calendar.AUGUST, 21);
        pollingDays.add(cal.getTimeInMillis());
        cal.set(2013, Calendar.SEPTEMBER, 7);
        pollingDays.add(cal.getTimeInMillis());
    }
    static final long timeBefore = TimeUnit.DAYS.toMillis(90),
            timeAfter = TimeUnit.DAYS.toMillis(0);

    @Override
    public Optional<Boolean> classify(Headline element) {
        final long hlDate = element.date.getTime();
        return Optional.of(pollingDays.stream().anyMatch(pollDate ->
                hlDate >= pollDate - timeBefore && hlDate <= pollDate + timeAfter));
    }
}