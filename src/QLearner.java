import java.util.ArrayList;

/**
 * Implements Q-Learning on a grid world.
 */
public class QLearner {
    // When false, run until numRuns
    private boolean terminateAtGoalState;
    private int maxSteps;

    // Reward and gamma values
    private int goalValue;
    private double goalGamma;
    private int stepValue;
    private double stepGamma;

    private double currentMaxReward = 0;

    // Grid
    private ArrayList<double[][]> gridWorlds;

    // Starting coordinates
    private int startX;
    private int startY;

    // Q matrix
    private double[][][] Q; // 0 1 2 3 for north, east, south, west

    private ArrayList<int[]> optimalPath;


    // learning constants
    private double alpha;
    private double epsilon;

    private LearnerUtils learnerUtils;


    public QLearner(
            boolean terminateAtGoalState,
            int maxSteps,
            int goalValue,
            double goalGamma,
            int stepValue,
            double stepGamma,
            ArrayList<double[][]> gridWorlds,
            int startX,
            int startY,
            double alpha,
            double epsilon) {
        this.terminateAtGoalState = terminateAtGoalState;
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
        learnerUtils = new LearnerUtils();
    }

    public void runQLearner(int runs, int gridWorldIndex, boolean useFirstEpisode) throws MazeException {
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
                int[] next = learnerUtils.greedy(current, gridWorld, Q, epsilon);

                // get direction of next state compared to current state
                Direction direction = learnerUtils.getDirection(current,next);

                // get reward of moving to next state
                double reward = gridWorld[next[0]][next[1]];

                // if goal hit & program should terminate at goal, exit loop
                if (reward >= this.goalValue && this.terminateAtGoalState) {
                    terminate = true;
                } else {
                    // terminate if trial has reached maxSteps
                    terminate = (steps >= this.maxSteps);
                }

                // if current is goal, weight old reward with goalGamma
                boolean currentIsGoal = (gridWorld[current[0]][current[1]] >= this.goalValue);
                double weightedOldReward = (currentIsGoal) ? trialReward*this.goalGamma : trialReward*this.stepGamma;

                trialReward = weightedOldReward + reward;
//                System.out.println("moving from " + current[0] + ", " + current[1] + " to " + next[0] + ", " + next[1] + " reward: " + reward);

                updateQ(current[0], current[1],next[0],next[1], direction, reward, gridWorld);
                steps++;
                current = next;
                visited.add(next);
//                printQ();
            }

            if (trialReward > maxReward) {
                maxReward = trialReward;
//                printPath(visited, gridWorld);
                optimalPath = visited;
//                System.out.println("Reward " + maxReward);
                double calculatedReward = getReward(optimalPath, gridWorld);
                if (useFirstEpisode) {
                    if (visited.size() > 1000) {
                        System.out.println("-------- num steps:" + visited.size());
                    }
                }
//                System.out.println("Calculated reward " + calculatedReward);
//                printQ();
                this.currentMaxReward = calculatedReward;
            }
//            System.out.println(totalReward);
            log.add(steps);
        }
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
//        System.out.println("Q value at " + x + ", " + y + ", " + direction + ": " + Q[x][y][directionValue] + " ~ update value: " + updateValue);
    }

    public void printQ() {
        int[][] numAdded = new int[5][5];
        double[][] heatMap = new double[5][5];
        double[][][] q = this.Q;

        int qSize = q[0].length;

        for (int i = 0; i < qSize; i++) { // rows
            for (int j = 0; j < qSize; j++) { // columns
                if (i - 1 >= 0) { // up
                    heatMap[i-1][j] += q[i][j][0];
                    if (heatMap[i-1][j] > Double.NEGATIVE_INFINITY) {
                        numAdded[i-1][j] += 1;
                    }
                }

                if (i + 1 < qSize) { // down
                    heatMap[i+1][j] += q[i][j][2];
                    if (heatMap[i+1][j] > Double.NEGATIVE_INFINITY) {
                        numAdded[i+1][j] += 1;
                    }
                }

                if (j - 1 >= 0) { // left
                    heatMap[i][j-1] += q[i][j][3];
                    if (heatMap[i][j-1] > Double.NEGATIVE_INFINITY) {
                        numAdded[i][j-1] += 1;
                    }
                }

                if (j + 1 < qSize) { // right
                    heatMap[i][j+1] += q[i][j][1];
                    if (heatMap[i][j+1] > Double.NEGATIVE_INFINITY) {
                        numAdded[i][j+1] += 1;
                    }
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

    private void printGridWorld(double[][] gridWorld) {
        for (int i = 0; i < gridWorld[0].length; i++) {
            System.out.printf("%6.2f | %6.2f | %6.2f | %6.2f | %6.2f", gridWorld[i][0], gridWorld[i][1], gridWorld[i][2], gridWorld[i][3], gridWorld[i][4]);
            System.out.println(" ");
        }
    }

    public void resetSystem() {
        resetQ();
        setCurrentMaxReward(-111);
    }

    private void resetQ() {
        int length = gridWorlds.get(0).length;
        Q = new double[length][length][4];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < 4; k++) {
                    Q[i][j][k] = 0;
                }
            }
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

    public boolean isTerminateAtGoalState() {
        return terminateAtGoalState;
    }

    public void setTerminateAtGoalState(boolean terminateAtGoalState) {
        this.terminateAtGoalState = terminateAtGoalState;
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

    public void setCurrentMaxReward(int currentMaxReward) {
        this.currentMaxReward = currentMaxReward;
    }

    public double[][][] getQ() {
        return this.Q;
    }

    public void setQ(double[][][] Q) {
        this.Q = Q;
    }
}
