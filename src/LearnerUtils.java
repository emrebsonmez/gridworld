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

    protected int[] nextCell(int x, int y, Direction direction, int[][] maze) throws MazeException {
        int[] ret = new int[2];
        switch (direction) {
            // up
            case UP:
                ret[0] = x-1;
                ret[1] = y;
                break;
            // right
            case RIGHT:
                ret[0] = x;
                ret[1] = y+1;
                break;
            // down
            case DOWN:
                ret[0] = x+1;
                ret[1] = y;
                break;
            // left
            case LEFT:
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

    protected ArrayList<int[]> getValidCells(int x, int y, double[][] maze) {
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
//        System.out.println("for cell " + x + ", " + y);
//        for (int[] c: cells) {
//            System.out.print(" " + c[0] + " " + c[1] + " | ");
//        }
        return cells;
    }

    /**
     * returns direction
     * @param from
     * @param to
     * @return
     */
    protected Direction getDirection(int[] from, int[] to) throws MazeException {
        if (from.equals(to)) {
            throw new MazeException("From and to cannot be equivalent.");
        }

        if (from[0] > to[0]) { // up
            return Direction.UP;
        }
        if (from[1] < to[1]) { // right
            return Direction.RIGHT;
        }
        if (from[0] < to[0]) { // down
            return Direction.DOWN;
        }
        if (from[1] > to[1]) { // left
            return Direction.LEFT;
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
    protected int[] maxQCell(int[] from, double[][] maze, double[][][] Q) throws MazeException {
        int[] ret = new int[2];
        Random r = new Random();

        ArrayList<int[]> validCells = getValidCells(from[0],from[1],maze);
        double maxQValue = getMaxQValue(from, validCells, Q);
        ArrayList<int[]> cellsWithMaxQValue = getCellsWithMaxQValue(from, validCells, Q);

        int numChoices = cellsWithMaxQValue.size();

        if (numChoices == 0) {
            throw new MazeException("No choices exist for moving from " + from[0] + ", " + from[1]);
        }

        int[] maxQCell = pickCellAtRandom(cellsWithMaxQValue);
        if (maxQCell[0] == from[0] && maxQCell[1] == from[1]) {
            throw new MazeException("from and max q cell are same");
        }
        return maxQCell;
    }

    /**
     * Given list of cells & starting point, return array list of cells that have maxQValue.
     * @param from
     * @param cells
     * @param Q
     * @return
     * @throws MazeException
     */
    private ArrayList<int[]> getCellsWithMaxQValue(int[] from, ArrayList<int[]> cells, double[][][] Q) throws MazeException {
        ArrayList<int[]> maxQValueCells = new ArrayList<>();

        double maxQValue = getMaxQValue(from, cells, Q);
        for (int[] cell:cells) {
            double qValue = getQValueForTransition(from, cell, Q);
            if (qValue >= maxQValue) {
                maxQValueCells.add(cell);
            }
        }

        return maxQValueCells;
    }

    /**
     * Given list of cells, return max Q value of cells in that list.
     * @param from
     * @param cells
     * @param Q
     * @return
     * @throws MazeException
     */
    private double getMaxQValue(int[] from, ArrayList<int[]> cells, double[][][] Q) throws MazeException {
        double max = Double.NEGATIVE_INFINITY;
        for (int[] cell:cells) {
            double qValue = getQValueForTransition(from, cell, Q);
            if (qValue > max) {
                max = qValue;
            }
        }
        return max;
    }

    private double getQValueForTransition(int[] from, int[] to, double[][][] Q) throws MazeException {
        Direction direction = getDirection(from, to);
        int directionValue = getDirectionValue(direction);
        double qValue = Q[from[0]][from[1]][directionValue];
        return qValue;
    }

    // Given list of cells, pick one at random from list
    private int[] pickCellAtRandom(ArrayList<int[]> cells) {
        int randomInt = 0 + (int)(Math.random()*(cells.size()));
        return cells.get(randomInt);
    }

    public int getDirectionValue(Direction direction) throws MazeException {
        switch(direction) {
            case UP:
                return 0;
            case RIGHT:
                return 1;
            case DOWN:
                return 2;
            case LEFT:
                return 3;
        }
        throw new MazeException("Direction could not be found.");
    }

    protected double maxQVal(int x, int y, double[][][] Q) throws MazeException {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            double val = Q[x][y][i];
            if (val > max){
                max = val;
            }
        }

        if (max > Double.NEGATIVE_INFINITY) {
            return max;
        } else {
            throw new MazeException("Max q value is negative infinity...");
        }
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

    protected int[] greedy(int[] cell, double[][] maze, double[][][] Q, double epsilon) throws MazeException {
        double randomNum = randomInt(100, new Random())/100.0;
        if(randomNum < epsilon) {
            ArrayList<int[]> validCells = getValidCells(cell[0],cell[1],maze);
            int randomNum2 = randomInt(validCells.size(),new Random());
            return validCells.get(randomNum2);
        } else {
            return maxQCell(cell,maze,Q);
        }
    }

    /**
     * Given multiple q matrices, choose one at random.
     * @param cell
     * @param maze
     * @param qMatrices
     * @param epsilon
     * @return
     * @throws MazeException
     */
    protected int[] greedyWithMultipleOptions(int[] cell, double[][] maze, ArrayList<double[][][]> qMatrices, double epsilon) throws MazeException {
        double randomNum = randomInt(100, new Random())/100.0;
        if(randomNum < epsilon) {
            ArrayList<int[]> validCells = getValidCells(cell[0], cell[1], maze);
            int randomNum2 = randomInt(validCells.size(), new Random());
            return validCells.get(randomNum2);
        } else {
            double[][][] Q = pickQMatrixAtRandom(qMatrices);
            return maxQCell(cell, maze, Q);
        }
    }

    private double[][][] pickQMatrixAtRandom(ArrayList<double[][][]> qMatrices) {
        int randomInt = 0 + (int)(Math.random()*(qMatrices.size()));
        return qMatrices.get(randomInt);
    }

    public double[][][] copyQ(double[][][] q) throws MazeException {
        int length = q[0].length;
        double [][][] qCopy = new double[length][length][4];

        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                for (int k = 0; k < 4; k++) {
                    qCopy[i][j][k] = q[i][j][k];
                }
            }
        }

        if (!qEqual(qCopy, q)) {
            throw new MazeException("Q matrix was not copied correctly");
        }

        return qCopy;
    }

    public boolean qEqual(double[][][] q1, double[][][] q2) {
        int len = q1[0].length;
        for(int i = 0; i < len; i++) {
            for(int j = 0; j < len; j++) {
                for(int k = 0; k < 4; k++) {
                    if (q1[i][j][k] != q2[i][j][k]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
