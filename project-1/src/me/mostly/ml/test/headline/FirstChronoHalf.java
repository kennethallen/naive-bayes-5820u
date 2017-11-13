package me.mostly.ml.test.headline;

import me.mostly.ml.BinaryClassifier;

import java.util.*;

class FirstChronoHalf implements BinaryClassifier<Headline> {

    final Date midpoint;

    public FirstChronoHalf(Collection<? extends Headline> headlines) {
        midpoint = new Date((headlines.stream().map(h -> h.date).min(Comparator.naturalOrder()).get().getTime() / 2L)
                + (headlines.stream().map(h -> h.date).max(Comparator.naturalOrder()).get().getTime() / 2L));
    }

    @Override
    public Optional<Boolean> classify(Headline element) {
        return Optional.of(element.date.compareTo(midpoint) < 0);
    }
}
