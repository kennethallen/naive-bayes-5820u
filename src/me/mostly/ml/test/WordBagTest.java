package me.mostly.ml.test;

import me.mostly.ml.BayesianClassifier;
import me.mostly.ml.Vocabulary;
import me.mostly.ml.WordBag;
import me.mostly.ml.WordBagModel;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class WordBagTest<E extends WordBag, C> {

    public final Vocabulary vocab;

    public final Oracle<? super E, ? extends C> oracle;
    public final BayesianClassifier<WordBag, C, WordBagModel> classifier;

    protected final SortedMap<C, SortedMap<C, Integer>> results;

    public WordBagTest(final Vocabulary vocab, Oracle<? super E, ? extends C> oracle) {
        this.vocab = vocab;
        this.oracle = oracle;

        classifier = new BayesianClassifier<>();
        oracle.allClasses().forEach(cls -> classifier.put(cls, new WordBagModel(vocab)));

        final SortedMap<C, SortedMap<C, Integer>> results = new TreeMap<>();
        oracle.allClasses().forEach(c -> results.put(c, Collections.synchronizedSortedMap(new TreeMap<>())));
        this.results = Collections.unmodifiableSortedMap(results);
    }

    public void learn(final E element) {
        oracle.classify(element).ifPresent(cls ->
                classifier.get(cls).learnExample(element));
    }

    public void unlearn(final E element) {
        oracle.classify(element).ifPresent(cls ->
                classifier.get(cls).unlearnExample(element));
    }

    public void test(final E element) {
        oracle.classify(element).ifPresent(real ->
                classifier.classify(element).ifPresent(guess ->
                        results.get(real).merge(guess, 1, (i, one) -> i + 1)));
    }

    public int successes() {
        return results.entrySet().stream().mapToInt(e -> e.getValue().getOrDefault(e.getKey(), 0)).sum();
    }

    public int totalTrials() {
        return results.values().stream().flatMap(r -> r.values().stream()).mapToInt(Integer::intValue).sum();
    }

    @Override
    public String toString() {
        return "Oracle: " + oracle + "\n"
                + "Classifier: " + classifier + "\n"
                + "\n"
                + stats();
    }

    public String stats() {
        final int successes = successes(), total = totalTrials();
        return total + " total, " + successes + " successes, " + (total - successes) + " failures\n"
                + "Accuracy: " + (((double) successes)/total) + "\n"
                + "\n"
                + "Real classes -> how their members were classified:\n"
                + results.entrySet().stream()
                .map(Object::toString)
                .reduce("", (a, b) -> a + '\n' + b);
    }
}