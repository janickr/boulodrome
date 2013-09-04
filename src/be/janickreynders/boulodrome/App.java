/*
 * MIT license
 *
 * Copyright (c) 2013 Janick Reynders
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package be.janickreynders.boulodrome;

import be.janickreynders.bubblegum.*;
import com.google.appengine.api.datastore.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static be.janickreynders.boulodrome.DataAccessUtils.*;
import static be.janickreynders.boulodrome.JsonUtils.json;
import static be.janickreynders.bubblegum.Filters.catchAndHandle;
import static be.janickreynders.bubblegum.Handlers.status;
import static be.janickreynders.bubblegum.Matchers.accept;
import static java.util.Collections.singletonMap;

public class App implements be.janickreynders.bubblegum.App {
    @Override
    public Config createConfig() {
        Config on = new Config();

        on.apply(catchAndHandle(EntityNotFoundException.class, status(HttpServletResponse.SC_NOT_FOUND)));

        RequestMatcher json = accept("application/json");

        on.post("/competitions", json, new Handler() {
            @Override
            public void handle(Request req, Response resp) throws IOException {
                Entity competition = new Entity("Competition");
                addAllParams(req, competition);
                competition.setProperty("startDate", new Date());
                datastore().put(competition);
                resp.ok(jsonEntityID(competition));
            }
        });

        on.get("/competitions/:id", accept("text/html"), new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.vary("Accept");
                req.forward("/competition.jsp", resp);
            }
        });

        on.get("/competitions/:id", json, new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                resp.vary("Accept");
                resp.ok(json(datastore().get(getKey(req, "id"))));
            }
        });


        on.post("/competitions/:id", json, new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                DatastoreService datastore = datastore();
                Key competitionId = getKey(req, "id");
                Entity competition = datastore.get(competitionId);
                addAllParams(req, competition);
                datastore.put(competition);
                updateParticipantStats(competitionId, datastore);

                resp.ok(json(competition));
            }
        });

        on.get("/competitions/:id/games", json, new Handler() {
            @Override
            public void handle(Request req, Response resp) throws IOException {
                resp.ok(json(list(new Query("Game", getKey(req, "id")).addSort("timeMillis", Query.SortDirection.DESCENDING).addSort("creationTime", Query.SortDirection.DESCENDING), datastore())));
            }
        });

        on.post("/competitions/:id/games", json, new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                Key key = getKey(req, "id");
                DatastoreService datastore = datastore();

                Entity game = new Entity("Game", key);
                addAllParams(req, game);
                game.setProperty("creationTime", new Date().getTime());
                game.setProperty("timeMillis", parseDate(req.queryParam("date")).getTime());
                decideWinnersAndLosers(game);
                datastore.put(game);
                updateParticipantStats(key, datastore);
                resp.ok(jsonEntityID(game));
            }
        });

        on.delete("/competitions/:id/games/:gameId", json, new Handler() {

            @Override
            public void handle(Request req, Response resp) throws Exception {
                Key key = getKey(req, "id");
                DatastoreService datastore = datastore();

                datastore.delete(getKey(req, "gameId"));
                updateParticipantStats(key, datastore);
            }
        });

        on.get("/competitions/:id/participants", json, new Handler() {
            @Override
            public void handle(Request req, Response resp) throws IOException {
                resp.ok(json(getParticipants(getKey(req, "id"), datastore())));
            }
        });

        on.delete("/competitions/:id/participants/:participantId", json, new Handler() {

            @Override
            public void handle(Request req, Response resp) throws Exception {
                datastore().delete(getKey(req, "participantId"));
            }
        });


        on.post("/competitions/:id/participants", json, new Handler() {
            @Override
            public void handle(Request req, Response resp) throws Exception {
                Key key = getKey(req, "id");
                DatastoreService datastore = datastore();

                Entity participant = new Entity("Participant", key);
                participant.setProperty("rating", 0);
                participant.setProperty("played", 0);
                participant.setProperty("won", 0);
                participant.setProperty("percentagePlayed", 0);
                participant.setProperty("displayRating", 1000);
                addAllParams(req, participant);
                datastore.put(participant);
                resp.ok(jsonEntityID(participant));
            }

        });

        return on;
    }

    private Date parseDate(String date) {
        try {
            return new SimpleDateFormat("dd/MM/yyyy").parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String jsonEntityID(Entity entity) {
        return json(singletonMap("id", (Object) KeyFactory.keyToString(entity.getKey())));
    }

    private Iterable<Entity> getParticipants(Key id, DatastoreService datastore) {
        return list(new Query("Participant", id).addSort("rating", Query.SortDirection.DESCENDING).addSort("won", Query.SortDirection.DESCENDING), datastore);
    }

    private void addAllParams(Request request, Entity entity) {
        Set<String> parameterNames = request.queryParams();
        for (String parameterName : parameterNames) {
            entity.setProperty(parameterName, request.queryParam(parameterName));
        }
    }

    private void updateParticipantStats(Key competitionId, DatastoreService datastore) throws EntityNotFoundException {
        Iterable<Entity> games = datastore.prepare(new Query("Game", competitionId).addSort("timeMillis").addSort("creationTime")).asIterable();
        Iterable<Entity> participants = getParticipants(competitionId, datastore);
        Map<String, Player.Individual> players = getPlayers(participants);

        int totalGames = 0;

        Competition c = new Competition();
        for (Entity game : games) {
            c.playGame(new Player.Team(getPlayers(game, "winners", players)), new Player.Team(getPlayers(game, "losers", players)));
            totalGames++;
        }

        String percentagePlayedThreshold = (String) datastore.get(competitionId).getProperty("percentagePlayedThreshold");
        int minimumPercentagePlayed = (percentagePlayedThreshold != null && !percentagePlayedThreshold.isEmpty()) ? Integer.valueOf(percentagePlayedThreshold) : 0;
        for (Entity participant : participants) {
            String name = (String) participant.getProperty("name");
            Player.Individual player = players.get(name);
            int percentagePlayed = player.getPlayed() * 100 / totalGames;
            participant.setProperty("rating", (percentagePlayed >= minimumPercentagePlayed) ? player.getRating() : 0);
            participant.setProperty("displayRating", player.getRating());
            participant.setProperty("played", player.getPlayed());
            participant.setProperty("won", player.getWon());
            participant.setProperty("percentagePlayed", percentagePlayed);
            datastore.put(participant);
        }
    }

    private Map<String, Player.Individual> getPlayers(Iterable<Entity> participants) {
        HashMap<String, Player.Individual> players = new HashMap<String, Player.Individual>();
        for (Entity entity : participants) {
            String name = (String) entity.getProperty("name");
            players.put(name, new Player.Individual(name));
        }
        return players;
    }

    private List<String> getParticipantNamesOfGame(Entity game) {
        ArrayList<String> names = new ArrayList<String>();
        for (String name : game.getProperties().keySet()) {
            if (name.startsWith("player-")) {
                names.add(name.substring(7));
            }
        }
        return names;
    }

    private void decideWinnersAndLosers(Entity game) {
        List<String> namesOfGame = getParticipantNamesOfGame(game);
        int highest = 0;
        for (String name : namesOfGame) {
            String number = (String) game.getProperty("player-" + name);
            if (number.matches("\\d+")) {
                highest = Math.max(highest, Integer.valueOf(number));
            }
        }


        List<String> winners = new ArrayList<String>();
        List<String> losers = new ArrayList<String>();
        for (String name : namesOfGame) {
            String number = (String) game.getProperty("player-" + name);
            if (number.matches("\\d+")) {
                if (Integer.valueOf(number).equals(highest)) {
                    winners.add(name);
                } else {
                    losers.add(name);
                }
            }
        }

        game.setProperty("winners", winners);
        game.setProperty("losers", losers);
    }


    private List<Player.Individual> getPlayers(Entity game, String property, Map<String, Player.Individual> players) {
        List<String> names = (List<String>) game.getProperty(property);
        ArrayList<Player.Individual> result = new ArrayList<Player.Individual>();
        for (String name : names) {
            result.add(players.get(name));
        }
        return result;
    }
}
