package taggame;

import math.geom2d.Point2D;


public class InMemoryRunner {
    protected static final String RL_PLAYER_NAME = "Sili";
    protected static final int PLAYER_COUNT = 2;
    protected static final float PLAYER_RADIUS = 20;
    protected static final int WIDTH = 1000;
    protected static final int HEIGHT = 1000;
    protected final static float TIME_COEFFICIENT = 1f;
    protected static final int MAX_VELOCITY = 5;
    protected static final int TAGGER_SLEEP_TIME_MS = 50;
    protected static final int DISTANCE_LEVEL_COUNT = 6;

    public static void main(String[] args) {
        TagGame arena = new TagGame(RL_PLAYER_NAME, PLAYER_COUNT, PLAYER_RADIUS, WIDTH, HEIGHT, TIME_COEFFICIENT, MAX_VELOCITY, TAGGER_SLEEP_TIME_MS, DISTANCE_LEVEL_COUNT);
        arena.initGame();

        long lastTime = System.nanoTime();

        while (true) {
            Point2D pos = arena.getRLPlayer().getStaticInfo().getPos();
            System.out.println("(" + Math.round(pos.x()) + ", " + Math.round(pos.y()) + ")");

            long currentTime = System.nanoTime();
            long deltaTime = currentTime - lastTime;
            System.out.println("t: " + deltaTime + " ns");
            lastTime = currentTime;

            arena.updateGame((int) Math.max(deltaTime / 1_000_000L, 1));
        }
    }
}
