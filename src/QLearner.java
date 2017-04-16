import java.util.ArrayList;

/**
 * Implements Q-Learning on a grid world.
 */
public class QLearner {
    // When false, run until numRuns
    private int maxSteps;

    // Reward and gamma values
    private int goalValue;
    private double goalGamma;
    private int stepValue;
    private double stepGamma;

    private double currentMaxReward = -1000;

    // Grid
    private ArrayList<double[][]> gridWorlds;

    // Starting coordinates
    private int startX;
    private int startY;

    // Q matrix
    private double[][][] Q; // 0 1 2 3 for north, east, south, west
    private ArrayList<double[][][]> qMatrices;

    private ArrayList<int[]> optimalPath;


    // learning constants
    private double alpha;
    private double epsilon;

    private LearnerUtils learnerUtils = new LearnerUtils();


    public QLearner(
            int maxSteps,
            int goalValue,
            double goalGamma,
            int stepValue,
            double stepGamma,
            ArrayList<double[][]> gridWorlds,
            int startX,
            int startY,
            double alpha,
            double epsilon) throws MazeException {
        this.maxSteps = maxSteps;
        this.goalValue = goalValue;
        this.goalGamma = goalGamma;
        this.stepValue = stepValue;
        this.stepGamma = stepGamma;
        this.gridWorlds = gridWorlds;
        this.startX = startX;
        this.startY = startY;
        this.alpha = alpha;
        this.epsilon = epsilon;

        resetSystem();
        Q = new double[gridWorlds.get(0).length][gridWorlds.get(0).length][4];
    }

    public double runQLearner(int runs, int gridWorldIndex, boolean useFirstEpisode, boolean terminateAtGoalState, boolean useAveragedPolicy) throws MazeException {
        ArrayList<Integer> log = new ArrayList<>();
        double maxReward = -10000;

        double[][] gridWorld = gridWorlds.get(gridWorldIndex);

        // Get number of times to run q-learning. 1 if only using first episode
        int numRuns = useFirstEpisode ? 1 : runs;

        for(int i = 0; i < numRuns; i++) {
            int steps = 0;

            int xStart = useFirstEpisode ? 0 : getStartCell();
            int yStart = useFirstEpisode ? 0: getStartCell();
            int[] current = new int[]{xStart, yStart};
            double trialReward = 0;

            // Track visited
            ArrayList<int[]> visited = new ArrayList<>();
            visited.add(current);

            boolean terminate = false;

            while (!terminate) {
                // get next state from greedy state selection
                int[] next;
                if (useAveragedPolicy) {
                    next = learnerUtils.greedyWithMultipleOptions(current, gridWorld, this.qMatrices, this.epsilon);
                } else {
                    next = learnerUtils.greedy(current, gridWorld, Q, this.epsilon);
                }

                // get direction of next state compared to current state
                Direction direction = learnerUtils.getDirection(current,next);

                // get reward of moving to next state
                double reward = gridWorld[next[0]][next[1]];

                // if goal hit & program should terminate at goal, exit loop
                if (reward >= this.goalValue && terminateAtGoalState) {
                    terminate = true;

                    // uncomment for debugging
//                    System.out.println("Run about to terminate.");
                } else {
                    // terminate if trial has reached maxSteps
                    terminate = (steps >= this.maxSteps);
                }

                // if current is goal, weight old reward with goalGamma
                boolean currentIsGoal = (gridWorld[current[0]][current[1]] >= this.goalValue);
                double weightedOldReward = (currentIsGoal) ? trialReward*this.goalGamma : trialReward*this.stepGamma;

                trialReward = weightedOldReward + reward;

                // uncomment for debugging
//                System.out.println("moving from " + current[0] + ", " + current[1] + " to " + next[0] + ", " + next[1] + " reward: " + reward);
                updateQ(current[0], current[1],next[0],next[1], direction, reward, gridWorld);

                steps++;
                current = next;
                visited.add(next);

                // uncomment for debugging
//                printQ();
            }

            if (trialReward > maxReward) {
                maxReward = trialReward;
                optimalPath = visited;

                double calculatedReward = getReward(optimalPath, gridWorld);
                if (useFirstEpisode) {
                    if (visited.size() > 100) {
//                        System.out.println("-------- num steps:" + visited.size());
                    }
                }
//                System.out.println("Num steps in current max reward run " + visited.size());
                // uncomment below for debugging
//                System.out.println("Reward " + maxReward);
//                printPath(visited, gridWorld);
//                System.out.println("----------------- Visited " + visited.size());
//                System.out.println("----------------- Calculated reward " + calculatedReward);
//                printQ();
//                this.currentMaxReward = calculatedReward;
            }
            log.add(steps);
        }
        return maxReward;
    }

    private int getStartCell() {
        int randomNum = 0 + (int)(Math.random() * 4);
        return randomNum;
    }

    private void printPath(ArrayList<int[]> path, double[][] gridWorld) {
        String pathString = "";
        for (int[] cell: path){
            if (gridWorld[cell[0]][cell[1]] >= this.goalValue) {
                pathString += " g:";
            } else {
                pathString += " ";
            }
            pathString += "("  + cell[0] + ", " + cell[1] + ")";
        }
        System.out.println(pathString);
        System.out.println("Steps in this iteration: " + path.size());
    }

    /**
     * given path, will calculate reward with gammas of traveling along that path
     * @param path
     * @param myGrid
     * @return
     */
    public double getReward(ArrayList<int[]> path, double[][] myGrid) {
        double reward = 0.0;
        double runningGamma = 1;
        for (int[] cell : path) {
            int x = cell[0];
            int y = cell[1];
            double value = myGrid[x][y];
//            System.out.print(value + " ");
            if (value >= this.goalValue) {
                runningGamma = this.goalGamma * runningGamma;
            } else {
                runningGamma = this.stepGamma * runningGamma;
            }

            if (runningGamma == 0) {
                reward += value;
            } else {
                reward += runningGamma * value;
            }
        }
        return reward;
    }

    /**
     * updates Q matrix
     * @param x
     * @param y
     * @param direction
     * @param r
     */
    void updateQ(int x, int y, int nextX, int nextY, Direction direction, double r, double[][] gridWorld) throws MazeException {
        double newGamma;

        if (gridWorld[x][y] >= this.goalValue) {
            newGamma = this.goalGamma;
        } else {
            newGamma = this.stepGamma;
        }

        int directionValue = learnerUtils.getDirectionValue(direction);
        double adjustment = (Q[x][y][directionValue] > Double.NEGATIVE_INFINITY) ? Q[x][y][directionValue] : 0;
        double updateValue = alpha * (r + newGamma * learnerUtils.maxQVal(nextX, nextY, Q) - adjustment);
        Q[x][y][directionValue] += updateValue;

        // uncomment below for debugging
//        System.out.println("Q value at " + x + ", " + y + ", " + direction + ": " + Q[x][y][directionValue] + " ~ update value: " + updateValue);
    }

    public void printQ() {
        printQ(this.Q);
    }

    public void printQ(double[][][] q) {
        int qSize = q[0].length;

        for (int i = 0; i < qSize; i++) { // rows
            String row = "";
            ArrayList<double[]> cells = new ArrayList<>();
            for (int j = 0; j < qSize; j++) { // columns
                double[] cellVals = new double[4];
                if (i - 1 >= 0) { // up
                    cellVals[0] = q[i][j][0];
                }

                if (j + 1 < qSize) { // right
                    cellVals[1] = q[i][j][1];
                }

                if (i + 1 < qSize) { // down
                    cellVals[2] = q[i][j][2];
                }

                if (j - 1 >= 0) { // left
                    cellVals[3] = q[i][j][3];
                }
                cells.add(cellVals);
            }
            double[] cell0 = cells.get(0);
            double[] cell1 = cells.get(1);
            double[] cell2 = cells.get(2);
            double[] cell3 = cells.get(3);
            double[] cell4 = cells.get(4);

            System.out.printf("u:%6.2f r:%6.2f d:%6.2f l:%6.2f | u:%6.2f r:%6.2f d:%6.2f l:%6.2f | u:%6.2f r:%6.2f d:%6.2f l:%6.2f | u:%6.2f r:%6.2f d:%6.2f l:%6.2f | u:%6.2f r:%6.2f d:%6.2f l:%6.2f",
                    cell0[0], cell0[1], cell0[2], cell0[3],
                    cell1[0], cell1[1], cell1[2], cell1[3],
                    cell2[0], cell2[1], cell2[2], cell2[3],
                    cell3[0], cell3[1], cell3[2], cell3[3],
                    cell4[0], cell4[1], cell4[2], cell4[3]);

            System.out.println(row);
        }
        System.out.println(" ");
    }

    private void printGridWorld(double[][] gridWorld) {
        for (int i = 0; i < gridWorld[0].length; i++) {
            System.out.printf("%6.2f | %6.2f | %6.2f | %6.2f | %6.2f", gridWorld[i][0], gridWorld[i][1], gridWorld[i][2], gridWorld[i][3], gridWorld[i][4]);
            System.out.println(" ");
        }
    }

    public void resetSystem() throws MazeException {
        resetQ();
        setCurrentMaxReward(-1000);
    }

    private void resetQ() throws MazeException {
        int length = gridWorlds.get(0).length;
        Q = new double[length][length][4];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < 4; k++) {
                    Q[i][j][k] = 0;
                }
            }
        }
        // sanity check
        if (!learnerUtils.qEqual(new double[5][5][4], this.Q)) {
            throw new MazeException("Q matrix was not reset properly.");
        }
    }

    public ArrayList<int[]> getOptimalPath() {
        return optimalPath;
    }

    public double getStepGamma() {
        return stepGamma;
    }

    public void setStepGamma(double stepGamma) {
        this.stepGamma = stepGamma;
    }

    public double getGoalGamma() {
        return goalGamma;
    }

    public void setGoalGamma(double goalGamma) {
        this.goalGamma = goalGamma;
    }

    public int getGoalValue() {
        return goalValue;
    }

    public void setGoalValue(int goalValue) {
        this.goalValue = goalValue;
    }

    public int getStepValue() {
        return stepValue;
    }

    public void setStepValue(int stepValue) {
        this.stepValue = stepValue;
    }

    public double getCurrentMaxReward() {
        return currentMaxReward;
    }

    public void setCurrentMaxReward(double currentMaxReward) {
        this.currentMaxReward = currentMaxReward;
    }

    public double[][][] getQ() {
        return this.Q;
    }

    public void setQ(double[][][] Q) {
        this.Q = Q;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public void setQMatrices(ArrayList<double[][][]> qMatrices) {
        this.qMatrices = qMatrices;
    }

    public ArrayList<double[][][]> getQMatrices() {
        return this.qMatrices;
    }

    public LearnerUtils getLearnerUtils() {
        return this.learnerUtils;
    }
}
