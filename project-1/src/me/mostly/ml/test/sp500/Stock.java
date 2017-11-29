package me.mostly.ml.test.sp500;

import java.util.stream.IntStream;

import libsvm.svm_node;

public class Stock {

    public final String ticker;
    public final double[] returns;

    public Stock(String ticker, double[] returns) {
        this.ticker = ticker;
        this.returns = returns;
    }

    public svm_node[] libSvmFormat() {
        return libSvmFormat(0, returns.length);
    }

    public svm_node[] libSvmFormat(final int start, final int end) {
        return IntStream.range(start, end)
                .mapToObj(idx -> {
                    final svm_node node = new svm_node();
                    node.index = idx;
                    node.value = returns[idx];
                    return node;
                }).toArray(svm_node[]::new);
    }

    public double similarity(final Stock s) {
        double total = 0;
        for (int i = 0; i < returns.length; i++) {
            total += returns[i] * s.returns[i];
        }
        return total;
    }

    public double calcReturn() {
        return calcReturn(0);
    }

    public double calcReturn(final int start) {
        return calcReturn(start, returns.length);
    }

    public double calcReturn(final int start, final int end) {
        return IntStream.range(start, end)
                .mapToDouble(i -> 1 + returns[i])
                .reduce(1, (a, b) -> a * b);
    }

    @Override
    public String toString() {
        return ticker;
    }
}