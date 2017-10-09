package me.mostly.ml;

import java.util.Collection;

public class NormalDistribution {

    public <T extends Collection<? extends Number>> NormalDistribution(final T sample) {
        final float n = sample.size();
        {
            float sum = 0;
            for (final Number x : sample) {
                sum += x.floatValue();
            }
            mean = sum / n;
        }
        {
            float variance = 0;
            for (final Number x : sample) {
                float delta = x.floatValue() - mean;
                variance += delta * delta;
            }
            variance /= n - 1;

            factor = 1 / ((float) Math.sqrt(2 * Math.PI * variance));
            expFactor = -0.5f / variance;
        }
    }

    final float mean, factor, expFactor;

    float probDensity(float freq) {
        freq -= mean;
        return factor * ((float) Math.exp(expFactor * (freq * freq)));
    }

}