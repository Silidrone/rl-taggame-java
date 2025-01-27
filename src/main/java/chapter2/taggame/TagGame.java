package chapter2.taggame;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Bootstrap;

public class TagGame extends StateBasedGame {
    public static final int DemoWidth = 1600;
    public static final int DemoHeight = 1200;
    public static final int MAX_VELOCITY = 20;
    public final static float TIME_COEFFICIENT = 0.03f;
    public final static int FRAME_RATE = 60;
    private static final String TAGGAME = "Tag Game";
    private final int tagArenaIndex = 0;

    public TagGame() {
        super(TAGGAME);
        addState(new TagArena(DemoWidth, DemoHeight));
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

