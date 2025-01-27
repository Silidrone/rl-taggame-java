package chapter2.taggame;


import chapter2.*;
import math.geom2d.Point2D;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.state.StateBasedGame;

public class TagPlayer extends MovingEntity {
    private static final float PLAYER_RADIUS = 20;
    private static final int TAGRADIUS = 23;
    private static final Color TAGCOLOR = Color.red;

    protected String name;
    protected TagArena arena;
    protected Color color;
    protected boolean isTagged;

    public TagPlayer(String name, StaticInfo staticInfo, Color color, TagArena _arena) {
        super(staticInfo, new Ball(color, PLAYER_RADIUS));
        this.name = name;
        this.color = color;
        arena = _arena;
    }

    @Override
    public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) {
        super.render(gameContainer, stateBasedGame, graphics);

        if (isTagged) {
            Point2D center = staticInfo.getPos();
            Color c = graphics.getColor();
            graphics.setColor(TAGCOLOR);
            graphics.drawOval((float) center.x() - TAGRADIUS, (float) center.y() - TAGRADIUS, 2 * TAGRADIUS, 2 * TAGRADIUS);

            graphics.setColor(c);
        }
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, float time) {
        super.update(gameContainer, stateBasedGame, time);
    }

    public boolean isTagging(TagPlayer player) {
        return (body.getRadius() + player.body.getRadius() >= staticInfo.getPos().distance(player.staticInfo.getPos()));
    }

    TagArena getArena() {
        return arena;
    }

    public void setIsTagged(boolean v) {
        this.isTagged = v;
    }

    boolean isTagged() {
        return isTagged;
    }
}
