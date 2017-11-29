package me.mostly.ml;

import libsvm.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LibSVMClassifier<E> implements BinaryClassifier<E> {

    final svm_model model;
    final int[] labels = new int[2];
    final Function<? super E, svm_node[]> featureExtractor;

    public LibSVMClassifier(final svm_parameter param, final Function<? super E, svm_node[]> featureExtractor,
                            final Collection<? extends E> train, final BinaryClassifier<? super E> oracle) {
        this.featureExtractor = featureExtractor;

        // Setup training data for LibSVM.
        final List<? extends Map.Entry<E, Boolean>> labeledTrainers = train.stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e, oracle.classify(e)))
                .filter(entry -> entry.getValue().isPresent())
                .map(entry -> new AbstractMap.SimpleImmutableEntry<E, Boolean>(entry.getKey(), entry.getValue().get()))
                .collect(Collectors.toList());

        final svm_problem prob = new svm_problem();
        prob.l = labeledTrainers.size();
        prob.x = labeledTrainers.stream().map(Map.Entry::getKey).map(featureExtractor).toArray(svm_node[][]::new);
        prob.y = labeledTrainers.stream().map(Map.Entry::getValue).mapToDouble(b -> b ? 1 : 0).toArray();

        // Train model.
        Optional.ofNullable(svm.svm_check_parameter(prob, param)).ifPresent(message -> {
            throw new IllegalArgumentException("Parameter problem: " + message);
        });
        model = svm.svm_train(prob, param);
        svm.svm_get_labels(model, this.labels);
    }

    @Override
    public Optional<Boolean> classify(E input) {
        return Optional.of((int) Math.round(svm.svm_predict(model, featureExtractor.apply(input))) == 1);
    }

}
