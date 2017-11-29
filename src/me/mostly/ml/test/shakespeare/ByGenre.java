package me.mostly.ml.test.shakespeare;

import me.mostly.ml.Classifier;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ByGenre implements Classifier<Play, Genre> {

    @Override
    public List<? extends Genre> allClasses() {
        return Arrays.asList(Genre.values());
    }

    @Override
    public Optional<Genre> classify(Play input) {
        return Genre.forTitle(input.title);
    }
}
