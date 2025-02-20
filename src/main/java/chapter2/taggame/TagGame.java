package chapter2.taggame;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Bootstrap;
import math.geom2d.Point2D;

public class TagGame extends StateBasedGame {
    public static final int DemoWidth = 500;
    public static final int DemoHeight = 500;
    public static final float PLAYER_RADIUS = 20;
    public static final int TAG_PLAYER_RADIUS = 22;
    public static final Color TAGCOLOR = Color.red;
    public static final Color RL_PLAYER_COLOR = Color.blue;
    public static final int MAX_VELOCITY = 2;
    public final static int FRAME_RATE = 1000;
    public static final int PLAYER_COUNT = 2;

    public final static float TIME_COEFFICIENT = 0.5f;
    public static final int DISTANCE_LEVEL_COUNT = 10;
    public static final double TAGGER_SLEEP_TIME_MS = 5;
    public static final String TAGGAME = "Tag Game";
    public static final Color PanelColor = Color.decode("#222222");
    public static final String RL_PLAYER_NAME = "Sili";
    public static final Point2D center = new Point2D((double) DemoWidth / 2, (double) DemoHeight / 2);

    public static double getMaxDistance() {
        return new Point2D(0, 0).distance(new Point2D(DemoWidth, DemoHeight));
    }

    public TagGame() {
        super(TAGGAME);
        addState(new TagArena());
    }

    @Override
    public void initStatesList(GameContainer gameContainer) {
        enterState(0);
    }

    public static void main(String[] args) {
        TagGame game = new TagGame();
        Bootstrap.runAsApplication(game, DemoWidth, DemoHeight, false);
    }
}

