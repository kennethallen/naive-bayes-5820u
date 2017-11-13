package me.mostly.ml;

import me.mostly.ml.test.SVMTest;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SVMClassifier<E> implements BinaryClassifier<E> {

    public final ToDoubleBiFunction<? super E, ? super E> kernel;
    protected final List<Entry> data;
    protected double b;
    protected final double[][] kernelVals;

    public SVMClassifier(final ToDoubleBiFunction<? super E, ? super E> kernel,
                         final List<? extends E> examples,
                         final Classifier<E, Boolean> oracle) {
        this.kernel = kernel;

        data = examples.stream()
                .map(elem -> new AbstractMap.SimpleImmutableEntry<E, Optional<Boolean>>(elem, oracle.classify(elem)))
                .filter(e -> e.getValue().isPresent())
                .map(e -> new Entry(e.getKey(), e.getValue().get()))
                .collect(Collectors.toList());

        final int n = data.size();

        // Precompute kernel values.
        kernelVals = new double[n][];
        final Iterator<? extends E> iter1 = examples.iterator();
        for (int i = 0; i < n; i++) {
            kernelVals[i] = new double[i + 1];
            final E x_i = iter1.next();

            final Iterator<? extends E> iter2 = examples.iterator();
            for (int j = 0; j < kernelVals[i].length; j++) {
                kernelVals[i][j] = kernel.applyAsDouble(x_i, iter2.next());
            }
        }

        // maximize sum(α_i) - 0.5*sum(sum(α_i*α_j*y_i*y_j*kernel(x_i, x_j)))
        // subject to sum(α_i*y_i) = 0, and 0 <= α_i <= C

        // For boundary point x_i,
        // -b = sum(α_j*y_j*kernel(x_i, x_j)) - y_i
    }

    private double kernelVal(final int i, final int j) {
        return i >= j ? kernelVals[i][j] : kernelVals[j][i];
    }

    public void step(final double C) {
        final int a = ThreadLocalRandom.current().nextInt(data.size()), b;
        {
            final int bBase = ThreadLocalRandom.current().nextInt(data.size() - 1);
            b = bBase >= a ? bBase + 1 : bBase;
        }
        final Entry x_a = data.get(a), x_b = data.get(b);

        // Optimize alpha_a
        final double s = x_a.signedAlpha() + x_b.signedAlpha();
        x_a.alpha = ((x_a.cls == x_b.cls ? 0d : 2d) - x_a.label()*(
                        IntStream.range(0, data.size())
                                .filter(i -> i != a && i != b)
                                .mapToDouble(i -> data.get(i).signedAlpha()*(kernelVal(a, i) - kernelVal(b, i)))
                                .sum()
                        + s*(kernelVal(b, b) - kernelVal(a, b))
                )) / (kernelVal(a, a) + kernelVal(b, b) - 2d*kernelVal(a, b));

        // Enforce bounds
        x_a.alpha = Double.max(x_a.alpha, 0);
        x_a.alpha = Double.min(x_a.alpha, C);
        if (x_a.cls == x_b.cls) {
            x_a.alpha = Double.min(x_a.alpha, s); // Ensures alpha_b >= 0
            x_a.alpha = Double.max(x_a.alpha, -C + (x_a.cls ? s : -s)); // Ensures alpha_b <= C
        } else {
            x_a.alpha = Double.max(x_a.alpha, s); // Ensures alpha_b >= 0
            x_a.alpha = Double.min(x_a.alpha, C + (x_a.cls ? s : -s)); // Ensures alpha_b <= C
        }

        // Set alpha_b
        x_b.alpha = Math.abs(s - x_a.signedAlpha());

        data.stream().max(Comparator.comparing(e -> e.alpha)).ifPresent(boundary -> {
            this.b = boundary.label() - data.stream().mapToDouble(e -> e.score(boundary.entry)).sum();
        });
    }

    private double score(final E input) {
        return data.stream().mapToDouble(e -> e.score(input)).sum() + b;
    }

    @Override
    public Optional<Boolean> classify(final E input) {
        final double score = score(input);
        if (score < 0)
            return Optional.of(false);
        else if (score > 0)
            return Optional.of(true);
        else
            return Optional.empty();
    }

    protected class Entry {
        public final E entry;
        public final boolean cls;
        protected double alpha;

        public Entry(final E entry, final boolean cls) {
            this.entry = entry;
            this.cls = cls;
            alpha = 0;
        }

        public double label() {
            return cls ? 1d : -1d;
        }

        public double signedAlpha() {
            return cls ? alpha : -alpha;
        }

        public double score(final E test) {
            return signedAlpha() * kernel.applyAsDouble(entry, test);
        }

        @Override
        public String toString() {
            return "{x = " + entry + ", y = " + label() + ", α = " + alpha + "}";
        }
    }

    @Override
    public String toString() {
        return "{b = " + b + ", data:\n"
                + data.stream().map(Object::toString).reduce((a, b) -> a + ",\n" + b).orElse("") + "}";
    }


}