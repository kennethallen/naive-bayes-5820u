package me.mostly.ml.test.headline;

import me.mostly.ml.BinaryClassifier;

import java.util.Optional;

class Crowdsourced implements BinaryClassifier<Headline> {

    @Override
    public Optional<Boolean> classify(Headline input) {
        return Optional.of(input.source.equals("The Examiner"));
    }
}
