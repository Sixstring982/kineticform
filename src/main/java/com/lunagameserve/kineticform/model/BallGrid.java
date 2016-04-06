package com.lunagameserve.kineticform.model;

import com.lunagameserve.kineticform.twitter.TwitterCommand;
import com.lunagameserve.kineticform.twitter.TwitterCommandQueue;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by sixstring982 on 4/2/16.
 */
public class BallGrid {
    private static final float MAX_HEIGHT = 5.0f;
    private static final Vector3f FRAME_COLOR = new Vector3f(0.5f, 0.5f, 0.5f);
    private static final Vector3f BALL_COLOR = new Vector3f(0.7f, 0.3f, 0.05f);

    private final int width;
    private final int height;

    private float[][] heights;

    private BallGridState state = BallGridState.IDLE;
    private BallGridMoveArray moveArray;
    private float moveTick = 0.0f;


    public BallGrid(int width, int height) {
        this.width = width;
        this.height = height;

        this.heights = new float[width][height];
    }

    public void gl(float x, float y, float z, float dx, float dy, float dz) {
        for (int xx = 0; xx < width; xx++) {
            for (int zz = 0; zz < height; zz++) {
                /* Draw frame */
                glColor3f(FRAME_COLOR.x, FRAME_COLOR.y, FRAME_COLOR.z);
                glBegin(GL_LINES); {
                    glVertex3f(x + xx * dx, y, z + zz * dz);
                    glVertex3f(x + (xx + 1) * dx, y, z + zz * dz);

                    glVertex3f(x + (xx + 1) * dx, y, z + zz * dz);
                    glVertex3f(x + (xx + 1) * dx, y, z + (zz + 1) * dz);

                    glVertex3f(x + (xx + 1) * dx, y, z + (zz + 1) * dz);
                    glVertex3f(x + xx * dx, y, z + (zz + 1) * dz);

                    glVertex3f(x + xx * dx, y, z + (zz + 1) * dz);
                    glVertex3f(x + xx * dx, y, z + zz * dz);

                } glEnd();

                float ballHeight = heights[xx][zz];
                if (ballHeight % (MAX_HEIGHT * 2) < MAX_HEIGHT) {
                    ballHeight = ballHeight % MAX_HEIGHT;
                } else {
                    ballHeight = MAX_HEIGHT - (ballHeight % MAX_HEIGHT);
                }
                glColor3f(BALL_COLOR.x, BALL_COLOR.y, BALL_COLOR.z);
                glBegin(GL_POINTS); {
                    glVertex3f(x + xx * dx + (dx / 2), y - ballHeight * dy, z +  zz * dz +  (dz / 2));
                } glEnd();
            }
        }
    }

    public void update(TwitterCommandQueue queue, float delta) {
        switch (state) {
            case IDLE:
                if (queue.isCommandAvailable()) {
                    TwitterCommand command = queue.nextCommand();
                    moveArray = new BallGridMoveArray(width, height, command.getPredicate());
                    state = BallGridState.PROCESSING_MOVE;
                }
                break;
            case PROCESSING_CHECK:
                moveArray.decrement();
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (moveArray.get(x, y) > 0) {
                            state = BallGridState.PROCESSING_MOVE;
                            return;
                        }
                    }
                }
                state = BallGridState.IDLE;
                break;
            case PROCESSING_MOVE:
                moveTick += delta;
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        if (moveArray.get(x, y) > 0) {
                            heights[x][y] += delta;
                        }
                    }
                }
                if (moveTick > 1) {
                    moveTick = 0;
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            if (moveArray.get(x, y) > 0) {
                                heights[x][y] = Math.round(heights[x][y]);
                            }
                        }
                    }
                    state = BallGridState.PROCESSING_CHECK;
                }
                break;
        }
    }
}
