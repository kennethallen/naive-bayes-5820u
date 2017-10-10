package me.mostly.ml;

import java.util.*;

public class BayesianClassifier<E, C, M extends Model<? super E>>
        extends HashMap<C, M> implements Classifier<E, C> {

    @Override
    public Collection<? extends C> allClasses() {
        return keySet();
    }

    @Override
    public Optional<C> classify(final E input) {
        return entrySet().parallelStream()
                .map(e -> new SimpleImmutableEntry<>(e.getKey(), e.getValue().score(input)))
//                .peek(System.out::println)
                .max(Map.Entry.comparingByValue())
                .map(Entry::getKey);
    }

}