package me.mostly.ml.test;

import me.mostly.ml.BayesianClassifier;
import me.mostly.ml.Vocabulary;
import me.mostly.ml.WordBag;
import me.mostly.ml.WordBagModel;

public class WordBagBinaryTest<E extends WordBag> extends WordBagTest<E, Boolean> {

    public WordBagBinaryTest(Vocabulary vocab, BinaryOracle<? super E> oracle) {
        super(vocab, oracle);
    }

    public int truePositives() {
        return results.get(true).getOrDefault(true, 0);
    }
    public int falsePositives() {
        return results.get(false).getOrDefault(true, 0);
    }
    public int falseNegatives() {
        return results.get(true).getOrDefault(false, 0);
    }
    public int trueNegatives() {
        return results.get(false).getOrDefault(false, 0);
    }

    @Override
    public String stats() {
        final int truePos = truePositives(), falsePos = falsePositives(),
                falseNeg = falseNegatives(), trueNeg = trueNegatives(),
                total = totalTrials();
        final double tpr, fpr, fnr, tnr, npv, ppv, lrPlus, lrMinus;

        return "Actual prevalence:                        "
                + ((double) truePos + falseNeg)/total + " (RealPos/Total)\n"
                + "\n"
                + "=== RESULTS ===\n"
                + "True positives (TP):                      " + truePos + '\n'
                + "False positives (FP):                     " + falsePos + '\n'
                + "False negatives (FN):                     " + falseNeg + '\n'
                + "True negatives (TN):                      " + trueNeg + '\n'
                + "\n"
                + "=== DESIRABLE STATS (0 to 1) ===\n"
                + "Accuracy:                                 "
                        + (((double) (truePos + trueNeg)) / total) + " (accuracy=(TP+TN)/Total)\n"
                + "True positive rate (Recall, Sensitivity): "
                        + (tpr = (((double) truePos) / (truePos + falseNeg))) + " (TPR=TP/RealPos)\n"
                + "True negative rate (Specificity):         "
                        + (tnr = (((double) trueNeg) / (falsePos + trueNeg))) + " (TNR=TN/RealNeg)\n"
                + "Positive predictive value (Precision):    "
                        + (ppv = (((double) truePos) / (truePos + falsePos))) + " (PPV=TP/PredictedPos)\n"
                + "Negative predictive value:                "
                        + (npv = (((double) trueNeg) / (trueNeg + falseNeg))) + " (NPV=TN/PredictedNeg)\n"
                + "Area under convex hull ROC curve:         "
                        + 0.5*(tpr + tnr) + " (area=(TPR+TNR)/2)\n"
                + "F1 score:                                 "
                        + (2/(1/tpr + 1/ppv)) + " (F1=2/(1/TPR + 1/PPV))\n"
                + "F1 score (negative condition):            "
                        + (2/(1/tnr + 1/npv)) + " (F1=2/(1/TNR + 1/NPV))\n"
                + "\n"
                + "=== UNDESIRABLE STATS (0 to 1) ===\n"
                + "Inaccuracy:                               "
                        + (((double) (falsePos + falseNeg)) / total) + " (inaccuracy=(FP+FN)/Total)\n"
                + "False positive rate (Fallout):            "
                        + (fpr = (((double) falsePos) / (falsePos + trueNeg))) + " (FPR=FP/RealNeg)\n"
                + "False negative rate (Miss rate):          "
                        + (fnr = (((double) falseNeg) / (truePos + falseNeg))) + " (FNR=FN/RealPos)\n"
                + "False discovery rate (False alarm):       "
                        + (((double) falsePos) / (truePos + falsePos)) + " (FDR=FP/PredictedPos)\n"
                + "False omission rate:                      "
                        + (((double) falseNeg) / (trueNeg + falseNeg)) + " (FOR=FN/PredictedNeg)\n"
                + "\n"
                + "=== DESCRIPTIVE STATS (0 to inf) ===\n"
                + "Positive likelihood ratio (high better):  "
                        + (lrPlus = tpr/fpr) + " (LR+=TPR/FPR)\n"
                + "Negative likelihood ratio (low better):   "
                        + (lrMinus = fnr/tnr) + " (LR-=FNR/TNR)\n"
                + "Diagnostic odds ratio (high better):      "
                        + (lrPlus/lrMinus) + " (DOR=LR+/LR-)\n";
    }
}