package project;

import com.google.common.graph.Graph;
import com.google.common.primitives.Ints;
import org.jgraph.JGraph;
import org.jgraph.graph.*;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.text.html.StyleSheet;

import java.io.IOException;
import java.util.*;
import java.util.List;

class Pair {

    int a;
    int b;

    Pair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "{" +
                "" + a +
                ", \t" + b +
                '}';
    }
}

public class Main {

    private static int y = 0;

    private static int m = 0; // число хромосом у особи
    private static int n = 4; // число процессоров
    private static int z = 9; // число особей
    private static int k = 9; // число повторов полного цикла смены поколений
    private static int pk = 78; // %
    private static int pm = 77; // вероятность мутации

    private static ArrayList<int[]> arrayList = new ArrayList<>(); // для хранения особей и их данных
    private static Integer[] processors = new Integer[n]; // для выявления максимальной нагрузки

    private static int c = 256; //(int) Math.pow(2, m); // получили то самое большое число
    private static int d = c / n; // шаг (для вычисления нагрузок на процессор)

    private static int numberForMutation;
    private static int bitMutation;

    private static int currentK = k;
    private static int r;

    private static int[][] weights;

    public static DefaultGraphCell createVertex(String name, double x,
                                                double y, double w, double h, Color bg) {

        DefaultGraphCell cell = new DefaultGraphCell(name);
        //GraphConstants.setB
        GraphConstants.setBounds(cell.getAttributes(),
                new Rectangle2D.Double(x, y, w, h));

        if (bg != null) {
            GraphConstants.setGradientColor(cell.getAttributes(), bg);
            GraphConstants.setOpaque(cell.getAttributes(), true);
        }

        GraphConstants.setBorderColor(cell.getAttributes(), Color.BLACK);

        DefaultPort port = new DefaultPort();
        cell.add(port);

        return cell;
    }

    public static void main(String[] args) throws IOException {

        GraphModel model = new DefaultGraphModel();
        GraphLayoutCache view = new GraphLayoutCache(model, new DefaultCellViewFactory());

        JGraph graph = new JGraph(model, view);
        graph.setBackground(Color.BLACK);
        graph.setCloneable(true);
        graph.setInvokesStopCellEditing(true);

        ArrayList<DefaultGraphCell> cells = new ArrayList<>();

        int t = 7;
        m = t;
        double aspect = 45;
        double sum = 0;
        for (int i = 1; i < t; i++)
            if (i <= t/2) {
                cells.add(createVertex(Integer.toString(i), sum, (t - i * 2) * (t - i * 2) * aspect / 6, 20, 20, Color.MAGENTA));
                if (i != t/2) sum += aspect * i;
            }
            else {
                sum += aspect * (t - i);
                cells.add(createVertex(Integer.toString(i), sum, (t - i * 2) * (t - i * 2) * aspect / 6, 20, 20, Color.MAGENTA));
            }

        sum += 128;
        ArrayList<DefaultGraphCell> cells2 = new ArrayList<>();
        for (int i = 1; i < t; i++)
            if (i <= t/2) {
                cells2.add(createVertex(Integer.toString(i), sum, (t - i * 2) * (t - i * 2) * aspect / 6, 20, 20, Color.MAGENTA));
                if (i != t/2) sum += aspect * i;
            }
            else {
                sum += aspect * (t - i);
                cells2.add(createVertex(Integer.toString(i), sum, (t - i * 2) * (t - i * 2) * aspect / 6, 20, 20, Color.MAGENTA));
            }

        weights = new int[cells.size()][cells.size()];
        for (int i = 0; i < cells.size(); i++)
            for (int j = i + 1; j < cells.size(); j++)
                weights[i][j] = 0;

        for (int i = 0; i < cells.size(); i++)
            for (int j = i + 1; j < cells.size(); j++)
                weights[i][j] = (int) (Math.random()*10 + 10);

        for (int i = 0; i < cells.size(); i++)
            for (int j = 0; j < i; j++)
                weights[i][j] = weights[j][i];

        DefaultEdge[][] edges = new DefaultEdge[cells.size()][cells.size()];
        DefaultEdge[][] edges2 = new DefaultEdge[cells.size()][cells.size()];



        for (int i = 0; i < cells.size(); i++)
            for (int j = 0; j < cells.size(); j++) {
                if (i != j) {
                    DefaultEdge edge = new DefaultEdge(weights[i][j]);
                    edge.setSource(cells.get(i).getChildAt(0));
                    edge.setTarget(cells.get(j).getChildAt(0));
                    edges[i][j] = edge;
                }
            }
        for (int i = 0; i < cells.size(); i++)
            for (int j = 0; j < cells.size(); j++) {
                if (i != j) {
                    DefaultEdge edge = new DefaultEdge(weights[i][j]);
                    edge.setSource(cells2.get(i).getChildAt(0));
                    edge.setTarget(cells2.get(j).getChildAt(0));
                    edges2[i][j] = edge;
                }
            }

        for (int i = 0; i < t - 1; i++)
            for (int j = 0; j < t - 1; j++) {
                if (i != j) {
                    cells.add(edges[i][j]);
                    cells2.add(edges2[i][j]);
                }
            }

//
//        for (DefaultEdge edge : edges) {
//            //int arrow = GraphConstants.ARROW_SIMPLE;
//            GraphConstants.setForeground(edge.getAttributes(), Color.WHITE);
//            GraphConstants.setFont(edge.getAttributes(), new Font("Helvetica", Font.PLAIN, 11));
//            GraphConstants.setLineColor(edge.getAttributes(), Color.BLUE);
//            GraphConstants.setLineEnd(edges.get(i).getAttributes(), arrow);
//            GraphConstants.setEndFill(edge.getAttributes(), true);
//        }

        //System.out.println("Введите вершину:");
        int begin = 0;
        int a = begin;

        int min = 1000000;
        int index = a;
        HashSet<Integer> h = new HashSet<>();
        h.add(a);



        while (true) {
            for (int j = 0; j < weights[0].length; j++)
                if (weights[a][j] < min) {
                    if (!h.contains(j)) {
                        min = weights[a][j];
                        index = j;
                    }
                }
            if (h.size() == weights[0].length) break;
            h.add(index);
            GraphConstants.setLineColor(edges[a][index].getAttributes(), Color.RED);
            GraphConstants.setLineColor(edges[index][a].getAttributes(), Color.RED);
            GraphConstants.setLineEnd(edges[a][index].getAttributes(), GraphConstants.ARROW_SIMPLE);
            a = index;
            min = 1000000;
        }

        GraphConstants.setLineColor(edges[a][begin].getAttributes(), Color.RED);
        GraphConstants.setLineColor(edges[begin][a].getAttributes(), Color.RED);
        GraphConstants.setLineEnd(edges[a][begin].getAttributes(), GraphConstants.ARROW_SIMPLE);

        graph.getGraphLayoutCache().insert(cells.toArray());

        JFrame frame2 = new JFrame();
        JScrollPane jScrollPane2 = new JScrollPane(graph);
        jScrollPane2.setBackground(Color.BLACK);
        frame2.getContentPane().add(jScrollPane2);
        frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame2.pack();
        frame2.setVisible(true);


















        for (int i = 0; i < z; i++) {
            ArrayList<Integer> osoba = new ArrayList<>();
            osoba.add(begin);
            while (true) {
                int random = (int) (Math.random()*(t-1));
                if (!osoba.contains(random)) osoba.add(random);
                if (osoba.size() == t-1) break;
            }
            osoba.add(begin);
            arrayList.add(Ints.toArray(osoba));
            // System.out.println(Arrays.toString(pair));

        } // сформировали лист с особями и их данными
        for (int[] integers : arrayList)
            System.out.println(Arrays.toString(integers));
        // необходимо написать функцию подсчёта макимальной нагрузки для особи
        // на вход приходит сама особь, на выходе - одно число
        // готово

        int[] lastEvent = new int[z];
        int[] currentEvent = new int[z];

        while (true) {
            y++;
            System.out.println("\n\n\nНомер поколения: " + y);
            System.out.println("Текущее поколение:");
            for (int i = 0; i < z; i++) {
                lastEvent[i] = findMax(arrayList.get(i));
                System.out.println(lastEvent[i] + "\t" + Arrays.toString(arrayList.get(i)));
            }

            // теперь нужно запустить цикл отбора
            // нужно написать функцию кроссовера двух особей
            // на вход подаётся номер особи
            // на выходе - сообщение о результате (true/false)

            System.out.println("Результат цикла:");
            for (int i = 0; i < z; i++) {

                //if (t < pk)
                    System.out.println(crossover(i));
                //else System.out.println("Кроссовера не произошло!");
            }

            //System.out.println("Поколение после цикла:");
            for (int i = 0; i < z; i++) {
                currentEvent[i] = findMax(arrayList.get(i));
                //System.out.println(Arrays.toString(arrayListPair.get(i)) + " " + currentEvent[i]);
            }

            // теперь нужно проверить, изменилось ли вообще решение
            if (Arrays.stream(lastEvent).max().equals(Arrays.stream(currentEvent).max())) {
                currentK--;
                System.out.println("Осталось " + currentK + " повторов!");
            } else currentK = k;
            int[] result = new int[0];
            if (currentK == 0) {
                System.out.println("\n\n\nИтоговое поколение:");
                for (int i = 0; i < z; i++) {
                    currentEvent[i] = findMax(arrayList.get(i));
                    if (Arrays.stream(currentEvent).max().getAsInt() == currentEvent[i]) {
                        result = arrayList.get(i);
                    }
                    System.out.println(currentEvent[i] + "\t" + Arrays.toString(arrayList.get(i)));
                }



                for (int l = 0; l < result.length - 1; l++) {
                    GraphConstants.setLineColor(edges2[result[l]][result[l+1]].getAttributes(), Color.GREEN);
                    GraphConstants.setLineColor(edges2[result[l+1]][result[l]].getAttributes(), Color.GREEN);
                    GraphConstants.setLineEnd(edges2[result[l]][result[l+1]].getAttributes(), GraphConstants.ARROW_SIMPLE);
                }

                //graph.getGraphLayoutCache().reload();
                JGraph graph2 = new JGraph(model, view);
                graph2.setBackground(Color.BLACK);
                graph2.setCloneable(true);
                graph2.setInvokesStopCellEditing(true);

                graph2.getGraphLayoutCache().insert(cells2.toArray());

                JFrame frame = new JFrame();
                JScrollPane jScrollPane = new JScrollPane(graph2);
                jScrollPane.setBackground(Color.BLACK);
                frame.getContentPane().add(jScrollPane);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);



                return;
            }
        }



    }

    public static int findMax(int[] integers) {

        int sum = 0;

        for (int i = 0; i < integers.length - 1; i++) {
            sum += weights[integers[i]][integers[i+1]];
        }

        return sum;
    }

    public static boolean crossover(int number) {

        int t1;
        int partner = (int) (Math.random() * (z - 1));
        while (partner == number) {
            t1 = (int) (Math.random() * 99);
            if (t1 < pk) {
                partner = (int) (Math.random() * (z - 1));
            } else {
                System.out.println("Партнёр не подошёл, генерируем нового...");
            }
        }

        int partValue = (int) (Math.random() * (m - 3)) + 2; // в каком месте делим?


        // теперь нужно сделать сам кроссовер
        // создаём новую особь - результат кроссовера
        boolean flag = false;
        System.out.println("Создаётся кроссовер " + (number + 1) + "-" + (partner + 1) + ". Точка раздела: " + (partValue));
        int[] newIntegers = new int[m];
        for (int i = 0; i < m - 1; i++) {
            if (i < partValue)
                newIntegers[i] = arrayList.get(number)[i];
            else {
                for (int j = 0; j < m; j++) {
                    for (int newInteger : newIntegers)
                        if (arrayList.get(partner)[j] == newInteger)  // значит, идем к следующему
                        {
                            flag = true;
                            break;
                        }
                    if (!flag) {
                        newIntegers[i] = arrayList.get(partner)[j];
                        break;
                    } else flag = false;
                }
            }
        }
        // результат кроссовера

        System.out.println(findMax(newIntegers) + "\t" + Arrays.toString(newIntegers));

        // тут ещё должна быть мутация
//        r = (int) (Math.random() * 100);
//        if (r < pm) {
//            numberForMutation = (int) (Math.random() * (m-1));
//            bitMutation = (int) (Math.random() * (2) + 5);
//
//            if ((int) (newPair[numberForMutation].b / Math.pow(2, bitMutation)) % 2 == 1)
//                newPair[numberForMutation].b =
//                        newPair[numberForMutation].b - (int) Math.pow(2, bitMutation);
//            else newPair[numberForMutation].b =
//                    newPair[numberForMutation].b + (int) Math.pow(2, bitMutation);
//            System.out.println("В числе номер " + (numberForMutation+1) + " в бите " +
//                    (bitMutation) + " произошла мутация!");
//            System.out.println(findMax(newPair) + "\t" + Arrays.toString(newPair));
//        }
//
        // А теперь нужен кроссовер-наоборот
        System.out.println("Создаётся кроссовер " + (partner + 1) + "-" + (number + 1) + ". Точка раздела: " + (partValue));
        // создаём новую особь - результат кроссовера-наоборот
        int[] newIntegers2 = new int[m];
        for (int i = 0; i < m - 1; i++) {
            if (i < partValue)
                newIntegers2[i] = arrayList.get(partner)[i];
            else {
                for (int j = 0; j < m; j++) {
                    for (int newInteger : newIntegers2)
                        if (arrayList.get(number)[j] == newInteger)  // значит, идем к следующему
                        {
                            flag = true;
                            break;
                        }
                    if (!flag) {
                        newIntegers2[i] = arrayList.get(number)[j];
                        break;
                    } else flag = false;
                }
            }
        }
        System.out.println(findMax(newIntegers2) + "\t" + Arrays.toString(newIntegers2));
//        // тут ещё должна быть мутация
//        r = (int) (Math.random() * 100);
//        if (r < pm) {
//            numberForMutation = (int) (Math.random() * (m-1));
//            bitMutation = (int) (Math.random() * (2) + 5);
//
//            if ((int) (newPair2[numberForMutation].b / Math.pow(2, bitMutation)) % 2 == 1)
//                newPair2[numberForMutation].b =
//                        newPair2[numberForMutation].b - (int) Math.pow(2, bitMutation);
//            else newPair2[numberForMutation].b =
//                    newPair2[numberForMutation].b + (int) Math.pow(2, bitMutation);
//            System.out.println("В числе номер " + (numberForMutation+1) + " в бите " +
//                    (bitMutation) + " произошла мутация!");
//            System.out.println(findMax(newPair2) + "\t" + Arrays.toString(newPair2));
//        }
//
        // что лучше - обычный кроссовер или кроссовер-наоборот?
        if (findMax(newIntegers) > findMax(newIntegers2)) {
            newIntegers = newIntegers2;
            System.out.println("Второй потомок лучше первого");
        } else System.out.println("Первый потомок лучше второго");


        if (findMax(newIntegers) < findMax(arrayList.get(number))) {
            System.out.println("Потомок переходит в новое поколение: " + findMax(newIntegers) + "<" + findMax(arrayList.get(number)));
            arrayList.set(number, newIntegers);
            return true;
        } else {
            System.out.println("Переход в новое поколение без изменений: " + findMax(newIntegers) + ">=" + findMax(arrayList.get(number)));
            return true;
        }
    }

}
