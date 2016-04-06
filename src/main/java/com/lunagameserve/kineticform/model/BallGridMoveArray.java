package com.lunagameserve.kineticform.model;

/**
 * Created by sixstring982 on 4/2/16.
 */
public class BallGridMoveArray {
    private final int width;
    private final int height;

    private int[][] moves;

    public BallGridMoveArray(int width, int height, BallPredicate pred) {
        this.width = width;
        this.height = height;

        this.moves = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                this.moves[x][y] = pred.delta(x, y, width, height);
            }
        }
    }

    public int get(int x, int y) {
        return this.moves[x][y];
    }

    public void decrement() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (this.moves[x][y] > 0) {
                    this.moves[x][y]--;
                }
            }
        }
    }
}
