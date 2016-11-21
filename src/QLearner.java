import java.util.ArrayList;

/**
 * Implements Q-Learning on a grid world.
 * Initial implementation: 9/21/15
 * Updated implementation: 11/20/16
 */
public class QLearner {
    private int goalThreshold;
    private int stepValue;
    private int numGoals;

    private int startX;
    private int startY;

    private int[][] gridWorld;
    private double[][][] Q; // 0 1 2 3 for north, east, south, west

    // learning constants
    private double alpha;
    private double gamma;
    private double epsilon;

    private LearnerUtils learnerUtils;

    public QLearner(double epsilon, double gamma, double alpha, int[][] gridWorld,
                    int stepValue, int startX, int startY, int goalValue) {
        // for defaults, use 0.1, 15, 0.95 for alpha, gamma, epsilon respectively
        this.epsilon = epsilon;
        this.gamma = gamma;
        this.alpha = alpha;
        this.gridWorld = gridWorld;
        this.stepValue = stepValue;
        this.startX = startX;
        this.startY = startY;
        this.goalThreshold = goalValue;
        this.numGoals = getNumGoals();

        Q = new double[gridWorld.length][gridWorld.length][4];
        learnerUtils = new LearnerUtils();
    }

    private int getNumGoals() {
        int count = 0;
        for(int i = 0; i < gridWorld.length; i++) {
            for (int j = 0; j < gridWorld.length; j++) {
                if (gridWorld[i][j] >= goalThreshold) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * runs Q learning n times, terminates a run when all goal states have been hit
     * @param runs
     * @throws MazeException
     */
    public ArrayList<Integer> qLearning(int runs) throws MazeException {
        ArrayList<Integer> log = new ArrayList<>();
        int maxReward = 0;
        for(int i = 0; i < runs; i++) {
            int steps = 0;
            int[] current = new int[]{startX, startY};
            int totalReward = 0;
            // keep track of goals reached
            ArrayList<int[]> goalsHit = new ArrayList<>();
            ArrayList<int[]> visited = new ArrayList<>();
            visited.add(current);
            // iterate until all goals have been hit
            while (goalsHit.size() < numGoals) {
                int[] next = learnerUtils.greedy(current, gridWorld,Q,epsilon);
                int direction = learnerUtils.getDirection(current,next);
                int reward = gridWorld[next[0]][next[1]];
                if (reward == goalThreshold && !goalsHit.contains(next)) {
                    goalsHit.add(next);
                }
                totalReward += reward;
                updateQ(current[0], current[1],next[0],next[1], direction, reward);
                steps++;
                current = next;
                visited.add(next);
            }
            if (totalReward > maxReward) {
                maxReward = totalReward;
                System.out.println("Path found with reward: " + maxReward + " (steps: " + steps + ")");
                String path = "";
                for (int[] cell: visited){
                    if (gridWorld[cell[0]][cell[1]] == goalThreshold) {
                        path += " g:";
                    } else {
                        path += " ";
                    }
                    path += "("  + cell[0] + ", " + cell[1] + ")";
                }
                System.out.println(path);
            }
            log.add(steps);
        }
        System.out.println();
        return log;
    }

    /**
     * updates Q matrix
     * @param x
     * @param y
     * @param direction
     * @param r
     */
    void updateQ(int x, int y, int nextX, int nextY, int direction, int r) {
        double newGamma;

        if (gridWorld[x][y] == 100) {
            newGamma = 3/4;
        } else {
            newGamma = .99;
        }

        Q[x][y][direction] += alpha * (r + newGamma * learnerUtils.maxQVal(nextX,nextY,Q) - Q[x][y][direction]);
    }


}
