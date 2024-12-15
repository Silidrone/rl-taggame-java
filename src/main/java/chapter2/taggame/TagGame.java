package chapter2.taggame;

import chapter2.MovingEntity;
import chapter2.StaticInfo;
import math.geom2d.Point2D;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Bootstrap;
import org.newdawn.slick.Color;

import java.util.Random;

public class TagGame extends StateBasedGame {
    public static final int DemoWidth = 1500;
    public static final int DemoHeight = 1000;
    private static final String TAGGAME = "Tag Game";
    private final int tagArenaIndex = 0;

    public TagGame() {
        super(TAGGAME);
        addState(new TagArena());
    }

    @Override
    public void initStatesList(GameContainer gameContainer) throws SlickException {
        enterState(tagArenaIndex);
    }

    public TagArena getArena() {
        return (TagArena) getState(tagArenaIndex);
    }

    public void addPlayer(TagPlayer player) {
        TagArena arena = (TagArena) getState(tagArenaIndex);

        arena.addPlayer(player);
    }

    public void generateRandomPlayers(int n) {
        Random rand = new Random();
        for (int i = 0; i < n; i++) {
            String playerName = "P" + (i + 1);
            double x = rand.nextDouble() * (DemoWidth - 200);
            double y = rand.nextDouble() * (DemoHeight - 200);
            Point2D position = new Point2D(x, y);
            Color[] colors = {Color.blue, Color.green, Color.yellow, Color.magenta, Color.cyan, Color.orange, Color.pink};
            Color playerColor = colors[rand.nextInt(colors.length)];
            TagPlayer player = new TagPlayer(playerName, new StaticInfo(position), playerColor);
            player.setTagSteeringEngine(new DumbTagSteeringEngine(DemoWidth, DemoHeight, MovingEntity.MAX_VELOCITY));
            addPlayer(player);
        }
    }

    public static void main(String[] args) {
        TagGame game = new TagGame();

        game.generateRandomPlayers(5);

        Bootstrap.runAsApplication(game, DemoWidth, DemoHeight, false);
    }
}
