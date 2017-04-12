import java.util.ArrayList;

/**
 * Implements Q-Learning on a grid world.
 */
public class QLearner {
    // When false, run until numRuns
    private boolean terminateAtGoalState;
    private int numSteps;

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
            int numSteps,
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
        this.numSteps = numSteps;
        this.goalValue = goalValue;
        this.goalGamma = goalGamma;
        this.stepValue = stepValue;
        this.stepGamma = stepGamma;
        this.gridWorlds = gridWorlds;
        this.startX = startX;
        this.startY = startY;
        this.alpha = alpha;
        this.epsilon = epsilon;

        Q = new double[gridWorlds.get(0).length][gridWorlds.get(0).length][4];
        learnerUtils = new LearnerUtils();
    }

    public void runQLearner(int runs, int gridWorldIndex, boolean useFirstEpisode) throws MazeException {
        ArrayList<Integer> log = new ArrayList<>();
        int maxReward = -10000;

        double[][] gridWorld = gridWorlds.get(gridWorldIndex);

        // only run once if run will not terminate
        int adjustedRuns = (this.terminateAtGoalState && !useFirstEpisode) ? runs : 1;
        for(int i = 0; i < adjustedRuns; i++) {
            int steps = 0;
            int[] current = new int[]{startX, startY};
            int totalReward = 0;

            // Track visited
            ArrayList<int[]> visited = new ArrayList<>();
            visited.add(current);

            boolean terminate = false;
            if (i == adjustedRuns - 1 && !this.terminateAtGoalState) {
                setCurrentMaxReward(-1000);
            }
            while (!terminate) {
                int[] next = learnerUtils.greedy(current, gridWorld, Q, epsilon);
                int direction = learnerUtils.getDirection(current,next);
                double reward = gridWorld[next[0]][next[1]];

                // if reward hit and program should terminate at goal state, break
                if (reward >= this.goalValue && this.terminateAtGoalState) {
                    terminate = true;
                }

                // otherwise, terminate if steps greater than configured numsteps
                else {
                    if (steps >= this.numSteps) {
                        terminate = true;
                    }
                }

                totalReward += reward;
                updateQ(current[0], current[1],next[0],next[1], direction, reward, gridWorld);
                steps++;
                current = next;
                visited.add(next);
            }

            if (totalReward > maxReward) {
                maxReward = totalReward;
                String path = "";
                for (int[] cell: visited){
                    if (gridWorld[cell[0]][cell[1]] >= this.goalValue) {
                        path += " g:";
                    } else {
                        path += " ";
                    }
                    path += "("  + cell[0] + ", " + cell[1] + ")";
                }
                System.out.println(path);

                optimalPath = visited;
                double calculatedReward = getReward(optimalPath, gridWorld);
                this.currentMaxReward = calculatedReward;
            }
            log.add(steps);
        }
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
    void updateQ(int x, int y, int nextX, int nextY, int direction, double r, double[][] gridWorld) {
        double newGamma;

        if (gridWorld[x][y] >= this.goalValue) {
            newGamma = this.goalGamma;
        } else {
            newGamma = this.stepGamma;
        }

        Q[x][y][direction] += alpha * (r + newGamma * learnerUtils.maxQVal(nextX, nextY, Q) - Q[x][y][direction]);
    }

    public ArrayList<int[]> getOptimalPath() {
        return optimalPath;
    }

    public double getStepGamma() {
        return stepGamma;
    }

    public void setStepGamma(int stepGamma) {
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
