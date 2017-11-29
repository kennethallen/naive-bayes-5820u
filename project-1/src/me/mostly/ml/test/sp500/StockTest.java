package me.mostly.ml.test.sp500;

import me.mostly.ml.BinaryClassifier;
import me.mostly.ml.test.BinaryTest;

import java.util.Comparator;
import java.util.OptionalDouble;
import java.util.SortedSet;
import java.util.TreeSet;

public class StockTest extends BinaryTest<Stock> {

    final SortedSet<Stock> picks = new TreeSet<>(Comparator.comparing(s -> s.ticker));
    final int evalStart, evalEnd;

    public StockTest(BinaryClassifier<Stock> oracle, BinaryClassifier<Stock> classifier, final int evalStart, final int evalEnd) {
        super(oracle, classifier);
        this.evalStart = evalStart;
        this.evalEnd = evalEnd;
    }

    @Override
    protected void onResult(Stock element, boolean actual, boolean predicted) {
        super.onResult(element, actual, predicted);
        if (predicted)
            picks.add(element);
    }

    public double averageReturn() {
        return picks.stream().mapToDouble(s -> s.calcReturn(evalStart, evalEnd)).average().orElse(1);
    }
}
