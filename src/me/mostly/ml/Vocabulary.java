package me.mostly.ml;

import java.util.HashMap;

public class Vocabulary {

    private final HashMap<String, Integer> idLookup = new HashMap<>();
    private final HashMap<Integer, String> wordLookup = new HashMap<>();

    public int size() {
        return wordLookup.size();
    }

    public int getId(final String s) {
        return idLookup.computeIfAbsent(s, word -> {
            final Integer id = size();
            wordLookup.put(id, word);
            return id;
        });
    }

    public String getWord(final int id) {
        return wordLookup.get(id);
    }

}
