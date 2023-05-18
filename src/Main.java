import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static void main1(int basePop, int baseMut) throws FileNotFoundException {
        System.out.println();
        SudokuMaze maze = new SudokuMaze(basePop, baseMut, "result.txt");
        System.out.print("The result is in result.txt");
        if(!maze.isFound()) {
            System.out.println(" but if I were you, I would introduce a 1 again becouse it looks like " +
                               "the algorithm struggled finding a solution this time");
        }
        else {
            System.out.println(" with a solution to your puzzle");
        }
        System.out.println();
    }

    private static void main2(int basePop, int baseMut, int aux1, int aux2) throws FileNotFoundException {
        System.out.println();
        Long tiempo1 = Long.valueOf(0), tiempo2 = Long.valueOf(0);
        List<Long> tiempos = new ArrayList<>();
        List<Long> meanFitnesses = new ArrayList<>();
        List<Integer> bestFitnesses = new ArrayList<>();
        SudokuMaze maze = null;
        boolean display = false;
        for(int i = 0; i < aux1; ++i) {
            for(int j = 0; j < aux2; ++j) {
                Calendar ahora1 = Calendar.getInstance();
                tiempo1 = ahora1.getTimeInMillis();
                maze = new SudokuMaze(basePop + (j * 1000), (baseMut * (10 + i)) / 10, "result.txt");
                Calendar ahora2 = Calendar.getInstance();
                tiempo2 = ahora2.getTimeInMillis();
                if(maze.isFound()) {
                    if(!display) {
                        display = true;
                        meanFitnesses.addAll(maze.getMeanFitnesses());
                        bestFitnesses.addAll(maze.getBestFitnesses());
                    }
                    tiempos.add(tiempo2 - tiempo1);
                } else {
                    tiempos.add(Long.valueOf(-1));
                }
            }
        }
        System.out.println("Best Fitnesses throughtout " + bestFitnesses.size() + " iterations:\n" + bestFitnesses.toString());
        System.out.println("Mean Fitnesses throughout " + meanFitnesses.size() + " iterations:\n" + meanFitnesses.toString());
        System.out.println();
        int contador = 0;
        try(PrintWriter pw = new PrintWriter(new File("excel3.txt"))) {
            for (int i = 0; i < aux1; ++i) {
                for (int j = 0; j < aux2; ++j, ++contador) {
                    double time = tiempos.get(contador);
                    time = time < 0 ? 0 : time / 1000;
                    /*System.out.println("Población: " + (basePop + (j * 1000)));
                    System.out.println("Mutation-Rate: " + (double) (baseMut * (10 + i)) / 1000000);
                    System.out.println("Time: " + time);
                    System.out.println();*/
                    pw.println((basePop + (j * 1000)) + ", " + (double) (baseMut * (10 + i)) / 1000000 + ", " + time);
                }
                pw.println();
            }
        }
        System.out.println(tiempos);
        System.out.println();
    }

    public static void main(String[] args) throws FileNotFoundException {
        String line = "";
        int basePop = 2000, baseMut = 5000;
        while(!line.equals("F")) {
            try {
                Scanner sc = new Scanner(System.in);
                System.out.println("------------------------------------------------------\n" +
                                   "What main do you want to access 1 or 2? (F to finish):\n" +
                                   "------------------------------------------------------\n");
                line = sc.next();
                if(line.equals("1")) {
                    main1(basePop, baseMut);
                }
                else if(line.equals("2")) {
                    main2(basePop, baseMut, 5, 15);
                }
                else if(!line.equals("F")) {
                    throw new SudokuException("\nNeither main 1 or main 2 wanted to be accessed\n");
                }
            }
            catch(SudokuException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
