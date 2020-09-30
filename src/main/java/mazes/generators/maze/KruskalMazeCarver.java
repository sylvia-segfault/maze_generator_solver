package mazes.generators.maze;

import datastructures.concrete.Graph;
import datastructures.interfaces.ISet;
import mazes.entities.Maze;
import mazes.entities.Room;
import mazes.entities.Wall;

import java.util.Random;


/**
 * Carves out a maze based on Kruskal's algorithm.
 *
 * See the spec for more details.
 */
public class KruskalMazeCarver implements MazeCarver {
    @Override
    public ISet<Wall> returnWallsToRemove(Maze maze) {
        // Note: make sure that the input maze remains unmodified after this method is over.
        //
        // In particular, if you call 'wall.setDistance()' at any point, make sure to
        // call 'wall.resetDistanceToOriginal()' on the same wall before returning.

        // we take the maze, treat each room as a vertex and each wall as an edge,
        // assign each wall a random weight, and run any MST-finding algorithm.
        // We then remove any wall that was a part of that MST.
        ISet<Wall> walls = maze.getWalls(); // reference, if I change something, will be updated?
        Random rand = new Random();
        for (Wall wall: walls) {
            wall.setDistance(rand.nextDouble());
        }
        Graph<Room, Wall> graph = new Graph<>(maze.getRooms(), walls);
        ISet<Wall> result = graph.findMinimumSpanningTree();
        for (Wall wall: walls) {
            wall.resetDistanceToOriginal();
        }
        return result;
    }
}
