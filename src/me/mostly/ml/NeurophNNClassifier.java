package me.mostly.ml;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.data.DataSet;
import org.neuroph.nnet.MultiLayerPerceptron;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

public class NeurophNNClassifier<E> implements BinaryClassifier<E> {

    final NeuralNetwork nn;
    final Function<? super E, double[]> featureExtractor;
    final double sensitivity = 0.5;

    public NeurophNNClassifier(Function<? super E, double[]> featureExtractor,
                               final DataSet trainingSet, final int... layerSizes) {
        this.featureExtractor = featureExtractor;

        nn = new MultiLayerPerceptron(layerSizes);
        nn.learn(trainingSet);
    }

    @Override
    public Optional<Boolean> classify(E input) {
        nn.setInput(featureExtractor.apply(input));
        nn.calculate();
        return Optional.of(nn.getOutput()[0] > sensitivity);
    }

    private static final double[] NEGATIVE_LABEL = {0}, POSITIVE_LABEL = {1};

    public static <E> DataSet createDataSet(final int inputDimension, final Function<? super E, double[]> featureExtractor,
                                            final Collection<? extends E> train, final BinaryClassifier<? super E> oracle) {
        final DataSet data = new DataSet(inputDimension);
        train.stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e, oracle.classify(e)))
                .filter(entry -> entry.getValue().isPresent())
                .map(entry -> new AbstractMap.SimpleImmutableEntry<E, Boolean>(entry.getKey(), entry.getValue().get()))
                .forEach(e -> data.addRow(
                        featureExtractor.apply(e.getKey()), e.getValue() ? POSITIVE_LABEL : NEGATIVE_LABEL));
        return data;
    }

    public void save(final String path) {
        nn.save(path);
    }

    public NeurophNNClassifier(Function<? super E, double[]> featureExtractor, final File path) {
        this.featureExtractor = featureExtractor;
        this.nn = NeuralNetwork.createFromFile(path);
    }
}
