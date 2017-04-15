import java.util.ArrayList;

/**
 * Created by emresonmez on 11/9/15.
 */
public class Main {
    private FileUtils fileUtils;
    private ArrayList<double[][]> gridWorlds;
    private double[][] averagedMaze;
    private int numRuns;

    public Main(){
        fileUtils = new FileUtils();
        this.populateGridWorlds();
        this.numRuns = 10;
    }

    /**
     * Create four grid worlds (each with one goal state) and one grid world
     * that is the average of the four original grid worlds.
     */
    private void populateGridWorlds() {
        double[] startRow = {-1,-1,-1,-1,-1};
        double[] emptyRow = {-1,-1,-1,-1,-1};
        double[] reward1 = {-1,100,-1,-1,-1};
        double[] reward2  = {-1,-1,-1,-1,100};
        double[] reward3 = {100,-1,-1,-1,-1};
        double[] reward4 = {-1,-1,-1,100,-1};

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
     * Get an averaged grid world from the four original grid worlds.
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
                } else {
                    averagedGridWorld[i][j] = -1;
                }
            }
        }

        return averagedGridWorld;
    }


    /**
     * Execute the following algorithm:
     *   1) Run q-learning on each individual grid world. Get the q matrix.
     *      Using this q matrix to initialize the qLearner, get the reward
     *      of one episode for each of the four grid worlds. Average the
     *      four rewards.
     *
     *   2) Run q-learning on the averaged grid world. Get the q matrix.
     *      Using this q matrix to initialize the qLearner, get the reward
     *      of one episode for each of the four grid worlds. Average the
     *      four rewards.
     *
     * The goal is to figure out whether the policy generated in (2) does
     * better, on average, than any policy generated in (1).
     *
     * @throws MazeException
     */
    private void runQ() throws MazeException {
        boolean terminateAtGoalState = true; // exit episode if goal state is reached
        int numSteps = 50000;
        int goalValue = 22; // this is the minimum threshold that defines a goal
        int stepValue = -1;
        double goalGamma = 0;
        double stepGamma = .99;
        int startX = 0;
        int startY = 0;
        double alpha = 0.1;
        double epsilon = 0.1;

        QLearner qLearner = new QLearner(numSteps, goalValue, goalGamma, stepValue, stepGamma,
                this.gridWorlds, startX, startY, alpha, epsilon);

        // print grid worlds
//        this.printGridWorlds();
        qLearner.resetSystem();

        for (int i = 0; i < 5; i++) {
            runQLearningOnSingleGridWorld(i, qLearner, 100000);
        }


        // Part 1. Run learning on individual grid worlds with 1000 runs.
        this.runLearningOnIndividualGridWorlds(qLearner, 50);

        qLearner.setGoalGamma(0);
        qLearner.setStepGamma(.99);
        // Part 2. Run learning on averaged grid world.
        this.runLearningOnAveragedGridWorld(qLearner);
    }

    // use this for debugging
    private void runQLearningOnSingleGridWorld(int testGridWorldIndex, QLearner qLearner, int numTrials) throws MazeException {
        qLearner.resetSystem();
        System.out.println("Running q learning on grid world " + testGridWorldIndex);
        // run q learner with numTrials, terminate at goal state for each trial
        qLearner.runQLearner(numTrials, testGridWorldIndex, false, true);

        double[][][] q = qLearner.getQ();
        printGridWorldByIndex(testGridWorldIndex);
        qLearner.printQ(q);
//        System.out.println("Reward: " + qLearner.getCurrentMaxReward());
    }


    /**
     * 1. Gets Q matrix by running numTrials of qLearning on individual grid worlds.
     * 2. Uses this matrix as a policy for each of the four grid worlds and gets the
     *    reward of the policy for each one. The four rewards are averaged.
     * 3. Part 2 is executed 100 times and the average is returned.
     *
     * @param qLearner
     * @param numTrials
     * @throws MazeException
     */
    private void runLearningOnIndividualGridWorlds(QLearner qLearner, int numTrials) throws MazeException {
        double[][][] emptyQ = new double[5][5][4];

        System.out.println("Running q-learning on all four mazes.");
        for (int i = 0; i < 4; i++) {
            qLearner.resetSystem();

            qLearner.runQLearner(numTrials, i, false, true);

            double[][][] q = qLearner.getQ();
            printGridWorldByIndex(i);

            qLearner.printQ(q);
            double reward = qLearner.getCurrentMaxReward();
            double averagedReward = this.runMazesWithQMatrixAndGetAverageReward(qLearner, q, this.numRuns);
            System.out.println("Averaged reward across 4 mazes, policy generated from maze " + i + ": " + averagedReward);
            System.out.println(" ");
            qLearner.resetSystem();
        }
    }

    /**
     * 1. Gets Q matrix by running numTrials of qLearning on
     *    averaged grid world.
     * 2. Uses this matrix as a policy for each of the four
     *    grid worlds and gets the reward of the policy for
     *    each one. The four rewards are averaged.
     * 3. Part 2 is executed 100 times and the average is
     *    returned.
     *
     * @param qLearner
     * @throws MazeException
     */
    private void runLearningOnAveragedGridWorld(QLearner qLearner) throws MazeException {
        qLearner.resetSystem();

        double averagedGoalGamma = .99 * 3 / 4;
        qLearner.setGoalGamma(averagedGoalGamma);

        qLearner.runQLearner(numRuns, 4, false, false);

        double[][][] qSaved = qLearner.getQ();
        printGridWorldByIndex(4);
        printQ(qSaved);

        double averagedReward = runMazesWithQMatrixAndGetAverageReward(qLearner, qSaved, this.numRuns);
        System.out.println("Averaged reward across 4 mazes, policy generated from average of 4 mazes: " + averagedReward);
        System.out.println(" ");
    }

    /**
     * 1. Uses a Q matrix to initialize the learner and then calculates
     *    the reward of using this policy on each of the four grid worlds.
     * 2. The four rewards are averaged.
     * 3. Steps 1 and 2 are repeated numRuns times and the average of the
     *    runs is returned.
     *
     * @param qLearner
     * @param qInitial
     * @param numRuns
     * @return
     * @throws MazeException
     */
    private double runMazesWithQMatrixAndGetAverageReward(QLearner qLearner, double[][][] qInitial, int numRuns) throws MazeException {
        double cumulativeRewardSum = 0;

        for (int j = 0; j < numRuns; j++) {
            double totalReward = 0;
            for (int i = 0; i < 4; i++) {
                qLearner.resetSystem();
                qLearner.setQ(qInitial);
                qLearner.runQLearner(1, i, true, true);
                double reward = qLearner.getCurrentMaxReward();
                totalReward += reward;
                qLearner.resetSystem();
            }
            double averagedTotalReward = totalReward / 4.0;
            cumulativeRewardSum += averagedTotalReward;
        }

        return cumulativeRewardSum / numRuns;
    }

    private void printGridWorlds() {
        for (int i = 0; i < this.gridWorlds.size(); i++) {
            printGridWorldByIndex(i);
        }
    }

    private void printGridWorldByIndex(int index) {
        System.out.println(" ");
        System.out.println("Grid world: " + index);
        double[][] gridWorld = gridWorlds.get(index);
        printGridWorld(gridWorld);
        System.out.println(" ");
    }

    private void printGridWorld(double[][] gridWorld) {
        for (int i = 0; i < gridWorld[0].length; i++) {
            System.out.printf("%6.2f | %6.2f | %6.2f | %6.2f | %6.2f", gridWorld[i][0], gridWorld[i][1], gridWorld[i][2], gridWorld[i][3], gridWorld[i][4]);
            System.out.println(" ");
        }
    }

    private void printQ(double[][][] q) {
        int[][] numAdded = new int[5][5];
        double[][] heatMap = new double[5][5];

        int qSize = q[0].length;
        // up (0) down (1) left (2) right (3)
        for (int i = 0; i < qSize; i++) { // rows
            for (int j = 0; j < qSize; j++) { // columns
                if (i - 1 >= 0) { // up
                    heatMap[i-1][j] += q[i][j][0];
                    numAdded[i-1][j] += 1;
                }

                if (i + 1 < qSize) { // down
                    heatMap[i+1][j] += q[i][j][2];
                    numAdded[i+1][j] += 1;
                }

                if (j - 1 >= 0) { // left
                    heatMap[i][j-1] += q[i][j][3];
                    numAdded[i][j-1] += 1;
                }

                if (j + 1 < qSize) { // right
                    heatMap[i][j+1] += q[i][j][1];
                    numAdded[i][j+1] += 1;
                }
            }
        }

        for (int u = 0; u < heatMap.length; u++) {
            for (int v = 0; v < heatMap.length; v++) {
                heatMap[u][v] = heatMap[u][v] / numAdded[u][v];
            }
        }
        System.out.println("Q heat map:");
        printGridWorld(heatMap);
        System.out.println(" ");
    }

    public static void main(String[] args) throws MazeException {
        Main main = new Main();
        main.runQ();
    }

}