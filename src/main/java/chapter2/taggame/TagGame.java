package chapter2.taggame;

import chapter2.GameStateSender;
import chapter2.MovingEntity;
import chapter2.StaticInfo;
import math.geom2d.Point2D;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.util.Bootstrap;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

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
    public void initStatesList(GameContainer gameContainer) {
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
        Color[] colors = {Color.blue, Color.green, Color.yellow, Color.magenta, Color.cyan, Color.orange, Color.pink};
        for (int i = 0; i < n; i++) {
            TagPlayer player = new TagPlayer( "P" + (i + 1), new StaticInfo(new Point2D(rand.nextDouble() * (DemoWidth - 200), rand.nextDouble() * (DemoHeight - 200))), colors[i]);
            player.setTagSteeringEngine(new DumbTagSteeringEngine(DemoWidth, DemoHeight, MovingEntity.MAX_VELOCITY));
            addPlayer(player);
        }
    }

    public static void main(String[] args) {
        TagGame game = new TagGame();

        game.generateRandomPlayers(5);
        TagPlayer me = game.getArena().getPlayers().get(0);
        me.setName("Sili");

        CountDownLatch latch = new CountDownLatch(1);

        GameStateSender sender = new GameStateSender(game, latch);
        new Thread(sender).start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Bootstrap.runAsApplication(game, DemoWidth, DemoHeight, false);
    }
}
