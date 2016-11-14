import java.util.ArrayList;

/**
 * Implements q-Learning
 * Hits 7 steps consistently at about 851 runs
 * Created by emresonmez on 9/21/15.
 */
public class QLearner {
    private int goalReward;
    private int stepPenalty;
    private int[][] maze;
    private double[][][] Q; // 0 1 2 3 for north, east, south, west
    private int startX;
    private int startY;
    private double alpha = 0.1;
    private double gamma = 0.95;
    private double epsilon = 15;
    private LearnerUtils learnerUtils;


    public QLearner(int reward, int step, int[][] maze) {
        this.goalReward = reward;
        this.stepPenalty = step;
        this.maze = maze;
        Q = new double[maze.length][maze.length][4];
        startX = 0;
        startY = 0;
        learnerUtils = new LearnerUtils();
    }

    /**
     * runs Q learning n times
     * @param runs
     * @throws MazeException
     */
    public ArrayList<Integer> qLearning(int runs) throws MazeException {
        ArrayList<Integer> log = new ArrayList<>();
        int maxReward = -10000;
        for(int i = 0; i < runs; i++) {
            int steps = 0;
            int[] current = new int[2];
            current[0] = startX;
            current[1] = startY;
            int totalReward = 0;
            while (maze[current[0]][current[1]] != 100) {
                int[] next = learnerUtils.greedy(current, maze,Q,epsilon);
                int direction = learnerUtils.getDirection(current,next);

                int reward = learnerUtils.getReward(next[0], next[1],maze,stepPenalty,goalReward);
                totalReward += reward;
                updateQ(current[0], current[1],next[0],next[1],direction, reward);
                steps++;
                current = next;
            }
            if (totalReward + 100 > maxReward) {
                maxReward = totalReward + 100;
                System.out.println("Path found with reward: " + maxReward + " (steps: " + steps + ")");
            }
            log.add(steps);
        }
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

        if (maze[x][y] == 100) {
            newGamma = 3/4;
        } else {
            newGamma = .99;
        }

        Q[x][y][direction] += alpha * (r + newGamma * learnerUtils.maxQVal(nextX,nextY,Q) - Q[x][y][direction]);
    }


}
