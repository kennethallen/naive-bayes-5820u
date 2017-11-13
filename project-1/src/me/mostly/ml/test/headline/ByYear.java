package me.mostly.ml.test.headline;

import me.mostly.ml.Classifier;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class ByYear implements Classifier<Headline, Integer> {

    @Override
    public List<Integer> allClasses() {
        return IntStream.rangeClosed(2003, 2017).boxed().collect(Collectors.toList());
    }

    @Override
    public Optional<Integer> classify(Headline element) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(element.date);
        return Optional.of(cal.get(Calendar.YEAR));
    }
}