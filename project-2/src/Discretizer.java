import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author jeffreyyoung
 */
public class Discretizer {

    private Discretizer() {}

    //Hi-Low discretizes data according to n (> | < | =) n - 1
    public static ArrayList<Integer> disData(ArrayList<Double> d) {
        ArrayList<Integer> a = new ArrayList<>();//holds discretized values
        for (int i = d.size() - 1; i > 0; i--) {
            //for (int i = 1; i < d.size(); i++) {
            double t1 = Math.round(d.get(i) * 10000.0) / 10000.0;
            double t2 = Math.round(d.get(i - 1) * 10000.0) / 10000.0;
            if (t1 == t2) {
                a.add(0); //does not change
            } else if (t1 > t2) {
                a.add(1); //goes down
            } else if (t1 < t2) {
                a.add(0); //goes up
            }
        }
        return a;
    }
    //end Hi-Low----------------------------------------------------------------


    //get top 10 companies according to gains-----------------------------------
    public static ArrayList<ArrayList<Integer>> getTop10(ArrayList<ArrayList<Integer>> a, ArrayList<String> names) {
        ArrayList<ArrayList<Integer>> rList = new ArrayList<>();
        ArrayList<Double> gains = new ArrayList<>();
        ArrayList<String> namesTemp = new ArrayList<>();
        ArrayList<ArrayList<Integer>> b = new ArrayList<>(); //holds values from a which can be removed
        for (int i = 0; i < a.size(); i++) {
            b.add(a.get(i));
        }

        for (int i = 0; i < a.size() - 1; i++) {
            gains.add(getEntropy(a.get(a.size() - 1)) - getGain(a.get(a.size() - 1), a.get(i)));
        }
        for (int i = 0; i < a.size() - 1; i++) { //adjust to get more top companies

            double max = Collections.max(gains);
            System.out.println("max: " + max);
            int index = gains.indexOf(max);
            rList.add(b.get(index));
            namesTemp.add(names.get(index));

            gains.remove(index);
            names.remove(index);
            b.remove(index);
        }

        names.clear(); //clears and adds names of top 10
        for (int i = 0; i < namesTemp.size(); i++) {
            names.add(namesTemp.get(i));
        }

        return rList;

    }
    //end top10 gains-----------------------------------------------------------

    //gains, entropy and associated methods from this point---------------------
    public static double getGain(ArrayList<Integer> decision, ArrayList<Integer> list) {
        Set<Integer> s = new HashSet<>(list);
        ArrayList<Integer> att = new ArrayList<>(s);

        double gain = 0;
        for (int i = 0; i < att.size(); i++) {
            double freq = freqOf(att.get(i), list);
            double freqVar = freq / list.size();
            double a = 0;

            ArrayList<Integer> compare = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                if (list.get(j).equals(att.get(i))) {
                    compare.add(decision.get(j));
                }
            }
            Set<Integer> s2 = new HashSet<>(compare);
            ArrayList<Integer> att2 = new ArrayList<>(s2);
            for (int k = 0; k < att2.size(); k++) {
                double f = freqOf(att2.get(k), compare) / freq;
                a -= (f * log2(f));
            }

            gain += (freqVar * a);
        }

        return gain;
    }

    public static double getEntropy(ArrayList<Integer> list) {
        Set<Integer> s = new HashSet<>(list);
        ArrayList<Integer> att = new ArrayList<>(s);
        double freq = freqOf(att.get(0), list) / list.size();
        double entropy = -freq * log2(freq);

        for (int i = 1; i < att.size(); i++) {
            freq = (freqOf(att.get(i), list)) / list.size();
            entropy -= (freq * log2(freq));
        }

        return entropy;
    }

    public static double freqOf(int n, ArrayList<Integer> a) {
        double count = 0;
        for (int i = 0; i < a.size(); i++) {
            if (a.get(i).equals(n)) {
                count++;
            }
        }
        return count;
    }

    public static double log2(double n) {
        return (Math.log(n) / Math.log(2));
    }

}