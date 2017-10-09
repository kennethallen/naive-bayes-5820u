package me.mostly.ml.test;

import me.mostly.ml.Classifier;

import java.util.List;
import java.util.Optional;

public interface Oracle<E, C> extends Classifier<E, C> {

    List<? extends C> allClasses();

}