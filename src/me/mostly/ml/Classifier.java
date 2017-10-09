package me.mostly.ml;

import java.util.Optional;

public interface Classifier<Element, Class> {

    Optional<Class> classify(Element input);

}