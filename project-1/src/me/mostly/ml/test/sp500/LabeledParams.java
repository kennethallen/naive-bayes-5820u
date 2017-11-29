package me.mostly.ml.test.sp500;

import libsvm.svm_parameter;

public class LabeledParams extends svm_parameter {
    String notability;

    protected LabeledParams(final double C, final int kernelType, String kernelName) {
        notability = "C=" + C + ", " + kernelName + " kernel";

        svm_type = C_SVC;
        this.C = C;
        eps = 0.001;

        cache_size = 20000;
        probability = 0;
        shrinking = 0;

        kernel_type = kernelType;
    }

    public LabeledParams(final double C) {
        this(C, LINEAR, "linear");
    }

    public LabeledParams(final double C, final double gamma) {
        this(C, RBF, "RBF");
        this.gamma = gamma;
        this.notability += " (gamma=" + gamma + ")";
    }

    public LabeledParams(final double C, final double gamma, final double coef0) {
        this(C, SIGMOID, "sigmoid");
        this.gamma = gamma;
        this.coef0 = coef0;
        this.notability += " (gamma=" + gamma + ", coef0=" + coef0 + ")";
    }

    public LabeledParams(final double C, final double gamma, final double coef0, final int degree) {
        this(C, POLY, "poly");
        this.gamma = gamma;
        this.coef0 = coef0;
        this.degree = degree;
        this.notability += " (gamma=" + gamma + ", coef0=" + coef0 + ", degree=" + degree + ")";
    }

    @Override
    public String toString() {
        return notability;
    }
}