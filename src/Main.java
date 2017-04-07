import java.util.ArrayList;

/**
 * Created by emresonmez on 11/9/15.
 */
public class Main {
    private FileUtils fileUtils;
    private ArrayList<double[][]> gridWorlds;
    private double[][] averagedMaze;

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
        double[] startRow = {0,-1,-1,-1,-1};
        double[] emptyRow = {-1,-1,-1,-1,-1};
        double[] reward1 = {-1,100,-1,-1,-1};
        double[] reward2  = {-1,-1,-1,100,-1};
        double[] reward3 = {100,-1,-1,-1,-1};
        double[] reward4 = {-1,-1,100,-1,-1};

        double[][] grid1 = {startRow, reward1, emptyRow, emptyRow, emptyRow};
        double[][] grid2 = {startRow, emptyRow, reward2, emptyRow, emptyRow};
        double[][] grid3 = {startRow, emptyRow, emptyRow, reward3, emptyRow};
        double[][] grid4 = {startRow, emptyRow, emptyRow, emptyRow, reward4};

        // get the averaged grid world, setting each reward state to the average of all rewards.


        this.gridWorlds = new ArrayList<>();
        this.gridWorlds.add(grid1);
        this.gridWorlds.add(grid2);
        this.gridWorlds.add(grid3);
        this.gridWorlds.add(grid4);

        double[][] superGrid = {startRow, reward1, reward2, reward3, reward4};
        this.averagedMaze = getAveragedGridWorld(superGrid, 100, 4);
        this.gridWorlds.add(averagedMaze);
    }

    /**
     * Creates an average of the 4
     * @param gridWorld
     * @param reward
     * @param numRewards
     * @return
     */
    private double[][] getAveragedGridWorld(double[][] gridWorld, int reward, int numRewards) {
        double[][] averagedGridWorld = new double[gridWorld[0].length][gridWorld[0].length];

        double averagedReward = (reward + 1.0 - numRewards) / numRewards;

        for (int i = 0; i < gridWorld[0].length; i++) {
            for (int j = 0; j < gridWorld[0].length; j++) {
                if (gridWorld[i][j] == reward) {
                    averagedGridWorld[i][j] = averagedReward;
                }
            }
        }

        return averagedGridWorld;
    }



    private void runQ() throws MazeException {
        // Proof steps:
        //   1. Run q-learning on each individual grid world. Get the reward. Average rewards for all four gridWorlds.
        //   2. Run q-learning on averaged maze. Get the Q matrix. Return reward.
        //   3. Using the q-matrix, calculate the reward for each individual grid world using that Q matrix and average.
        //       Only use first episode.®
        //   4. Confirm the average in 2 should be same as average out of 3.

        // 2 and 3should be less than 1
        // Does averaged matrix in 2 do better than any single policy found in one?

        // Step 1
        boolean terminateAtGoalState = true;
        int numSteps = 10000;
        int goalValue = 24;
        int stepValue = -1;
        double goalGamma = 0; // averaged problem: 0.75
        double stepGamma = .99;
        int startX = 0;
        int startY = 0;
        double alpha = 0.1;
        double epsilon = 0.95;

        QLearner qLearner = new QLearner(terminateAtGoalState, numSteps, goalValue, goalGamma, stepValue, stepGamma,
                this.gridWorlds, startX, startY, alpha, epsilon);

        qLearner.runQLearner(10000, 0, false);
        double reward = qLearner.getCurrentMaxReward();
        System.out.println("Running q-learning on maze 0. Reward: " + reward);

        double[][][] emptyQ = new double[5][5][4];

        qLearner.setQ(emptyQ);
        qLearner.runQLearner(10000, 1, false);
        double reward1 = qLearner.getCurrentMaxReward();
        System.out.println("- Max reward, maze 1: " + reward1);
        qLearner.setCurrentMaxReward(0);

        qLearner.setQ(emptyQ);
        qLearner.runQLearner(10000, 2, false);
        double reward2 = qLearner.getCurrentMaxReward();
        System.out.println("- Max reward, maze 2: " + reward2);
        qLearner.setCurrentMaxReward(0);

        qLearner.setQ(emptyQ);
        qLearner.runQLearner(10000, 3, false);
        double reward3 = qLearner.getCurrentMaxReward();
        System.out.println("- Max reward, maze 3: " + reward3);
        qLearner.setCurrentMaxReward(0);

        double averagedReward = (reward + reward1 + reward2 + reward3) / 4;
        System.out.println("*** Averaged reward (Step 1): " + averagedReward + " ***");

        qLearner.setQ(emptyQ);
        qLearner.setGoalGamma(0.75);
        qLearner.setTerminateAtGoalState(false);
        qLearner.runQLearner(1000, 4, false);

        double[][][] Q_saved = qLearner.getQ();
        qLearner.setTerminateAtGoalState(true);
//        qLearner.setGoalGamma(0.75);
//        qLearner.runQLearner(1, 4, true);
//        double rewardQ = qLearner.getCurrentMaxReward();
//        System.out.println("- Step 2 reward: " + rewardQ);

        qLearner.setCurrentMaxReward(0);
        qLearner.runQLearner(1, 0, true);
        double rewardB = qLearner.getCurrentMaxReward();
        System.out.println("- Intelligent reward, maze 0: " + rewardB);

        qLearner.setCurrentMaxReward(0);
        qLearner.setQ(Q_saved);
        qLearner.runQLearner(1, 1, true);
        double rewardB2 = qLearner.getCurrentMaxReward();
        System.out.println("- Intelligent reward, maze 1: " + rewardB2);

        qLearner.setCurrentMaxReward(0);
        qLearner.setQ(Q_saved);
        qLearner.runQLearner(1, 2, true);
        double rewardB3 = qLearner.getCurrentMaxReward();
        System.out.println("- Intelligent reward, maze 2: " + rewardB3);

        qLearner.setCurrentMaxReward(0);
        qLearner.setQ(Q_saved);
        qLearner.runQLearner(1, 3, true);
        double rewardB4 = qLearner.getCurrentMaxReward();
        System.out.println("- Intelligent reward, maze 3: " + rewardB4);

        double averagedReward2 = (rewardB + rewardB2 + rewardB3 + rewardB4) / 4;
        System.out.println("*** Averaged reward (Step 2): " + averagedReward2 + " ***");
    }

    /**
     * gets Q matrix by running numTrials of qLearning
     * @param qLearner
     * @throws MazeException
     */
    private void runLearning(QLearner qLearner, int numTrials) throws MazeException {
        double[][][] emptyQ = new double[5][5][4];

        System.out.println("Running q-learning on all four mazes.");
        for (int i = 0; i < 4; i++) {
            qLearner.setQ(emptyQ);
            qLearner.setCurrentMaxReward(0);
            qLearner.runQLearner(numTrials, i, false);
            double[][][] q = qLearner.getQ();
            double reward = qLearner.getCurrentMaxReward();
            System.out.println("Max reward, maze: " + i + ": " + reward);
            double averagedReward = this.runMazesWithQMatrixAndGetAverageReward(qLearner, q);
            System.out.println("Averaged reward across 4 mazes, maze: " + i + ": "  + averagedReward);
        }
    }

    /**
     * will run one episode of q-learning using initial q matrix
     * @param qInitial
     */
    private double runMazesWithQMatrixAndGetAverageReward(QLearner qLearner, double[][][] qInitial) throws MazeException {
        double totalReward = 0;
        for (int i = 0; i < 4; i++) {
            qLearner.setQ(qInitial);
            qLearner.setCurrentMaxReward(0);
            qLearner.runQLearner(1, i, true);
            double reward = qLearner.getCurrentMaxReward();
            totalReward += reward;
            System.out.println(" - Reward for maze " + i + ": " + reward);
        }
        return totalReward / 4;
    }

}