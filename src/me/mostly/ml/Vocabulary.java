package me.mostly.ml;

import java.util.HashMap;
import java.util.IdentityHashMap;

public class Vocabulary {

    public final IdentityHashMap<String, Integer> idLookup = new IdentityHashMap<>();
    public final HashMap<Integer, String> wordLookup = new HashMap<>();

    public int size() {
        return wordLookup.size();
    }

    public int getId(String s) {
        s = s.intern();
        return idLookup.computeIfAbsent(s, word -> {
            final Integer id = size();
            wordLookup.put(id, word);
            return id;
        });
    }

    public String getWord(final Integer id) {
        return wordLookup.get(id);
    }

}
