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
        int averagedReward = (100 - 1 - 1 - 1)/4;

        int[] startRow = {0,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int[] emptyRow = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int[] goalRow1 = {-1,-1,-1,-1,-1,-1,-1,-1,100,-1};
        int[] goalRow2 = {-1,-1,-1,-1,-1,100,-1,-1,-1,-1};
        int[] goalRow3 = {-1,-1,-1,-1,-1,-1,-1,-1,-1,100};
        int[] goalRow4 = {-1,-1,100,-1,-1,-1,-1,-1,-1,-1};

        int[][] maze = new int[][]{startRow, goalRow1, emptyRow, emptyRow, goalRow2, emptyRow, goalRow3, emptyRow, emptyRow, goalRow4};
        Main main = new Main();
        main.runQ(maze,individualRuns,overallRuns,"q.txt", averagedReward);
    }

    private void runQ(int[][] maze, int individualRuns, int overallRuns, String filename, int reward) throws MazeException {
        QLearner q = new QLearner(reward, -1, maze);
        ArrayList<ArrayList<Integer>> qLogs = new ArrayList<>();
        for(int i = 0; i < overallRuns; i++){
            qLogs.add(q.qLearning(individualRuns));
        }
        fileUtils.averageAndWrite(filename, qLogs);
    }
}