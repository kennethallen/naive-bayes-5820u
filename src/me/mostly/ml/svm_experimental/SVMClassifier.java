package me.mostly.ml.svm_experimental;

import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.Classifier;

import java.util.*;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SVMClassifier<E> implements BinaryClassifier<E> {

    public final ToDoubleBiFunction<? super E, ? super E> kernel;
    protected final List<Entry> data;
    protected double b;

    public SVMClassifier(final ToDoubleBiFunction<? super E, ? super E> kernel,
                         final List<? extends E> examples,
                         final Classifier<E, Boolean> oracle) {
        this.kernel = kernel;

        data = examples.stream()
                .map(elem -> new AbstractMap.SimpleImmutableEntry<E, Optional<Boolean>>(elem, oracle.classify(elem)))
                .filter(e -> e.getValue().isPresent())
                .map(e -> new Entry(e.getKey(), e.getValue().get()))
                .collect(Collectors.toList());
    }

    public void optimize(final double C, final double eps) {
        new Optimizer(C, eps).optimize();
    }

    class Optimizer {
        final double C, eps;
        final double[][] kernelVals;

        public Optimizer(double c, double eps) {
            C = c;
            this.eps = eps;

            // Precompute kernel values.
            kernelVals = new double[data.size()][];
            for (final ListIterator<Entry> li = data.listIterator(); li.hasNext();) {
                final int i = li.nextIndex();
                final Entry x_i = li.next();

                kernelVals[i] = new double[i + 1];
                for (int j = 0; j <= i; j++) {
                    kernelVals[i][j] = kernel.applyAsDouble(x_i.datum, data.get(j).datum);
                }
            }
        }

        double kernelVal(final int i, final int j) {
            return i >= j ? kernelVals[i][j] : kernelVals[j][i];
        }

        void optimizePair(final int aIdx, final int bIdx) {
            final Entry a = data.get(aIdx), b = data.get(bIdx);
            final double L_b, H_b;
            if (a.cls == b.cls) {
                L_b = Double.max(0, b.alpha + a.alpha - C);
                H_b = Double.min(C, b.alpha + a.alpha);
            } else {
                L_b = Double.max(0, b.alpha - a.alpha);
                H_b = Double.min(C, C + b.alpha - a.alpha);
            }

            final double a_alphaOld = a.alpha, b_alphaOld = b.alpha,
                    E_a = a.error(), E_b = b.error(),
                    K_aa = kernelVal(aIdx, aIdx), K_bb = kernelVal(bIdx, bIdx), K_ab = kernelVal(aIdx, bIdx);
            b.alpha = Double.max(L_b, Double.min(H_b,
                    b.alpha + (b.cls ? E_a - E_b : E_b - E_a) / (K_aa + K_bb - 2*K_ab)));
            a.alpha += a.cls == b.cls ? b.alpha - b_alphaOld : b_alphaOld - b.alpha;

            SVMClassifier.this.b += 0.5*(E_a + E_b
                    + (a.cls ? a.alpha - a_alphaOld : a_alphaOld - a.alpha) * (K_aa + K_ab)
                    + (b.cls ? b.alpha - b_alphaOld : b_alphaOld - b.alpha) * (K_ab + K_bb));
        }

        void optimize() {
            while (!step()) {
                System.out.println(SVMClassifier.this);
            }
        }

        boolean step() {
            final Set<Integer> nonbound = new HashSet<>(data.size());

            boolean allValid = true;
            for (final ListIterator<Entry> li = data.listIterator(); li.hasNext();) {
                final int aIdx = li.nextIndex();
                final Entry a = li.next();

                final double discriminant = a.cls ? score(a.datum) : -score(a.datum);
                final boolean valid;
                if (a.alpha > 0) {
                    if (a.alpha < C) {
                        nonbound.add(aIdx);
                        valid = Math.abs(discriminant - 1) <= eps;
                    } else {
                        valid = discriminant <= 1 + eps;
                    }
                } else {
                    valid = discriminant >= 1 - eps;
                }

                if (!valid) {
                    allValid = false;
                    IntStream.range(aIdx + 1, data.size())
                            .filter(i -> i != aIdx)
                            .forEach(bIdx -> optimizePair(aIdx, bIdx));
                }
            }
            if (allValid)
                return true;

            do {
                allValid = true;

                for (final int aIdx : nonbound) {
                    final Entry a = data.get(aIdx);

                    final double discriminant = a.cls ? score(a.datum) : -score(a.datum);
                    final boolean valid;
                    if (a.alpha > 0) {
                        if (a.alpha < C) {
                            valid = Math.abs(discriminant - 1) <= eps;
                        } else {
                            valid = discriminant <= 1 + eps;
                        }
                    } else {
                        valid = discriminant >= 1 - eps;
                    }

                    if (!valid) {
                        allValid = false;
                        nonbound.stream()
                                .filter(i -> i != aIdx)
                                .forEach(bIdx -> optimizePair(aIdx, bIdx));
                    }
                }
            } while (!allValid);

            return false;
        }
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
        public final E datum;
        public final boolean cls;
        protected double alpha;

        public Entry(final E datum, final boolean cls) {
            this.datum = datum;
            this.cls = cls;
            alpha = 0;
        }

        public double label() {
            return cls ? 1d : -1d;
        }
        public double error() {
            return score(datum) - label();
        }

        public double signedAlpha() {
            return cls ? alpha : -alpha;
        }

        public double score(final E test) {
            return signedAlpha() * kernel.applyAsDouble(datum, test);
        }

        @Override
        public String toString() {
            return "{x = " + datum + ", y = " + label() + ", α = " + alpha + "}";
        }
    }

    @Override
    public String toString() {
        return "{b = " + b + ", data:\n"
                + data.stream().map(Object::toString).reduce((a, b) -> a + ",\n" + b).orElse("") + "}";
    }


}