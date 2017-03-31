import java.util.ArrayList;

/**
 * Created by emresonmez on 11/9/15.
 */
public class Main {
    private FileUtils fileUtils;
    private ArrayList<int[][]> mazes;
    private int[][] averagedMaze;

    public Main(){
        fileUtils = new FileUtils();
        this.populateGridWorlds();
    }

    public static void main(String[] args) throws MazeException {
//        int individualRuns = 1000;
//        int overallRuns = 20;
        System.out.println("Starting runs...");
        Main main = new Main();
        main.runQ();
        System.out.println("Runs finished.");
    }


    /**
     * Create grid worlds used for q-learning.
     */
    private void populateGridWorlds() {
        int[] startRow = {0,-1,-1,-1,-1};
        int[] emptyRow = {-1,-1,-1,-1,-1};
        int[] reward1 = {-1,100,-1,-1,-1};
        int[] reward2  = {-1,-1,-1,100,-1};
        int[] reward3 = {100,-1,-1,-1,-1};
        int[] reward4 = {-1,-1,100,-1,-1};

        int[][] grid1 = {startRow, reward1, emptyRow, emptyRow, emptyRow};
        int[][] grid2 = {startRow, emptyRow, reward2, emptyRow, emptyRow};
        int[][] grid3 = {startRow, emptyRow, emptyRow, reward3, emptyRow};
        int[][] grid4 = {startRow, emptyRow, emptyRow, emptyRow, reward4};

        // get the averaged grid world, setting each reward state to the average of all rewards.
        int[][] superGrid = {startRow, reward1, reward2, reward3, reward4};
        this.averagedMaze = getAveragedGridWorld(superGrid, 100, 4);

        this.mazes = new ArrayList<>();
        this.mazes.add(0, grid1);
        this.mazes.add(1, grid2);
        this.mazes.add(2, grid3);
        this.mazes.add(3, grid4);
        this.mazes.add(4, superGrid);
    }

    /**
     * Creates an average of the 4
     * @param gridWorld
     * @param reward
     * @param numRewards
     * @return
     */
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



    private void runQ() throws MazeException {
        // Proof steps:
        //   1. Run q-learning on each individual grid world. Get the reward. Average rewards for all four mazes.
        //   2. Run q-learning on averaged maze. Get the Q matrix.
        //   3. Using the q-matrix, calculate the reward for each individual grid world using that Q matrix and average.
        //   4. Confirm the average in 3 is equal to or greater than the average in 1.

        // Step 1
        boolean terminateAtGoalState = true;
        int numSteps = 10000;
        int goalValue = 100;
        int stepValue = -1;
        double goalGamma = .75;
        double stepGamma = .95;
        int startX = 0;
        int startY = 0;
        double alpha = 0.1;
        double epsilon = 0.95;

        QLearner qLearner = new QLearner(terminateAtGoalState, numSteps, goalValue, goalGamma, stepValue, stepGamma,
                this.mazes, startX, startY, alpha, epsilon);

        qLearner.runQLearner(10000, 0);
        double reward = qLearner.getCurrentMaxReward();
        System.out.println("Max reward, maze 0: " + reward);

        double[][][] emptyQ = new double[5][5][4];

        qLearner.setQ(emptyQ);
        qLearner.runQLearner(10000, 1);
        double reward1 = qLearner.getCurrentMaxReward();
        System.out.println("Max reward, maze 1: " + reward1);
        qLearner.setCurrentMaxReward(0);

        qLearner.setQ(emptyQ);
        qLearner.runQLearner(10000, 2);
        double reward2 = qLearner.getCurrentMaxReward();
        System.out.println("Max reward, maze 2: " + reward2);
        qLearner.setCurrentMaxReward(0);

        qLearner.setQ(emptyQ);
        qLearner.runQLearner(10000, 3);
        double reward3 = qLearner.getCurrentMaxReward();
        System.out.println("Max reward, maze 3: " + reward3);
        qLearner.setCurrentMaxReward(0);

        double averagedReward = (reward + reward1 + reward2 + reward3) / 4;
        System.out.println("Averaged reward (Step 1): " + averagedReward);

        // Step 2
        qLearner.setQ(emptyQ);
        qLearner.setTerminateAtGoalState(false);
        qLearner.runQLearner(1000, 4);
        double reward4 = qLearner.getCurrentMaxReward();
        System.out.println("Averaged reward: " + reward4);
        qLearner.setCurrentMaxReward(0);


    }
}