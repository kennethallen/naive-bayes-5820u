package me.mostly.ml.test.headline;

import me.mostly.ml.test.Oracle;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

class ByMonth implements Oracle<Headline, String> {

    private static final String[] NAMES = {
            "01/Jan", "02/Feb", "03/Mar", "04/Apr", "05/May", "06/Jun",
            "07/Jul", "08/Aug", "09/Sep", "10/Oct", "11/Nov", "12/Dec"};

    @Override
    public List<String> allClasses() {
        return Arrays.asList(NAMES);
    }

    @Override
    public Optional<String> classify(Headline element) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(element.date);
        return Optional.of(NAMES[cal.get(Calendar.MONTH)]);
    }
}