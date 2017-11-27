import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import libsvm.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {

        int choice = 0, classes = 2; //0 = sp500 1 = drought if SP500 then classes = 3
        //----------------------input data start--------------------------------
        ArrayList<ArrayList<XSSFCell>> cells = new ArrayList<>();
        ArrayList<ArrayList<Double>> data = new ArrayList<>(); //stores inputs

        File myFile;
        if (choice == 0) {
            myFile = new File("data/ReturnsFull.xlsx");
        } else {
            myFile = new File("data/DroughtData.xlsx");
        }
        FileInputStream fis = new FileInputStream(myFile);

        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);

        XSSFRow row;
        XSSFCell cell = null;

        int rows; // No of rows
        rows = sheet.getPhysicalNumberOfRows();

        System.out.println("rows = " + rows);
        int cols = 0; // No of columns
        int tmp = 0;

        // This trick ensures that we get the data properly even if it doesn't start from first few rows
        for (int i = 0; i < 10 || i < rows; i++) {
            row = sheet.getRow(i);
            if (row != null) {
                tmp = sheet.getRow(i).getPhysicalNumberOfCells();
                if (tmp > cols) {
                    cols = tmp;
                }
            }
        }
        for (int n = 0; n < cols; n++) {
            cells.add(new ArrayList<>()); //fills arraylists for number of columns
            data.add(new ArrayList<>());
        }

        System.out.println("rows 2: " + rows);
        System.out.println("cols: " + cols);
        for (int r = 0; r < rows * 2; r++) { //*2 to fix halfing problem
            row = sheet.getRow(r);
            if (row != null) {
                for (int c = 0; c < cols; c++) {
                    cell = row.getCell((short) c);
                    if (cell != null) {
                        cells.get(c % cols).add(cell);
                    }
                }
            }
        }
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++) {
            names.add(cells.get(i).get(0).toString());
        }
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i));
        }

        System.out.println("all cells contain n = : " + cells.get(0).size());
        for (int i = 0; i < cells.size(); i++) {
            for (int j = 1; j < cells.get(i).size(); j++) { //adjust to isolate years
                cells.get(i).get(j).setCellType(CellType.NUMERIC); //convert cell to numeric
                data.get(i).add((Double) cells.get(i).get(j).getNumericCellValue()); //convert cell to int and add to arraylist
            }
        }
        //-------------------input data end-------------------------------------
        System.out.println("all rows contain n = : " + data.get(0).size());

        ArrayList<ArrayList<Double>> sets;
        ArrayList<Double> c;
        if (choice == 0) {
            ArrayList<ArrayList<Integer>> disValues = new ArrayList<>(); //holds dis_values
            for (int i = 0; i < data.size(); i++) { //adds entire data set into discretizer
                disValues.add(Discretizer.disData(data.get(i)));
            }
            ArrayList<String> namesTemp = new ArrayList<>();
            for (int i = 0; i < names.size(); i++) {
                namesTemp.add(names.get(i));
            }
            Discretizer.getTop10(disValues, namesTemp); //top 10 gains

            System.out.println(namesTemp.size() + " sets chosen based on gains");

            sets = new ArrayList<>(); //holds dis_values
            for (int i = 0; i < namesTemp.size(); i++) {
                int v = names.indexOf(namesTemp.get(i));
                sets.add(data.get(v));
                flip(sets.get(i));
            }
            c = new ArrayList<>();
            for (int i = 0; i < disValues.get(disValues.size() - 1).size(); i++) {
                c.add((double) disValues.get(disValues.size() - 1).get(i));
            }
            //flip(c);
            c.add(0.0);
        } else {
            sets = new ArrayList<>(); //holds dis_values
            for (int i = 0; i < data.size() - 1; i++) {

                sets.add(data.get(i));
            }
            c = data.get(data.size() - 1);
        }
        System.out.println("C: " + c.size());
        System.out.println("Final data set size: " + sets.size());

        System.out.println("SVM starts here:---------------------------- ");

        int testN = 150;
        int trainN = sets.get(0).size();
        //double[][] train = new double[sets.size() * (sets.get(0).size() - testN - 1)][];
        double[][] train = new double[trainN - testN][];
        double[][] test = new double[testN][];

        /*int n = 0;
        for (int i = 0; i < sets.size(); i++) {
            for (int j = testN; j < trainN; j++) {
                if (sets.get(i).get(j) >= sets.get(i).get(j - 1)) {
                    double[] vals = {c.get(j), n, sets.get(i).get(j)};
                    train[n++] = vals;
                } else {
                    double[] vals = {c.get(j), -n, -sets.get(i).get(j)};
                    train[n++] = vals;
                }
            }
        }
        n = 0;
        for (int i = 0; i < sets.size(); i++) {
            for (int j = 1; j < testN; j++) {
                if (sets.get(i).get(j) >= sets.get(i).get(j - 1)) {
                    double[] vals = {c.get(j), n, sets.get(i).get(j) + train.length};
                    test[n++] = vals;
                } else {
                    double[] vals = {c.get(j), -n, -sets.get(i).get(j)};
                    test[n++] = vals;
                }
            }
        }
        int n = 0;
        for (int i = 0; i < sets.size(); i++) {
            for (int j = 0; j < testN; j++) {
                if (c.get(j) == 0.0) {
                    double[] vals = {c.get(j), n, sets.get(i).get(j)};
                    test[n++] = vals;
                } else {
                    double[] vals = {c.get(j), -n, -sets.get(i).get(j)};
                    test[n++] = vals;
                }
            }
        }
        n = 0;
        for (int i = 0; i < sets.size(); i++) {
            for (int j = testN; j < trainN; j++) {
                if (c.get(j) == 0.0) {
                    double[] vals = {c.get(j), n, sets.get(i).get(j)};
                    train[n++] = vals;
                } else {
                    double[] vals = {c.get(j), -n, -sets.get(i).get(j)};
                    train[n++] = vals;
                }
            }
        }*/
        int n = 0;
        for (int i = testN; i < sets.get(0).size(); i++) {
            double[] vals = new double[sets.size()];
            vals[0] = c.get(i);
            for (int j = 1; j < sets.size(); j++) {
                vals[j] = sets.get(j - 1).get(i);

            }
            train[n++] = vals;
        }

        n = 0;
        for (int i = 0; i < testN; i++) {
            double[] vals = new double[sets.size()];
            vals[0] = c.get(i);
            for (int j = 1; j < sets.size(); j++) {
                vals[j] = sets.get(j - 1).get(i);

            }
            test[n++] = vals;
        }

        System.out.println("Test: " + test.length);
        System.out.println("Train: " + train.length);
        svm_model s = svmTrain(train);
        System.out.println("Training complete..");
        double count = 0;
        for (int i = 0; i < test.length; i++) {
            count += evaluate(test[i], s, classes);
        }
        System.out.println("The total accuracy: " + (Double) (count / test.length));
    }

    private static svm_model svmTrain(double[][] train) {
        svm_problem prob = new svm_problem();
        int dataCount = train.length;
        prob.y = new double[dataCount];
        prob.l = dataCount;
        prob.x = new svm_node[dataCount][];

        for (int i = 0; i < dataCount; i++) {
            double[] features = train[i];
            prob.x[i] = new svm_node[features.length - 1];
            for (int j = 1; j < features.length; j++) {
                svm_node node = new svm_node();
                node.index = j;
                node.value = features[j];
                prob.x[i][j - 1] = node;
            }
            prob.y[i] = features[0];
        }

        svm_parameter param = new svm_parameter();
        param.probability = 1;
        param.gamma = 0.5;
        param.nu = 0.5;
        param.C = 1;
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 20000;
        param.eps = 0.001;

        svm_model model = svm.svm_train(prob, param);

        return model;
    }

    public static double evaluate(double[] features, svm_model model, int c) {
        svm_node[] nodes = new svm_node[features.length - 1];
        for (int i = 1; i < features.length; i++) {
            svm_node node = new svm_node();
            node.index = i;
            node.value = features[i];

            nodes[i - 1] = node;
        }

        int totalClasses = c;
        int[] labels = new int[totalClasses];
        svm.svm_get_labels(model, labels);

        double[] prob_estimates = new double[totalClasses];
        double v = svm.svm_predict_probability(model, nodes, prob_estimates);

        for (int i = 0; i < totalClasses; i++) {
            System.out.print("(" + labels[i] + ":" + prob_estimates[i] + ")");
        }
        System.out.println("(Actual:" + features[0] + " Prediction:" + v + ")");

        if (features[0] == v) {
            return 1.0;
        }
        return 0.0;
    }

    //tested OK Aug 20, 2017
    public static void flip(ArrayList<Double> data) {
        ArrayList<Double> temp = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            temp.add(data.get(i));
        }
        data.clear();
        for (int i = temp.size() - 1; i >= 0; i--) {
            data.add(temp.get(i));
        }
    }
}