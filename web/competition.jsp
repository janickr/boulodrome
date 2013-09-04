<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  ~ MIT license
  ~
  ~ Copyright (c) 2013 Janick Reynders
  ~
  ~ Permission is hereby granted, free of charge, to any person obtaining a copy of this
  ~ software and associated documentation files (the "Software"), to deal in the Software
  ~ without restriction, including without limitation the rights to use, copy, modify,
  ~ merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  ~ permit persons to whom the Software is furnished to do so, subject to the following
  ~ conditions:
  ~
  ~ The above copyright notice and this permission notice shall be included in all copies
  ~ or substantial portions of the Software.
  ~
  ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  ~ INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
  ~ PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  ~ HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
  ~ CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
  ~ OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  --%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <title>Boulodrome</title>
    <link href='http://fonts.googleapis.com/css?family=Open+Sans' rel='stylesheet' type='text/css'>
    <link rel="stylesheet" type="text/css" href="/css/reset.css"/>
    <link rel="stylesheet" type="text/css" href="/css/default.css"/>
    <link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.9.2/jquery-ui-1.9.2.custom.min.css"/>
    <script type="text/javascript" src="/js/jquery-1.8.2.min.js" ></script>
    <script type="text/javascript" src="/js/underscore-min.js" ></script>
    <script type="text/javascript" src="/js/jquery-ui-1.9.2.custom.min.js" ></script>
</head>
<body>
<h1></h1>
<div style="text-align: center"><a href="#" id="openConfiguration">configure</a></div>
<form accept-charset="utf-8" method="POST" id="configureCompetition" style="display:none; width:500px; margin-left: auto; margin-right: auto;">
    <p><label for="competitionName">Name of the competition:</label> <input type="text" name="name" id="competitionName" style="width: 100%"/></p>
    <br><p><label for="percentagePlayedThreshold">Percentage of games you need to play to take your place in the player statistics (0%-100%):</label> <input type="text" size="4" name="percentagePlayedTreshold" id="percentagePlayedThreshold"/></p>
    <br><div style="text-align: right"><input type="button" id="cancelConfig" value="Cancel"/><input type="button" id="saveConfig" value="Save Changes"/></div>
</form>

<div id="leaderboard">
    <table>
        <thead>
            <tr>
                <th></th>
                <th>Win ratio</th>
                <th>Games won</th>
                <th>Games played</th>
                <th>Percentage played</th>
                <th>Rating</th>
            </tr>
        </thead>
        <tfoot>
            <tr>
                <td colspan="5">
                <form accept-charset="utf-8" method="POST" id="addParticipant" style="text-align: right" >
                    <input type="text" size="10" name="name"/><input type="button" id="submitbutton" value="Add Participant"/>
                </form>
                </td>
            </tr>
        </tfoot>
        <tbody>
        </tbody>
    </table>
</div>
<div id="games">
    <form accept-charset="utf-8" method="POST" action="/games/competitionId">
        <table>
        <thead>
        </thead>
        <tbody></tbody>
    </table>
    </form>
</div>

<div style="width: 100%;text-align: center;font-size: smaller;padding: 4px;margin-top: 50px;clear: both">powered by <a href="http://janickreynders.be/bubblegum/">bubblegum</a></div>
<script type="text/javascript">
    var competitionId = '<c:out value="${bubblegumParams.id}"/>';
    _.templateSettings = {
        interpolate : /\{\{(.+?)\}\}/g
    };
    var participants = [];

    function renderParticipants(data) {
        participants = data;
        getGames();
        getCompetition();

        var players = $('#leaderboard tbody');

        players.empty();
        _.each(data, function(player) {
            playerHtml = player.rating > 0 ? '<tr style="color: black">' : '<tr style="color: grey">';
            playerHtml += '<td>' + player.name + '</td><td>' + ((player.won) ? (player.won*100/player.played).toFixed(2) : 0) +'%</td><td>'+ (player.won || 0) +'</td><td>'+ player.played +'</td>';
            if ('percentagePlayed' in player) {
                playerHtml += '<td>'+player.percentagePlayed+'%</td>';
            } else {
                playerHtml += '<td>?</td>';
            }
            playerHtml += '<td>' + (player.displayRating || player.rating) +'</td>'
            if (player.played == 0) {
                playerHtml += '<td style="width:20px"><img src="/img/sharp_grey_action_delete.png" alt="delete" onclick="deletePlayer(\''+player.id+'\')"></td>';
            }
            playerHtml += '</tr>';
            players.append(playerHtml);
        });
        $('#submitbutton').removeAttr('disabled');
    }

    function renderGames(data) {
        var players = $('#games thead');
        players.empty();
        players.append('<tr></tr>');
        players = $('#games thead tr');
        players.append('<th>Date</th>');

        var input = $('#games tbody');
        input.empty();
        input.append('<tr id="gameInput"></tr>');
        input = $('#games tbody tr');
        input.append('<td><input id="datepicker" type="text" size="7" name="date"/></td>');

        _.each(participants, function(player) {
            players.append('<th>'+player.name+'</th>');
            input.append('<td><input type="text" size="2" name="player-'+player.name+'"/></td>');
        });

        input.append('<td><input type="button" id="addGame" value="Add Game"/></td>');

        var games = $('#games tbody');

        _.each(data, function(game) {
            var gameHtml = '<tr>';
            gameHtml += '<td>'+game.date+'</td>';
            _.each(participants, function(player) {
                var points = _.has(game, 'player-'+player.name) ? game['player-'+player.name] : '';
                gameHtml += '<td>'+points +'</td>';
            });
            gameHtml += '<td><img src="/img/sharp_grey_action_delete.png" alt="delete" onclick="deleteGame(\''+game.id+'\')"></td>';
            gameHtml += '</tr>';
            games.append(gameHtml);
        });

        $( "#datepicker" ).datepicker({ "dateFormat":"dd/mm/yy"});

        $("#addGame").click(function() {
            $.post("/competitions/"+competitionId+'/games', $("#games form").serialize(), getParticipants, "json")
            $("#addGame").attr("disabled", "disabled");
        })

    }

    function renderCompetition(data) {
        $('title,h1').text(data.name);
        $('#competitionName').val(data.name);
        $('#percentagePlayedThreshold').val(data.percentagePlayedThreshold);
        $('#saveConfig').removeAttr('disabled');
        $("#configureCompetition").hide();
        $("#openConfiguration").show();
    }

    function deleteGame(id) {
        $.ajax({type: "DELETE", url: "/competitions/"+competitionId+'/games/'+id, success: getParticipants, dataType: "json" });
    }

    function deletePlayer(id) {
        $.ajax({type: "DELETE", url: "/competitions/"+competitionId+'/participants/'+id, success: getParticipants, dataType: "json" });
    }


    function getParticipants() {
        $.getJSON('/competitions/'+competitionId+'/participants', renderParticipants);
    }

    function getGames() {
        $.getJSON('/competitions/'+competitionId+'/games', renderGames);
    }

    function getCompetition() {
        $.getJSON('/competitions/'+competitionId+'', renderCompetition);
    }

    getParticipants();

    function postParticipant() {
        $("#submitbutton").attr("disabled", "disabled");
        $.post("/competitions/" + competitionId + '/participants', $("#addParticipant").serialize(), getParticipants, "json");
        $("#addParticipant input:text").val("");
        return false;
    }

    function saveConfig() {
        $("#saveConfig").attr("disabled", "disabled");
        $.post("/competitions/" + competitionId, $("#configureCompetition").serialize(), getParticipants, "json");
        return false;
    }

    $("#submitbutton").click(postParticipant);
    $("#addParticipant").submit(postParticipant);
    $("#saveConfig").click(saveConfig);
    $("#cancelConfig").click(function() {
        $("#configureCompetition").hide();
        $("#openConfiguration").show();
    });
    $("#openConfiguration").click(function() {
        $("#configureCompetition").show();
        $("#openConfiguration").hide();
    });



</script>
<script type="text/javascript">

    var _gaq = _gaq || [];
    _gaq.push(['_setAccount', 'UA-37743435-1']);
    _gaq.push(['_trackPageview']);

    (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
    })();

</script>
</body>
</html>