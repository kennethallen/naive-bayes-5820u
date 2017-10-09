package me.mostly.ml.test.shakespeare;

import java.util.*;

enum Genre {
    Comedy("A Comedy of Errors", "A Midsummer nights dream", "A Winters Tale", "Alls well that ends well",
            "As you like it", "Loves Labours Lost", "Measure for measure", "Merchant of Venice",
            "Merry Wives of Windsor", "Much Ado about nothing", "Taming of the Shrew", "The Tempest",
            "Twelfth Night", "Two Gentlemen of Verona"),
    Tragedy("Antony and Cleopatra", "Coriolanus", "Cymbeline", "Hamlet", "Julius Caesar", "King Lear", "macbeth",
            "Othello", "Romeo and Juliet", "Timon of Athens", "Titus Andronicus", "Troilus and Cressida"),
    History("Henry IV", "Henry V", "Henry VI Part 1", "Henry VI Part 2", "Henry VI Part 3", "Henry VIII",
            "King John", "Pericles", "Richard II", "Richard III");

    final Set<String> titles;

    Genre(String... titles) {
        this.titles = new HashSet<>(Arrays.asList(titles));
    }

    private static final Map<String, Genre> forTitle = new HashMap<>();
    static {
        for (final Genre genre : Genre.values()) {
            genre.titles.forEach(title -> forTitle.put(title, genre));
        }
    }

    public static Optional<Genre> forTitle(final String title) {
        return Optional.ofNullable(forTitle.get(title));
    }
}
