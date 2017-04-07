import java.util.ArrayList;
import java.util.HashSet;

public class GridWorld {
    private int[][] gridWorld;
    private HashSet<ArrayList<int[]>> allPaths = new HashSet<>();
    private ArrayList<int[]> optimalPath;
    private int maxReward = -100000;
    private int printCount = 0;

    public GridWorld(int[][] gridWorld) {
        this.gridWorld = gridWorld;
    }

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

    private int getReward(ArrayList<int[]> path) {
        int reward = 0;
        for (int[] coordinate : path) {
            reward += gridWorld[coordinate[0]][coordinate[1]];
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
        printCount++;
        System.out.println("Reward: " + reward + " (steps: " + path.size() + ")");
    }

    // returns true if coordinate in bounds of gridWorld
    // assumes gridWorld is square
    private boolean inBounds(int[] coordinate) {
        int x = coordinate[0];
        int y = coordinate[1];
        boolean xInBounds = (x < gridWorld.length && x >= 0);
        boolean yInBounds = (y < gridWorld.length && y >= 0);
        return xInBounds && yInBounds;
    }

    private void copyAndVisit(int[] target, ArrayList<int[]> pathToCurrent) {
        if (inBounds(target)) {
            if (!deepContains(target, pathToCurrent)) {
                ArrayList<int[]> pathToCurrentDeepCopy = deepCopy(pathToCurrent);
                pathToCurrentDeepCopy.add(target);
                enumeratePolicies(target, pathToCurrentDeepCopy);
                printPath(pathToCurrentDeepCopy, 100);
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
        if (gridWorld[x][y] == 100) {
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

    public int getPrintCount() {
        return printCount;
    }

    public static void main(String[] args) {
        int[][] gridWorldTest = new int[][]{{-1,-1,-1,-1},{-1,-1,-1,-1},{-1,-1,-1,-1},{-1,-1,-1,-1}};
        GridWorld gridWorld = new GridWorld(gridWorldTest);
        ArrayList<int[]> pathToCurrent = new ArrayList<>();
        pathToCurrent.add(new int[] {2,3});
        gridWorld.enumeratePolicies(new int[]{2, 3}, pathToCurrent);
        System.out.println(gridWorld.getPrintCount());
    }
}
