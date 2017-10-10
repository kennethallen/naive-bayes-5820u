package me.mostly.ml;

import java.math.BigDecimal;
import java.util.*;

public class WordBagModel extends BaseLearningModel<WordBag> {

    public final WordBag wordBag;
    protected final HashMap<Integer, BigDecimal> wordProbs = new HashMap<>();

    public WordBagModel(Vocabulary vocab) {
        wordBag = new WordBag(vocab);
    }

    @Override
    public void learnExample(final WordBag text) {
        super.learnExample(text);
        wordBag.addBag(text);
        wordProbs.clear();
    }

    public void unlearnExample(final WordBag text) {
        examples = examples.subtract(BigDecimal.ONE, PRECISION);
        wordBag.removeBag(text);
        wordProbs.clear();
    }

    private BigDecimal getWordProb(final Integer word) {
        return wordProbs.computeIfAbsent(word, this::calcWordProb);
    }

    private BigDecimal calcWordProb(final Integer word) {
        return new BigDecimal(wordBag.wordCounts.getOrDefault(word, 0) + 1, PRECISION)
                .divide(new BigDecimal(wordBag.totalCount + wordBag.vocab.size(), PRECISION), PRECISION);
    }

    @Override
    public BigDecimal score(final WordBag text) {
        return text.wordCounts.entrySet().stream()
                .map(e -> getWordProb(e.getKey()).pow(e.getValue(), PRECISION))
                .reduce(BigDecimal.ONE, (a, b) -> a.multiply(b, PRECISION))
                .multiply(super.score(text), PRECISION);
    }

    public String info() {
        return examples + " examples\n" + wordBag.totalCount + " total words\n\n" + wordBag;
    }

}