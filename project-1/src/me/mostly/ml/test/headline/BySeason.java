package me.mostly.ml.test.headline;

import me.mostly.ml.Classifier;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

class BySeason implements Classifier<Headline, String> {

    private static final String[] NAMES = {"1/Winter", "2/Spring", "3/Summer", "4/Fall"};

    @Override
    public List<String> allClasses() {
        return Arrays.asList(NAMES);
    }

    @Override
    public Optional<String> classify(Headline element) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(element.date);
        final int seasonIdx = ((cal.get(Calendar.MONTH) + 1) % 12) / 3;
        return Optional.of(NAMES[seasonIdx]);
    }
}