package me.mostly.ml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordBag {

    public static final Pattern WORD_PATTERN =
            Pattern.compile("\\b\\p{Alpha}+([-']\\p{Alpha}+)*\\b", Pattern.UNICODE_CHARACTER_CLASS);

    public static final Set<String> STOP_WORDS = new HashSet<>();
    static {
        try (final BufferedReader br = new BufferedReader(new FileReader("data/stopwords.txt"))) {
            String word;
            while ((word = br.readLine()) != null)
                STOP_WORDS.add(word);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final Vocabulary vocab;
    public final Map<Integer, Integer> wordCounts = new HashMap<>();
    public int totalCount = 0;

    public WordBag(final Vocabulary vocab) {
        this.vocab = vocab;
    }

    public void addWord(final String word) {
        if (!STOP_WORDS.contains(word)) {
            wordCounts.merge(vocab.getId(word), 1, (i, one) -> i + 1);
            ++totalCount;
        }
    }

    public void addBag(final WordBag bag) {
        bag.wordCounts.forEach((word, freq) -> wordCounts.merge(word, freq, (a, b) -> a + b));
        totalCount += bag.totalCount;
    }

    public void removeBag(final WordBag bag) {
        bag.wordCounts.forEach((word, freq) -> wordCounts.compute(word, (w, oldFreq) -> oldFreq - freq));
        totalCount -= bag.totalCount;
    }

    public void extractAndAddWords(final CharSequence str) {
        final Matcher matcher = WORD_PATTERN.matcher(str);
        while (matcher.find()) {
            addWord(matcher.group().toLowerCase());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Processed text [total ").append(totalCount).append(" words]");
        wordCounts.entrySet().stream()
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(vocab.getWord(e.getKey()), e.getValue()))
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> sb.append('\n').append(e));
        return sb.toString();
    }
}