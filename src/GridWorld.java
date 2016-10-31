import java.util.ArrayList;
import java.util.HashSet;

public class GridWorld {
    private int[][] board;
    private HashSet<ArrayList<int[]>> allPaths = new HashSet<ArrayList<int[]>>();

    private ArrayList<int[]> deepCopy(ArrayList<int[]> oldList) {
        ArrayList<int[]> newList = new ArrayList<>();
        for (int[] coordinate : oldList) {
            newList.add(coordinate);
        }
        return newList;
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

    public void printPath(ArrayList<int[]> path) {
        String pathString = "";
        int cost = 0;
        for (int[] coordinate : path) {
            String coordinateString = "(" + coordinate[0] + ", " + coordinate[1] + ")";
            if (pathString.length() > 0) {
                pathString += ", ";
            }
            pathString += coordinateString;
            cost += board[coordinate[0]][coordinate[1]];
        }
        System.out.println(pathString);
        System.out.println("Cost: " + cost);
    }

    /**
     * Will do a breadth first search on the grid, add all paths to goal states to allPaths.
     */
    public void enumeratePolicies(int x, int y, ArrayList<int[]> pathToCurrent, ArrayList<int[]> visited) {
        int[] current = {x,y};
        pathToCurrent.add(current);

        if (board[x][y] == 100) {
            allPaths.add(pathToCurrent);
            printPath(pathToCurrent);
        }

        if (x < board.length -1) {
            ArrayList<int[]> newPath = deepCopy(pathToCurrent);
            enumeratePolicies(x + 1, y, newPath, visited);
        }

        if (y < board.length - 1) {
            ArrayList<int[]> newPath2 = deepCopy(pathToCurrent);
            enumeratePolicies(x, y + 1, newPath2, visited);
        }

    }

    public static void main(String[] args) {
        GridWorld gridWorld = new GridWorld();
        ArrayList<int[]> pathToCurrent = new ArrayList<>();
        ArrayList<int[]> visited = new ArrayList<>();
        gridWorld.enumeratePolicies(0, 0, pathToCurrent, visited);
    }

}
