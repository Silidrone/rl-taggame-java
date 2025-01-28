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
    private Communicator communicator;

    private List<TagPlayer> players;
    private TagPlayer tagPlayer;
    private final int RL_PLAYER_INDEX = 0;
    private double tagChangedTime;

    public TagArena() {
        super();
        this.players = new ArrayList<>();
        this.tagChangedTime = 0;
    }

    @Override
    public int getID() {
        return 0;
    }

    private void initGame() {
        players.clear();
        Random rand = new Random();
        ArrayList<Color> colors = new ArrayList<>(Arrays.asList(Color.blue, Color.green, Color.yellow, Color.magenta, Color.cyan, Color.orange, Color.pink));
        colors = colors.stream().filter(color -> color != TagGame.RL_PLAYER_COLOR).collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < TagGame.PLAYER_COUNT; i++) {
            TagPlayer player = new TagPlayer(
                    i == RL_PLAYER_INDEX ? TagGame.RL_PLAYER_NAME : ("P" + (i + 1)),
                    new StaticInfo(new Point2D(rand.nextDouble() * (TagGame.DemoWidth - 200), rand.nextDouble() * (TagGame.DemoHeight - 200))),
                    i == RL_PLAYER_INDEX ? TagGame.RL_PLAYER_COLOR : colors.get(i % colors.size())
            );
            if (i != RL_PLAYER_INDEX) {
                player.setSteeringBehavior(new DumbTagSteering(player, this));
            }
            players.add(player);
        }

        setTag(players.get(Utils.randomInt(players.size())));
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
        graphics.setColor(TagGame.PanelColor);
        graphics.fillRect(0, 0, (float) TagGame.DemoWidth, (float) TagGame.DemoHeight);

        graphics.translate(0, (float) TagGame.DemoHeight);
        graphics.scale(1, -1);

        players.forEach(e -> e.render(graphics));

        graphics.scale(1, -1);
        graphics.translate(0, (float) -TagGame.DemoHeight);
    }

    @Override
    public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int time) throws SlickException {
        try {
            TagPlayer RL_player = players.get(RL_PLAYER_INDEX);

            // RL control is only used when playing as a non-tag player (i.e. evader).
            if (tagPlayer != RL_player) {
                String action = communicator.receiveAction();
                if (action == null || action.equalsIgnoreCase(Communicator.EXIT)) return;

                if (action.equals(Communicator.RESET)) {
                    initGame();
                } else {
                    RL_player.setSteeringBehavior((StaticInfo staticInfo, Vector2D currentVelocity) -> deserializeAction(action));
                }
            }

            boolean taggerSleeping = System.currentTimeMillis() - tagChangedTime < TagGame.TAGGER_SLEEP_TIME_MS;
            if (!taggerSleeping) {
                tagPlayer.setSteeringBehavior(new DumbTagSteering(tagPlayer, this));
                handleTaggingLogic();
            }

            for (TagPlayer player : players) {
                player.update(time);
            }

            // RL control is only used when playing as a non-tag player (i.e. evader).
            if (tagPlayer != RL_player) {
                sendSerializedGameState(players.get(RL_PLAYER_INDEX));
            }
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

        if (me == tagPlayer) {
            gameState.put("d", 0);
        } else {
            int distance = (int) taggedPosition.distance(myPosition);
            gameState.put("d", distance);
        }

        String newState = gameState.toString();
        communicator.sendState(newState);
    }

    private void handleTaggingLogic() {
        for (TagPlayer player : players) {
            if (player != tagPlayer && tagPlayer.isTagging(player)) {
                setTag(player);
                tagPlayer.setSteeringBehavior(TagPlayer.idleSteering);
                tagChangedTime = System.currentTimeMillis();
                if(tagPlayer == players.get(RL_PLAYER_INDEX)) {
                    sendSerializedGameState(players.get(RL_PLAYER_INDEX));
                }
                return;
            }
        }
    }

    private void setTag(TagPlayer newPlayer) {
        tagPlayer = newPlayer;
        players.forEach((p) -> p.setIsTagged(p == newPlayer));
    }
}
