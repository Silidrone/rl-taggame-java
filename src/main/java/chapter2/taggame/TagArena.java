package chapter2.taggame;

import chapter2.*;
import math.geom2d.Point2D;
import math.geom2d.Vector2D;
import org.json.JSONObject;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class TagArena extends BasicGameState {
    private static final Color PanelColor = Color.decode("#222222");
    private static final int PLAYER_COUNT = 5;
    private static final double TAGGER_SLEEP_TIME_MS = 3000;
    private final String RL_PLAYER_NAME = "Sili";
    private final Color RL_PLAYER_COLOR = Color.blue;

    private List<TagPlayer> players;
    private TagPlayer tagPlayer;
    private TagPlayer oldTagPlayer;
    private Communicator communicator;
    private final int RL_PLAYER_INDEX = 0;
    private double tagChangedTime;
    private int width;
    private int height;

    public TagArena(int width, int height) {
        super();
        this.players = new ArrayList<>();
        this.oldTagPlayer = null;
        this.tagChangedTime = 0;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getID() {
        return 0;
    }

    private void initGame() {
        players.clear();
        Random rand = new Random();
        ArrayList<Color> colors = new ArrayList<>(Arrays.asList(Color.blue, Color.green, Color.yellow, Color.magenta, Color.cyan, Color.orange, Color.pink));
        colors = colors.stream().filter(color -> color != RL_PLAYER_COLOR).collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < PLAYER_COUNT; i++) {
            TagPlayer player = new TagPlayer(
                    i == RL_PLAYER_INDEX ? RL_PLAYER_NAME : ("P" + (i + 1)),
                    new StaticInfo(new Point2D(rand.nextDouble() * (TagGame.DemoWidth - 200), rand.nextDouble() * (TagGame.DemoHeight - 200))),
                    i == RL_PLAYER_INDEX ? RL_PLAYER_COLOR : colors.get(i % colors.size())
            );
            if (i != RL_PLAYER_INDEX) {
                player.setSteeringBehavior(new DumbTagSteering(TagGame.DemoWidth, TagGame.DemoHeight, TagGame.MAX_VELOCITY, player, this));
            }
            players.add(player);
        }
        TagPlayer taggedPlayer = players.get(Utils.randomInt(players.size()));
        setTag(taggedPlayer, taggedPlayer);
    }

    @Override
    public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException {
        initGame();
        try {
            communicator = new Communicator();
        } catch (IOException e) {
            throw new SlickException("Failed to initialize communicator.", e);
        }
        gameContainer.setTargetFrameRate(TagGame.FRAME_RATE);
    }

    @Override
    public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics graphics) throws SlickException {
        graphics.clear();
        graphics.setColor(PanelColor);
        graphics.fillRect(0, 0, (float)width, (float)height);

        graphics.translate(0, (float) height);
        graphics.scale(1, -1);

        players.forEach(e -> e.render(graphics));

        graphics.scale(1, -1);
        graphics.translate(0, (float) -height);
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int time) throws SlickException {
        try {
            boolean taggerSleeping = System.currentTimeMillis() - tagChangedTime < TAGGER_SLEEP_TIME_MS;
            TagPlayer RL_player = players.get(RL_PLAYER_INDEX);

            if(tagPlayer != RL_player || !taggerSleeping) {
                String action = communicator.receiveAction();
                if (action == null || action.equalsIgnoreCase(Communicator.EXIT)) return;

                if (action.equals(Communicator.RESET)) {
                    initGame();
                } else {
                    RL_player.setSteeringBehavior((StaticInfo staticInfo, Vector2D currentVelocity) -> deserializeAction(action));
                }
            }

            if(!taggerSleeping) {
                tagPlayer.setSteeringBehavior(new DumbTagSteering(TagGame.DemoWidth, TagGame.DemoHeight, TagGame.MAX_VELOCITY, tagPlayer, this));
                handleTaggingLogic();
            }

            for(TagPlayer player : players) {
                player.update(gameContainer, stateBasedGame, time);
            }

            sendSerializedGameState(players.get(RL_PLAYER_INDEX));
        } catch (IOException e) {
            try {
                communicator.close();
                gameContainer.exit();
            } catch (IOException ex) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<TagPlayer> getPlayers() {
        return players;
    }

    private Vector2D deserializeAction(String action) {
        JSONObject json = new JSONObject(action);
        double x = json.getInt("x");
        double y = json.getInt("y");
        return new Vector2D(x, y);
    }

    private void sendSerializedGameState(TagPlayer me) {
        JSONObject gameState = new JSONObject();

        gameState.put("mv", Utils.toIntArray(Utils.toPoint(me.getVelocity())));
        gameState.put("tv", Utils.toIntArray(Utils.toPoint(tagPlayer.getVelocity())));

        Point2D myPosition = me.getStaticInfo().getPos();
        Point2D taggedPosition = tagPlayer.getStaticInfo().getPos();
        int distance = (int) taggedPosition.distance(myPosition);

        gameState.put("d", distance);
        gameState.put("tc", tagPlayer != oldTagPlayer);

        String newState = gameState.toString();
        communicator.sendState(newState);
    }

    private void handleTaggingLogic() {
        for (TagPlayer player : players) {
            if (player != tagPlayer && tagPlayer.isTagging(player)) {
                setTag(tagPlayer, player);
                tagPlayer.setSteeringBehavior(TagPlayer.idleSteering);
                tagChangedTime = System.currentTimeMillis();
                return;
            }
        }
    }

    private void setTag(TagPlayer oldPlayer, TagPlayer newPlayer) {
        oldTagPlayer = oldPlayer;
        tagPlayer = newPlayer;
        players.forEach((p) -> p.setIsTagged(p == newPlayer));
    }
}
