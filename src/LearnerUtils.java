import java.util.ArrayList;
import java.util.Random;

/**
 * Created by emresonmez on 11/9/15.
 */
public class LearnerUtils {
    /**
     * gets next cell from direction
     * @param x
     * @param y
     * @param direction
     * @return
     */
    protected int[] nextCell(int x, int y, int direction, int[][] maze) throws MazeException {
        int[] ret = new int[2];
        switch (direction) {
            case 0:
                ret[0] = x-1;
                ret[1] = y;
                break;
            case 1:
                ret[0] = x;
                ret[1] = y+1;
                break;
            case 2:
                ret[0] = x+1;
                ret[1] = y;
                break;
            case 3:
                ret[0] = x;
                ret[1] = y-1;
                break;
        }
        if(maze[ret[0]][ret[1]] == 1){
            throw new MazeException("next cell error for (" + x + ", " + y + ", " + direction + ")");
        }
        return ret;
    }

    protected int randomInt(int limit, Random r){
        return r.nextInt(limit);
    }

    protected ArrayList<int[]> getValidCells(int x, int y, int[][] maze) {
        ArrayList<int[]> cells = new ArrayList<>();
        if(x+1 < maze.length){
            int[] ret = new int[2];
            ret[0] = x+1;
            ret[1] = y;
            if(maze[ret[0]][ret[1]] != 1){
                cells.add(ret);
            }
        }
        if(x-1 >= 0){
            int[] ret = new int[2];
            ret[0] = x-1;
            ret[1] = y;
            if(maze[ret[0]][ret[1]] != 1){
                cells.add(ret);
            }
        }
        if(y+1 < maze.length){
            int[] ret = new int[2];
            ret[0] = x;
            ret[1] = y+1;
            if(maze[ret[0]][ret[1]] != 1){
                cells.add(ret);
            }
        }
        if(y-1 >= 0){
            int[] ret = new int[2];
            ret[0] = x;
            ret[1] = y-1;
            if(maze[ret[0]][ret[1]] != 1){
                cells.add(ret);
            }
        }
        return cells;
    }

    /**
     * returns direction
     * @param from
     * @param to
     * @return
     */
    protected int getDirection(int[] from, int[] to) throws MazeException {
        assert(!from.equals(to));
        if (from[0] > to[0]) { // up
            return 0;
        }
        if (from[1] < to[1]) { // right
            return 1;
        }
        if (from[0] < to[0]) { // down
            return 2;
        }
        if (from[1] > to[1]) { // left
            return 3;
        }
        System.out.println("from: " + from[0] + " " + from[1] + " to: " + to[0] + " " + to[1]);
        throw new MazeException("Invalid direction.");
    }

    /**
     * returns cell with max q value
     * breaks ties randomly
     * @param from
     * @param maze
     * @param Q
     * @return
     * @throws MazeException
     */
    protected int[] maxQCell(int[] from, int[][] maze, double[][][] Q) throws MazeException {
        int[] ret = new int[2];
        double max = Double.NEGATIVE_INFINITY;
        ArrayList<int[]> valid = getValidCells(from[0],from[1],maze);
        Random r = new Random();
        if(Q != null){
            for(int[] k:valid){
                double qValue = Q[from[0]][from[1]][getDirection(from,k)];
                if(qValue > max){
                    max = qValue;
                    ret = k;
                }
                if(qValue == max){ // pick randomly
                    int randomInt = randomInt(1,r);
                    if(randomInt == 1){ // replace
                        max = qValue;
                        ret = k;
                    }
                }
            }
        }else{ // if q is null pick randomly as well
            int randomInt = 0 + (int)(Math.random()*valid.size());
            ret = valid.get(randomInt);
        }
        return ret;
    }

    protected double maxQVal(int x, int y, double[][][] Q) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            double val = Q[x][y][i];
            if (val > max){
                max = val;
            }
        }
        return max;
    }

    /**
     * reward for going to a certain cell
     * @TODO replace 0 and 9 with vars
     * @param x
     * @param y
     * @return
     */
    protected int getReward(int x, int y, int[][] maze, int step, int reward) throws MazeException {
        if(maze[x][y] == 0) {
            return 0;
        }
        if(maze[x][y] == -1){
            return step;
        }
        if(maze[x][y] == 100){
            return reward;
        }
        throw new MazeException("Invalid cell to obtain reward from (" + x + ", " + y + ")");
    }

    protected int[] greedy(int[] cell, int[][] maze, double[][][] Q, double epsilon) throws MazeException {
        int randomNum = randomInt(100, new Random());
        if(randomNum < epsilon) {
            ArrayList<int[]> validCells = getValidCells(cell[0],cell[1],maze);
            int randomNum2 = randomInt(validCells.size(),new Random());
            return validCells.get(randomNum2);
        }else {
            return maxQCell(cell,maze,Q);
        }
    }
}
