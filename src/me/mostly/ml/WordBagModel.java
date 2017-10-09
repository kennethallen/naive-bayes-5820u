package me.mostly.ml;

import java.math.BigDecimal;
import java.util.*;

public class WordBagModel extends BaseLearningModel<WordBag> {

    protected final Vocabulary vocab;
    protected final Map<Integer, Integer> wordCounts = new HashMap<>();
    protected int totalCount;

    public WordBagModel(Vocabulary vocab) {
        this.vocab = vocab;
    }

    @Override
    public void learnExample(final WordBag text) {
        super.learnExample(text);
        text.wordCounts.forEach((word, freq) -> wordCounts.merge(word, freq, (a, b) -> a + b));
        totalCount += text.totalCount;
    }

    public void unlearnExample(final WordBag text) {
        examples = examples.subtract(BigDecimal.ONE, PRECISION);
        text.wordCounts.forEach((word, freq) -> wordCounts.compute(word, (w, oldFreq) -> oldFreq - freq));
        totalCount -= text.totalCount;
    }

    private BigDecimal wordProb(final int word) {
        return new BigDecimal(wordCounts.getOrDefault(word, 0) + 1, PRECISION)
                .divide(new BigDecimal(totalCount + vocab.size(), PRECISION), PRECISION);
    }

    @Override
    public BigDecimal score(final WordBag text) {
        return text.wordCounts.entrySet().stream()
                .map(e -> wordProb(e.getKey()).pow(e.getValue(), PRECISION))
                .reduce(BigDecimal.ONE, (a, b) -> a.multiply(b, PRECISION))
                .multiply(super.score(text), PRECISION);
    }

    public String info() {
        final StringBuilder sb = new StringBuilder();
        sb.append(examples).append(" examples\n").append(totalCount).append(" total words");

        wordCounts.entrySet().stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<String, Integer>(vocab.getWord(e.getKey()), e.getValue()))
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> sb.append('\n').append(e));

        return sb.toString();
    }

}