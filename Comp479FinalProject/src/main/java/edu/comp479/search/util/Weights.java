package edu.comp479.search.util;

import java.util.function.Function;

import edu.comp479.search.index.structure.Posting;

public final class Weights {

    private Weights() {
    }

    public static Function<Posting, Float> tfIdf(float termIdf) {
        return new Function<Posting, Float>() {
            @Override
            public Float apply(Posting p) {
                double tf = p.getTermFreq();
                double logTf;
                if (tf == 0) {
                    logTf = 0;
                } else {
                    logTf = Math.log10(1 + tf);
                }
                return (float) (logTf * termIdf);
            }
        };
    }
}
