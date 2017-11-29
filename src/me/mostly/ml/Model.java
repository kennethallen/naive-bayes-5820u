package me.mostly.ml;

import java.math.BigDecimal;

public interface Model<Element> {

    BigDecimal score(Element e);

}