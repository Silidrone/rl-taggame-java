package chapter2.taggame;


import chapter2.*;
import math.geom2d.Point2D;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.geom.Rectangle2D;

public class TagPlayer extends MovingEntity implements TagGameListener {
    private static final float PLAYER_RADIUS = 20;
    private static final int TAGRADIUS = 23;
    private static final Color TAGCOLOR = Color.red;
    private static final int TAG_PENALTY = 4;
    private static final int TAG_REWARD = 3;

    protected String name;
    protected TagArena arena;
    TagSteeringEngine tagSteeringEngine = player -> idleSteering;

    protected int negativeTagCount; // The number of times the player has been tagged by others.
    protected int positiveTagCount; // The number of times the player has tagged others.

    boolean isTagged;

    public TagPlayer(String name, StaticInfo staticInfo, Color color) {
        super(staticInfo, new Ball(color, PLAYER_RADIUS));
        this.name = name;
    }

    public void setTagSteeringEngine(TagSteeringEngine tagSteeringEngine) {
        this.tagSteeringEngine = tagSteeringEngine;
    }

    @Override
    public void tagChanged(TagPlayer oldTag, TagPlayer newTag) {
        isTagged = newTag == this;

        if (oldTag == this) {
            positiveTagCount++;
        }

        if (isTagged) {
            negativeTagCount++;
            setVelocity(noVelocity);
        } else {
            setSteeringBehavior(tagSteeringEngine.getSteeringBehavior(this));
        }
    }

    public boolean isTagging(TagPlayer player) {
        return (body.getRadius() + player.body.getRadius() >= staticInfo.getPos().distance(player.staticInfo.getPos()));
    }

    public int score() {
        return -TAG_PENALTY * negativeTagCount + TAG_REWARD * positiveTagCount;
    }

    @Override
    public void warmupFinished() {
        setSteeringBehavior(tagSteeringEngine.getSteeringBehavior(this));
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame, Rectangle2D boundary) {
        super.init(gameContainer, stateBasedGame, boundary);
        arena = ((TagGame) stateBasedGame).getArena();
        positiveTagCount = negativeTagCount = 0;
    }

    @Override
    public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) {
        super.render(gameContainer, stateBasedGame, graphics);

        if (isTagged) {
            renderTag(graphics);
        }
    }

    private void renderTag(Graphics graphics) {
        Point2D center = staticInfo.getPos();
        Color c = graphics.getColor();
        graphics.setColor(TAGCOLOR);
        graphics.drawOval((float) center.x() - TAGRADIUS, (float) center.y() - TAGRADIUS, 2 * TAGRADIUS, 2 * TAGRADIUS);

        graphics.setColor(c);
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, float time) {
        super.update(gameContainer, stateBasedGame, time);
    }

    TagArena getArena() {
        return arena;
    }

    boolean isTagged() {
        return isTagged;
    }

    public Color getColor() {
        Ball ball = (Ball) getBody();
        return ball.getColor();
    }
}
