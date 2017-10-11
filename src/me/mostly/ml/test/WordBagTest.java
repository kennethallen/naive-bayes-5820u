package me.mostly.ml.test;

import me.mostly.collection.Toplist;
import me.mostly.ml.*;

import java.util.*;
import java.util.stream.IntStream;

public class WordBagTest<E extends WordBag, C> {

    public final Vocabulary vocab;

    public final Classifier<? super E, ? extends C> oracle;
    public final BayesianClassifier<WordBag, C, WordBagModel> classifier;

    protected final SortedMap<C, SortedMap<C, Integer>> results;

    public WordBagTest(final Vocabulary vocab, Classifier<? super E, ? extends C> oracle) {
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
                + stats()
                + mostPredictiveWords();
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

    public String mostPredictiveWords() {
        final SortedMap<C, Toplist<ScoredWord>> tops = new TreeMap<>();
        oracle.allClasses().forEach(c -> tops.put(c, new Toplist<>(10)));
        vocab.wordLookup.keySet().forEach(word -> tops.forEach((cls, top) -> top.add(new ScoredWord(word, cls))));

        return tops.entrySet().stream()
                .map(e -> "\n\nMost indicative words for class " + e.getKey() + ":\n"
                        + e.getValue().stream()
                        .map(Object::toString)
                        .reduce("", (a, b) -> a + '\n' + b))
                .reduce("", String::concat);
    }

    private class ScoredWord implements Comparable<ScoredWord> {
        final int classCount, otherCount;
        final float score;
        final Integer word;

        public ScoredWord(Integer word, C cls) {
            this.word = word;
            classCount = classifier.get(cls).wordBag.wordCounts.getOrDefault(word,0);;
            otherCount = classifier.entrySet().stream()
                    .filter(e -> !e.getKey().equals(cls))
                    .map(Map.Entry::getValue)
                    .mapToInt(i -> i.wordBag.wordCounts.getOrDefault(word, 0))
                    .sum();
            score = (1f + classCount) / (classifier.allClasses().size() - 1 + otherCount);
        }

        @Override
        public int compareTo(ScoredWord w) {
            final int comp = Float.compare(score, w.score);
            return comp != 0 ? comp : word.compareTo(w.word);
        }

        @Override
        public String toString() {
            return vocab.getWord(word) + " (" + classCount + " in class, " + otherCount + " out of class)";
        }
    }
}