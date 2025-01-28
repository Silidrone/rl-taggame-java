package chapter2.taggame;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Bootstrap;

public class TagGame extends StateBasedGame {
    public static final int DemoWidth = 1600;
    public static final int DemoHeight = 1200;
    public static final int MAX_VELOCITY = 3;
    public static final int VELOCITY_SCALING = 10;
    public final static float TIME_COEFFICIENT = 0.04f;
    public final static int FRAME_RATE = 60;
    public static final int PLAYER_COUNT = 5;
    public static final double TAGGER_SLEEP_TIME_MS = 800;
    public static final String TAGGAME = "Tag Game";
    public static final Color PanelColor = Color.decode("#222222");
    public static final String RL_PLAYER_NAME = "Sili";
    public static final Color RL_PLAYER_COLOR = Color.blue;

    private final int tagArenaIndex = 0;

    public TagGame() {
        super(TAGGAME);
        addState(new TagArena());
    }

    @Override
    public void initStatesList(GameContainer gameContainer) {
        enterState(tagArenaIndex);
    }

    public static void main(String[] args) {
        TagGame game = new TagGame();
        Bootstrap.runAsApplication(game, DemoWidth, DemoHeight, false);
    }
}

