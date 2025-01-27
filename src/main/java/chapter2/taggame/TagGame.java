package chapter2.taggame;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Bootstrap;

public class TagGame extends StateBasedGame {
    public static final int DemoWidth = 1000;
    public static final int DemoHeight = 800;
    private static final String TAGGAME = "Tag Game";
    private final int tagArenaIndex = 0;

    public TagGame() {
        super(TAGGAME);
        addState(new TagArena());
    }

    @Override
    public void initStatesList(GameContainer gameContainer) {
        enterState(tagArenaIndex);
    }

    public TagArena getArena() {
        return (TagArena) getState(tagArenaIndex);
    }

    public static void main(String[] args) {
        TagGame game = new TagGame();
        Bootstrap.runAsApplication(game, DemoWidth, DemoHeight, false);
    }
}

