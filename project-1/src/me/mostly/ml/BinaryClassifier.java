package me.mostly.ml;

import java.util.Arrays;
import java.util.List;

public interface BinaryClassifier<E> extends Classifier<E, Boolean> {

    @Override
    default List<? extends Boolean> allClasses() {
        return Arrays.asList(true, false);
    }

}