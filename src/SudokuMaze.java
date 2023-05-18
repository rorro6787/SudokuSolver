import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import com.qqwing.Difficulty;

public class SudokuMaze {
    private List<Long> meanFitnesses = new ArrayList<>();
    private List<Integer> bestFitnesses = new ArrayList<>();
    private int pobTam;
    private int mutationParameter;
    private Random rand = new Random();
    private int maze[][];
    private int solution[][] = new int[9][9];
    private List<int[][]> population;
    private List<Long> probabilities;

    private static final int MAX = 162;
    private int[][] best;
    private boolean found = false;
    private static final int numIterations = 400;
    private PrintWriter pw;

    public SudokuMaze(int tam, int mutationParameter, String filename) throws FileNotFoundException {
        this.mutationParameter = mutationParameter;
        pw = new PrintWriter(new File(filename));
        this.pobTam = tam;
        this.maze = new int[9][9];
        int[] mazeArray = PuzzleGenerator.computePuzzleWithNHolesPerRow(5);
        int pos = 0;
        int[][] holes = new int[9][];
        this.best = new int[9][];
        for(int i = 0; i < 9; ++i) {
            int tamArray = 0;
            for(int j = 0; j < 9; ++j) {
                if(mazeArray[pos] == 0) {
                    ++tamArray;
                }
                this.maze[i][j] = mazeArray[pos];
                ++pos;
            }
            holes[i] = new int[tamArray];
            best[i] = new int[tamArray];
        }
        population = new ArrayList<>();
        probabilities = new ArrayList<>();
        for(int k = 0; k < pobTam; ++k) {
            int aux[][] = new int[9][];
            for(int i = 0; i < 9; ++i) {
                aux[i] = new int[holes[i].length];
            }
            for(int i = 0; i < 9; ++i) {
                Set<Integer> forbidden = new HashSet<>();
                for(int j = 0; j < 9; ++j) {
                    if(maze[i][j] != 0) { forbidden.add(maze[i][j]); }
                }
                Set<Integer> visitedPos = new HashSet<>();
                int tamHoles = aux[i].length;
                for(int j = 1; j <= 9; ++j) {
                    if(!forbidden.contains(j)) {
                        int newPos = rand.nextInt(tamHoles);
                        while(visitedPos.contains(newPos)) {
                            newPos = rand.nextInt(tamHoles);
                        }
                        visitedPos.add(newPos);
                        aux[i][newPos] = j;
                    }
                }
            }
            int scoreAux = score(aux);
            if(scoreAux == 162) { found = true; best = aux; }
            probabilities.add(Long.valueOf(0));
            population.add(aux);
        }
        pw.print(this.toString());
        gAlgorithm();
    }

    private Integer score(int[][] aux) {
        int[][] copy = new int[9][9];
        buildSolution(copy, aux);
        int res = 0;
        for(int i = 0; i < 9; ++i) {
            res += numberOfUniquesColumn(copy, i);
            if(i == 0 || i == 3 || i == 6) {
                res += numberOfUniquesDomain(copy, i, 0);
                res += numberOfUniquesDomain(copy, i, 3);
                res += numberOfUniquesDomain(copy, i, 6);
            }
        }
        return res;
    }

    private int numberOfUniquesDomain(int[][] copy, int i, int j) {
        int res = 0;
        for(int number = 1; number <= 9; ++number) {
            int cnt = 0;
            for(int row = i; row < i + 3; ++row) {
                for(int column = j; column < j + 3; ++column) {
                    if(copy[row][column] == number) {
                        ++cnt;
                    }
                }
            }
            if(cnt == 1) {
                ++res;
            }
        }
        return res;
    }

    private int numberOfUniquesColumn(int[][] copy, int j) {
        int res = 0;
        for(int number = 1; number <= 9; ++number){
            int cnt = 0;
            for(int pos = 0; pos < 9; ++pos) {
                if(copy[pos][j] == number) {
                    ++cnt;
                }
            }
            if(cnt == 1) {
                ++res;
            }
        }
        return res;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Sudoku provided:\n\n");
        for(int i = 0; i < 9; ++i) {
            for(int j = 0; j < 9; ++j) {
                sb.append(this.maze[i][j] + " ");
            }
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public void gAlgorithm() {
        pw.println("---------------------------------------------------------\n" +
                "The Genetic Algorithm is going to try to solve the Sudoku\n" +
                "---------------------------------------------------------\n");
        for(int i = 0; i < numIterations; ++i) {
            pw.print("Iteration " + (i+1) + ":\nMean Fitness -> ");
            double meanScore = 0;
            int maxScore = 18, cnt = 0;
            for(int j = 0; j < this.pobTam; ++j) {
                ++cnt;
                int score = score(population.get(j));
                Long probability = Math.round((double) (100 * score - 1800) / 144);
                meanScore += score;
                if(score > maxScore) {
                    maxScore = score;
                }
                if(score == 162) {
                    meanScore /= cnt;
                    long meanScoreRound = Math.round(meanScore);
                    this.meanFitnesses.add(meanScoreRound);
                    this.bestFitnesses.add(maxScore);
                    pw.println(meanScoreRound + "\nBest Fitness -> " + maxScore + "\n");
                    found = true;
                    best = population.get(j);
                    break;
                }
                this.probabilities.set(j, probability);
            }
            if(found) break;
            meanScore /= pobTam;
            this.meanFitnesses.add(Math.round(meanScore));
            this.bestFitnesses.add(maxScore);
            pw.println(meanScore + "\nBest Fitness -> " + maxScore + "\n");
            selection();
            crossover();
            mutation();
        }
        if(found) {
            pw.println("-------------------------------------------\n" +
                    "A solution to this sudoku problem was found\n" +
                    "-------------------------------------------\n\nBest Individual:");
            for(int i = 0; i < 9; ++i) {
                for(int j = 0; j < this.best[i].length; ++j) {
                    pw.print(this.best[i][j] + " ");
                }
            }
            pw.println("\n\nSudoku Solved:\n");
            buildSolution(this.solution, this.best);
            for(int i = 0; i < 9; ++i) {
                for(int j = 0; j < 9; ++j) {
                    pw.print(this.solution[i][j] + " ");
                }
                pw.println();
            }
            pw.println();
            pw.close();
        }
        else {
            pw.println("Error... -> The algorithm could not find any solution to the problem");
            pw.close();
        }
    }

    private void selection() {
        List<int[][]> populationAux = new ArrayList<>();
        int cnt = 0;
        while(cnt != this.pobTam) {
            for(int i = 0; i < this.pobTam && cnt != this.pobTam; ++i) {
                if(this.rand.nextInt(100) <= probabilities.get(i)-18) {
                    populationAux.add(population.get(i));
                    ++cnt;
                }
            }
        }
        population.removeAll(population);
        population.addAll(populationAux);
    }

    private void crossover() {
        List<int[][]> populationAux = new ArrayList<>();
        for(int i = 0; i+1 < this.pobTam; i += 2) {
            int begin = rand.nextInt(9), end = rand.nextInt(9);
            while(begin == end) {
                begin = rand.nextInt(9);
                end = rand.nextInt(9);
            }
            int aux = Math.max(begin, end);
            if(aux == begin) { begin = end; end = aux; }
            int[][] first = new int[9][], second = new int[9][];
            for(int j = 0; j < 9; ++j) {
                if(j >= begin && j <= end) {
                    first[j] = population.get(i)[j];
                    second[j] = population.get(i+1)[j];
                }
                else {
                    first[j] = population.get(i+1)[j];
                    second[j] = population.get(i)[j];
                }
            }
            populationAux.add(first);
            populationAux.add(second);
        }
        if(this.pobTam % 2 != 0) { populationAux.add(this.population.get(this.pobTam-1)); }
        population.removeAll(population);
        population.addAll(populationAux);
    }
    private void mutation() {
        for(int i = 0; i < pobTam; ++i) {
            int[][] aux = population.get(i);
            boolean finished = false;
            while(!finished) {
                if(rand.nextInt(this.mutationParameter) == 0) {
                    int xPos = rand.nextInt(9);
                    int yPos1 = rand.nextInt(aux[xPos].length);
                    int yPos2 = rand.nextInt(aux[xPos].length);
                    while(yPos1 == yPos2) {
                        yPos1 = rand.nextInt(aux[xPos].length);
                        yPos2 = rand.nextInt(aux[xPos].length);
                    }
                    int auxiliar = aux[xPos][yPos1];
                    if(validSwap(aux[xPos][yPos1], xPos, yPos2) && validSwap(aux[xPos][yPos2], xPos, yPos1)) {
                        aux[xPos][yPos1] = aux[xPos][yPos2];
                        aux[xPos][yPos2] = auxiliar;
                    }
                }
                else {
                    finished = true;
                }
            }
        }
    }

    private boolean validSwap(int aux, int xPos, int yPos2) {
        int i = 0, j;
        for(j = 0; j < 9; ++j) {
            if(this.maze[xPos][j] == 0) {
                if(i == yPos2) {
                    break;
                }
                ++i;
            }
        }
        for(i = 0; i < 9; ++i) {
            if(maze[i][j] == aux) {
                return false;
            }
        }
        int raw = xPos - xPos % 3;
        int column = j - j % 3;
        for(int z = raw; z < raw + 3; ++z) {
            for(int k = column; k < column + 3; ++k) {
                if(this.maze[z][k] == aux) {
                    return false;
                }
            }
        }
        return true;
    }

    private void buildSolution(int[][] res, int[][] aux) {
        for(int i = 0; i < 9; ++i) {
            int yPos = 0;
            for(int j = 0; j < 9; ++j) {
                if(this.maze[i][j] == 0) {
                    res[i][j] = aux[i][yPos];
                    ++yPos;
                }
                else {
                    res[i][j] = this.maze[i][j];
                }
            }
        }
    }

    public List<Long> getMeanFitnesses() {
        return meanFitnesses;
    }

    public List<Integer> getBestFitnesses() {
        return bestFitnesses;
    }

    public boolean isFound() {
        return found;
    }
}
