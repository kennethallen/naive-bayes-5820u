package me.mostly.ml;

import java.math.BigDecimal;
import java.math.MathContext;

public class BaseLearningModel<Element> implements Model<Element> {

    public static final MathContext PRECISION = MathContext.DECIMAL32;

    protected BigDecimal examples = BigDecimal.ZERO;

    @Override
    public BigDecimal score(final Element e) {
        return examples;
    }

    public void learnExample(final Element e) {
        examples = examples.add(BigDecimal.ONE, PRECISION);
    }
}