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
    private final int RL_PLAYER_INDEX = 0;

    private Communicator communicator;
    private List<TagPlayer> players;
    private TagPlayer tagPlayer;
    private double tagChangedTime;
    private ArrayList<Color> colors;

    public TagArena() {
        super();
        this.players = new ArrayList<>();
        this.tagChangedTime = 0;
        this.tagPlayer = null;
        colors = new ArrayList<>(Arrays.asList(Color.blue, Color.green, Color.yellow, Color.magenta, Color.cyan, Color.orange, Color.pink));
        colors = colors.stream().filter(color -> color != TagGame.RL_PLAYER_COLOR).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public int getID() {
        return 0;
    }

    private void initGame() {
        players.clear();
        tagPlayer = null;
        this.tagChangedTime = 0;

        Random rand = new Random();

        for (int i = 0; i < TagGame.PLAYER_COUNT; i++) {
            TagPlayer player = new TagPlayer(
                    i == RL_PLAYER_INDEX ? TagGame.RL_PLAYER_NAME : ("P" + (i + 1)),
                    new StaticInfo(new Point2D(rand.nextDouble() * (TagGame.DemoWidth), rand.nextDouble() * (TagGame.DemoHeight))),
                    i == RL_PLAYER_INDEX ? TagGame.RL_PLAYER_COLOR : colors.get(i % colors.size())
            );
            players.add(player);
        }

        setTag(getRandomNonRLPlayer());
    }

    private TagPlayer getRandomNonRLPlayer() {
        Random rand = new Random();
        TagPlayer randomPlayer;
        do {
            randomPlayer = players.get(rand.nextInt(players.size()));
        } while (players.indexOf(randomPlayer) == RL_PLAYER_INDEX);
        return randomPlayer;
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

            String action = communicator.receiveAction();
            if (action == null || action.equalsIgnoreCase(Communicator.EXIT)) return;

            if (action.equals(Communicator.RESET)) {
                initGame();
            } else {
                RL_player.setSteeringBehavior((StaticInfo staticInfo, Vector2D currentVelocity) ->
                        deserializeAction(action).times(TagGame.MAX_VELOCITY));
            }

            boolean taggerSleeping = System.currentTimeMillis() - tagChangedTime < TagGame.TAGGER_SLEEP_TIME_MS;
            if (!taggerSleeping) {
                tagPlayer.setSteeringBehavior(new DumbTagSteering(tagPlayer, this, (double) TagGame.MAX_VELOCITY * 0.7));
                handleTaggingLogic();
            }

            for (TagPlayer player : players) {
                player.update(time);
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

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void sendSerializedGameState(TagPlayer me) {
        JSONObject gameState = new JSONObject();

        var mv = Utils.toIntArray(Utils.toPoint(me.getVelocity().normalize()));
        var tv = Utils.toIntArray(Utils.toPoint(tagPlayer.getVelocity().normalize()));

        gameState.put("mv", mv);
        gameState.put("tv", tv);

        Point2D myPosition = me.getStaticInfo().getPos();
        Point2D taggedPosition = tagPlayer.getStaticInfo().getPos();

        if (me == tagPlayer) {
            gameState.put("d", 0);
        } else {
            double distance = taggedPosition.distance(myPosition);
            int bin_size = (int) ((TagGame.getMaxDistance() / TagGame.DISTANCE_LEVEL_COUNT));
            long discretized_distance = Math.round(this.clamp((distance / bin_size), 1, TagGame.DISTANCE_LEVEL_COUNT - 1));

            gameState.put("d", discretized_distance);
        }

        String newState = gameState.toString();
//        System.out.println("Sending: " + newState);
        communicator.sendState(newState);
    }

    private void handleTaggingLogic() {
        for (TagPlayer player : players) {
            if (player != tagPlayer && tagPlayer.isTagging(player)) {
                setTag(player);
                tagPlayer.setSteeringBehavior(TagPlayer.idleSteering);
                tagChangedTime = System.currentTimeMillis();
                return;
            }
        }
    }

    private void setTag(TagPlayer newPlayer) {
        tagPlayer = newPlayer;
        players.forEach((p) -> p.setIsTagged(p == newPlayer));
    }
}
