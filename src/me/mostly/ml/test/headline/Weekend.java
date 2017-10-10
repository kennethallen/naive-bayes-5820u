package me.mostly.ml.test.headline;

import me.mostly.ml.BinaryClassifier;

import java.util.Calendar;
import java.util.Optional;

class Weekend implements BinaryClassifier<Headline> {

    @Override
    public Optional<Boolean> classify(Headline element) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(element.date);
        final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return Optional.of(dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
    }
}