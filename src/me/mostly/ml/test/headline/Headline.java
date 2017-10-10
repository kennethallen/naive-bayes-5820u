package me.mostly.ml.test.headline;

import me.mostly.ml.Vocabulary;
import me.mostly.ml.WordBag;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

class Headline extends WordBag {

    final Date date;
    final String string, source;

    Headline(Vocabulary vocab, Date date, String string, String source) {
        super(vocab);
        this.date = date;
        this.string = string;
        extractAndAddWords(string);
        this.source = source;
    }

    @Override
    public String toString() {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return '\"' + string + "\" (" + DateFormat.getDateInstance().format(date) + ')';
    }
}