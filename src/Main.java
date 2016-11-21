import java.util.ArrayList;

/**
 * Created by emresonmez on 11/9/15.
 */
public class Main {
    private FileUtils fileUtils;

    public Main(){
        fileUtils = new FileUtils();
    }
    /**
     * runs q
     * averages "overall runs" number of individual runs
     * @param args
     * @throws MazeException
     */
    public static void main(String[] args) throws MazeException {
        int individualRuns = 50;
        int overallRuns = 100;

        int[] startRow = {0,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int[] emptyRow = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int[] goalRow1 = {-1,-1,-1,-1,-1,-1,-1,-1,100,-1};
        int[] goalRow2 = {-1,-1,-1,-1,-1,100,-1,-1,-1,-1};
        int[] goalRow3 = {-1,-1,-1,-1,-1,-1,-1,-1,-1,100};
        int[] goalRow4 = {-1,-1,100,-1,-1,-1,-1,-1,-1,-1};

        int[][] maze = new int[][]{startRow, goalRow1, emptyRow, emptyRow, goalRow2, emptyRow, goalRow3, emptyRow, emptyRow, goalRow4};

        Main main = new Main();
        int[][] averagedGridWorld = main.getAveragedGridWorld(maze, 20, 4);

        main.runQ(averagedGridWorld, individualRuns, overallRuns, "q.txt");
        main.runBruteForce(maze);
    }

    private int[][] getAveragedGridWorld(int[][] gridWorld, int reward, int numRewards) {
        int[][] averagedGridWorld = gridWorld;
        int averagedReward = (reward + 1 - numRewards) / numRewards;

        for (int i = 0; i < averagedGridWorld[0].length; i++) {
            for (int j = 0; j < averagedGridWorld[0].length; j++) {
                if (averagedGridWorld[i][j] == reward) {
                    averagedGridWorld[i][j] = averagedReward;
                }
            }
        }

        return averagedGridWorld;
    }

    private void runQ(int[][] maze, int individualRuns, int overallRuns, String filename) throws MazeException {
        QLearner q = new QLearner(0.1, 15, 0.9, maze, -1, 0, 0, 100);
        ArrayList<ArrayList<Integer>> qLogs = new ArrayList<>();
        for (int i = 0; i < overallRuns; i++) {
            qLogs.add(q.qLearning(individualRuns));
        }
        fileUtils.averageAndWrite(filename, qLogs);
    }

    private void runBruteForce(int[][] maze) {
        GridWorld gridWorld = new GridWorld(maze);
        ArrayList<int[]> pathToCurrent = new ArrayList<>();
        int[] start = {0,0};
        pathToCurrent.add(start);
        gridWorld.enumeratePolicies(start, pathToCurrent);
    }
}