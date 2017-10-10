package me.mostly.ml.test;

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
        final SortedMap<C, TopTen> tops = new TreeMap<>();
        oracle.allClasses().forEach(c -> tops.put(c, new TopTen(c)));
        vocab.wordLookup.keySet().forEach(word -> tops.values().forEach(top -> top.consider(word)));

        return tops.values().stream().map(t -> "\n\n" + t).reduce("", String::concat);
    }
    private class TopTen {
        final C cls;
        final ScoredWord[] top = new ScoredWord[10];
        int count = 0;

        public TopTen(C cls) {
            this.cls = cls;
        }

        private int classCount(final Integer word) {
            return classifier.get(cls).wordBag.wordCounts.getOrDefault(word,0);
        }
        private int otherCount(final Integer word) {
            return classifier.entrySet().stream()
                    .filter(e -> !e.getKey().equals(cls))
                    .map(Map.Entry::getValue)
                    .mapToInt(i -> i.wordBag.wordCounts.getOrDefault(word, 0))
                    .sum();
        }

        public void consider(Integer word) {
            final float score = (classCount(word) + 1) / (otherCount(word) + classifier.allClasses().size() - 1);

            int newIdx = count;
            while (newIdx > 0 && score > top[newIdx - 1].score)
                newIdx--;

            if (newIdx < top.length) {
                System.arraycopy(top, newIdx, top, newIdx + 1, top.length - 1 - newIdx);
                top[newIdx] = new ScoredWord(word, score);
                if (count < top.length)
                    count++;
            }
        }

        @Override
        public String toString() {
            return "Most indicative words for class " + cls + ":\n"
                    + IntStream.range(0, count)
                            .mapToObj(i -> {
                                final Integer word = top[i].word;
                                return i + ": " + vocab.getWord(word)
                                        + " (" + classCount(word) + " in class, " + otherCount(word) + " out of class)";
                            })
                            .reduce("", (a, b) -> a + '\n' + b);
        }
    }
    private static class ScoredWord {
        final float score;
        final Integer word;

        public ScoredWord(Integer word, float score) {
            this.word = word;
            this.score = score;
        }
    }
}