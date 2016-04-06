package com.lunagameserve.kineticform.twitter;

import com.lunagameserve.kineticform.model.BallPredicate;

/**
 * Created by sixstring982 on 4/2/16.
 */
public enum TwitterCommand {
    CENTER(new BallPredicate() {
        public int delta(int x, int y, int w, int h) {
            if ((x == w / 2  || x == w / 2 + 1) &&
                (y == h / 2  || y == h / 2 + 1)) {
                return 1;
            } else {
                return 0;
            }
        }
    }),
    EDGE(new BallPredicate() {
        public int delta(int x, int y, int w, int h) {
            if (x == 0 || y == 0 ||
                x == w - 1 || y == h - 1) {
                return 5;
            } else {
                return 0;
            }
        }
    }),
    SUBTRACT(new BallPredicate() {
        public int delta(int x, int y, int w, int h) {
            if (x > w / 2 && y > w / 2) {
                return 1;
            } else {
                return 0;
            }
        }
    }),
    WEDGE(new BallPredicate() {
        public int delta(int x, int y, int w, int h) {
            return x + 1;
        }
    }),
    CORNER(new BallPredicate() {
        public int delta(int x, int y, int w, int h) {
            return x + y + 1;
        }
    });

    BallPredicate predicate;

    TwitterCommand(BallPredicate predicate) {
        this.predicate = predicate;
    }

    public BallPredicate getPredicate() {
        return predicate;
    }
}
