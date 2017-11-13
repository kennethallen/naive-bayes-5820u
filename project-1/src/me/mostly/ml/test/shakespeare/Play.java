package me.mostly.ml.test.shakespeare;

import me.mostly.ml.Vocabulary;
import me.mostly.ml.WordBag;

public class Play extends WordBag {

    public final String title;

    public Play(Vocabulary vocab, String title) {
        super(vocab);
        this.title = title;
    }
}
