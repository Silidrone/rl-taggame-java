package chapter2.taggame;


import chapter2.MovementPanel;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import org.json.JSONArray;
import org.json.JSONObject;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

public class TagArena extends MovementPanel {

    private static final double SCOREBOARD_WIDTH = 100;
    TagGameState state;

    List<TagPlayer> players;

    TagPlayer tagPlayer;


    long tagChangeTime;
    private long TagWarmUpTime = 3000;

    public TagArena() {
        super();
        this.players = new ArrayList<>();
    }

    public List<TagPlayer> getPlayers() {
        return players;
    }

    public void addPlayer(TagPlayer player) {
        players.add(player);
        add(player);
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
        super.init(gameContainer, stateBasedGame);

        state = TagGameState.WarmUp;
        int tag = RandomUtils.randomInt(players.size());

        setTag(players.get(tag), players.get(tag));
    }

    @Override
    public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) throws SlickException {
        super.render(gameContainer, stateBasedGame, graphics);
        renderScores(gameContainer, stateBasedGame, graphics);
    }

    private void renderScores(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) {
        double x = width - FRAME_WIDTH - SCOREBOARD_WIDTH;
        double y = FRAME_WIDTH;

        for (TagPlayer player : players) {
            graphics.setColor(player.getColor());
            graphics.drawString(player.name + " : " + player.score(), (float) x, (float) y);
            y += 20;
        }
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int time) throws SlickException {
        super.update(gameContainer, stateBasedGame, time);

        if (state == TagGameState.WarmUp) {
            long now = System.currentTimeMillis();
            if (now - tagChangeTime > TagWarmUpTime) {
                players.forEach(TagPlayer::warmupFinished);
                state = TagGameState.InPlay;
            }
        } else {
            checkTagging();
        }
    }

    private void checkTagging() {
        for (TagPlayer player : players) {
            if (player == tagPlayer)
                continue;

            if (tagPlayer.isTagging(player)) {
                setTag(tagPlayer, player);
                break;
            }
        }
    }

    private void setTag(TagPlayer oldTag, TagPlayer newTag) {
        tagPlayer = newTag;
        state = TagGameState.WarmUp;
        tagChangeTime = System.currentTimeMillis();
        players.forEach((p) -> p.tagChanged(oldTag, tagPlayer));
    }

    private Point2D toPoint(Vector2D v) {
        return new Point2D(v.x(), v.y());
    }

    private int[] toIntArray(Point2D point) {
        return new int[]{
                (int) point.x(),
                (int) point.y()
        };
    }

    public String getGameStateAsString(TagPlayer me) {
        JSONObject gameState = new JSONObject();

        gameState.put("myVelocity", toIntArray(toPoint(me.getVelocity())));
        gameState.put("taggedVelocity", toIntArray(toPoint(tagPlayer.getVelocity())));

        Point2D myPosition = me.getStaticInfo().getPos();
        Point2D taggedPosition = tagPlayer.getStaticInfo().getPos();

        int[] relativePosition = me.isTagged() ?
                new int[]{0, 0} :
                toIntArray(new Point2D(
                        taggedPosition.getX() - myPosition.getX(),
                        taggedPosition.getY() - myPosition.getY()
                ));

        gameState.put("relativePosition", relativePosition);

        return gameState.toString();
    }

}
