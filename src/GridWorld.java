import java.util.ArrayList;
import java.util.HashSet;

public class GridWorld {
    private int[][] board;
    private HashSet<ArrayList<int[]>> allPaths = new HashSet<>();
    private ArrayList<int[]> optimalPath;
    private int maxReward = -100000;

    private ArrayList<int[]> deepCopy(ArrayList<int[]> oldList) {
        ArrayList<int[]> newList = new ArrayList<>();
        for (int[] coordinate : oldList) {
            newList.add(coordinate);
        }
        return newList;
    }

    private boolean deepContains(int[] coordinate, ArrayList<int[]> visited) {
        for (int[] loc : visited) {
            if (coordinate[0] == loc[0] && coordinate[1] == loc[1]) {
                return true;
            }
        }
        return false;
    }

    public GridWorld() {
        int[] startRow = {0,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int[] emptyRow = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
        int[] goalRow1 = {-1,-1,-1,-1,-1,-1,-1,-1,100,-1};
        int[] goalRow2 = {-1,-1,-1,-1,-1,100,-1,-1,-1,-1};
        int[] goalRow3 = {-1,-1,-1,-1,-1,-1,-1,-1,-1,100};
        int[] goalRow4 = {-1,-1,100,-1,-1,-1,-1,-1,-1,-1};

        this.board = new int[][]{startRow, goalRow1, emptyRow, emptyRow, goalRow2, emptyRow, goalRow3, emptyRow, emptyRow, goalRow4};
    }

    private int getReward(ArrayList<int[]> path) {
        int reward = 0;
        for (int[] coordinate : path) {
            reward += board[coordinate[0]][coordinate[1]];
        }
        return reward;
    }

    public void printPath(ArrayList<int[]> path, int reward) {
        String pathString = "";
        for (int[] coordinate : path) {
            String coordinateString = "(" + coordinate[0] + ", " + coordinate[1] + ")";
            if (pathString.length() > 0) {
                pathString += ", ";
            }
            pathString += coordinateString;
        }
        System.out.println(pathString);
        System.out.println("Reward: " + reward + " (steps: " + path.size() + ")");
    }

    // returns true if coordinate in bounds of board
    // assumes board is square
    private boolean inBounds(int[] coordinate) {
        int x = coordinate[0];
        int y = coordinate[1];
        boolean xInBounds = (x < board.length && x >= 0);
        boolean yInBounds = (y < board.length && y >= 0);
        return xInBounds && yInBounds;
    }

    private void copyAndVisit(int[] target, ArrayList<int[]> pathToCurrent) {
        if (inBounds(target)) {
            if (!deepContains(target, pathToCurrent)) {
                ArrayList<int[]> pathToCurrentDeepCopy = deepCopy(pathToCurrent);
                pathToCurrentDeepCopy.add(target);
                enumeratePolicies(target, pathToCurrentDeepCopy);
            }
        }
    }

    /**
     * Will do a breadth first search on the grid, add all paths to goal states to allPaths.
     */
    public void enumeratePolicies(int[] coordinate, ArrayList<int[]> pathToCurrent) {
        int currentReward = getReward(pathToCurrent);
        ArrayList<int[]> pathToCurrentDeepCopy = deepCopy(pathToCurrent);

        // Don't continue if a more optimal path has already been found.
        if (currentReward < maxReward) {
            return;
        }

        int x = coordinate[0];
        int y = coordinate[1];

        // If goal is reached, record path and update maxReward.
        if (board[x][y] == 100) {
            System.out.println("Goal reached.");
            allPaths.add(pathToCurrentDeepCopy);
            printPath(pathToCurrentDeepCopy, currentReward);
            if (currentReward > maxReward) {
                maxReward = currentReward;
                optimalPath = pathToCurrentDeepCopy;
            }
            return;
        }

        int[] right = {x+1, y};
        int[] left = {x-1, y};
        int[] up = {x, y-1};
        int[] down = {x, y+1};

        copyAndVisit(left, pathToCurrent);
        copyAndVisit(right, pathToCurrent);
        copyAndVisit(up, pathToCurrent);
        copyAndVisit(down, pathToCurrent);
    }


    public static void main(String[] args) {
        GridWorld gridWorld = new GridWorld();
        ArrayList<int[]> pathToCurrent = new ArrayList<>();
        int[] start = {0,0};
        pathToCurrent.add(start);
        gridWorld.enumeratePolicies(start, pathToCurrent);
    }

}
