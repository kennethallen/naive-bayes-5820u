package me.mostly.ml;

import libsvm.*;
import me.mostly.ml.test.sp500.LabeledParams;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LibSVMClassifier<E> implements BinaryClassifier<E> {

    final svm_model model;
    final svm_parameter param;
    final int[] labels = new int[2];
    final Function<? super E, svm_node[]> featureExtractor;

    public LibSVMClassifier(final svm_parameter param, final Function<? super E, svm_node[]> featureExtractor,
                            final Collection<? extends E> train, final BinaryClassifier<? super E> oracle) {
        this(param, featureExtractor, createProblem(featureExtractor, train, oracle));
    }

    public LibSVMClassifier(final svm_parameter param, final Function<? super E, svm_node[]> featureExtractor,
                            final svm_problem prob) {
        this.featureExtractor = featureExtractor;
        this.param = param;

        // Train model.
        Optional.ofNullable(svm.svm_check_parameter(prob, param)).ifPresent(message -> {
            throw new IllegalArgumentException("Parameter problem: " + message);
        });
        model = svm.svm_train(prob, param);
        svm.svm_get_labels(model, this.labels);
    }

    public static <E> svm_problem createProblem(final Function<? super E, svm_node[]> featureExtractor,
                                            final Collection<? extends E> train, final BinaryClassifier<? super E> oracle) {
        final List<? extends Map.Entry<E, Boolean>> labeledTrainers = train.stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e, oracle.classify(e)))
                .filter(entry -> entry.getValue().isPresent())
                .map(entry -> new AbstractMap.SimpleImmutableEntry<E, Boolean>(entry.getKey(), entry.getValue().get()))
                .collect(Collectors.toList());

        final svm_problem prob = new svm_problem();
        prob.l = labeledTrainers.size();
        prob.x = labeledTrainers.stream().map(Map.Entry::getKey).map(featureExtractor).toArray(svm_node[][]::new);
        prob.y = labeledTrainers.stream().map(Map.Entry::getValue).mapToDouble(b -> b ? 1 : 0).toArray();
        return prob;
    }

    @Override
    public Optional<Boolean> classify(E input) {
        return Optional.of((int) Math.round(svm.svm_predict(model, featureExtractor.apply(input))) == 1);
    }

    @Override
    public String toString() {
        if (param instanceof LabeledParams)
            return param.toString();
        else
            return super.toString();
    }
}
