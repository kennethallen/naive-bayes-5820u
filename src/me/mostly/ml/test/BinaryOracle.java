package me.mostly.ml.test;

import java.util.Arrays;
import java.util.List;

public interface BinaryOracle<E> extends Oracle<E, Boolean> {

    @Override
    default List<? extends Boolean> allClasses() {
        return Arrays.asList(true, false);
    }
}
