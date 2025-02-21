package chapter2.taggame;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Bootstrap;
import math.geom2d.Point2D;

public class TagGame extends StateBasedGame {
    public static final int DemoWidth = 1000;
    public static final int DemoHeight = 1000;
    public static final float PLAYER_RADIUS = 20;
    public static final int TAG_PLAYER_RADIUS = 22;
    public static final Color TAGCOLOR = Color.red;
    public static final Color RL_PLAYER_COLOR = Color.blue;
    public static final int MAX_VELOCITY = 5;
    public final static int FRAME_RATE = 3000;
    public static final int PLAYER_COUNT = 2;

    public final static float TIME_COEFFICIENT = 1.0f;
    public static final int DISTANCE_LEVEL_COUNT = 6;
    public static final double TAGGER_SLEEP_TIME_MS = 5;
    public static final double TOUCHING_CORNER_DISTANCE_THRESHOLD = TagGame.PLAYER_RADIUS + 130;
    public static final String TAGGAME = "Tag Game";
    public static final Color PanelColor = Color.decode("#222222");
    public static final String RL_PLAYER_NAME = "Sili";

    public static boolean isTouchingWall(TagPlayer player) {
        Point2D position = player.getStaticInfo().getPos();

        return position.x() <= TOUCHING_CORNER_DISTANCE_THRESHOLD ||
                position.x() >= DemoWidth - TOUCHING_CORNER_DISTANCE_THRESHOLD ||
                position.y() <= TOUCHING_CORNER_DISTANCE_THRESHOLD ||
                position.y() >= DemoHeight - TOUCHING_CORNER_DISTANCE_THRESHOLD;
    }

    public static double getMaxDistance() {
        return new Point2D(0, 0).distance(new Point2D(DemoWidth, DemoHeight)) - TagGame.PLAYER_RADIUS - 250;
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

