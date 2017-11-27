package re;

import java.util.ArrayList;

public class Stock {

    public final String ticker;
    public final double[] returns;

    public Stock(String ticker, int numEntries) {
        this.ticker = ticker;
        this.returns = new double[numEntries];
    }
}
