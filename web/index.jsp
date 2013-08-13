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
    <title>Boulodrome - Start a petanque competition</title>
    <link rel="stylesheet" type="text/css" href="css/reset.css"/>
    <link rel="stylesheet" type="text/css" href="css/default.css"/>
    <script type="text/javascript" src="js/jquery-1.8.2.min.js" ></script>
    <script type="text/javascript" src="js/underscore-min.js" ></script>
</head>
<body>
<h1>Boulodrome</h1>
<h2>Petanque competition stats made easy</h2>
<div style="width: 100%;text-align: center;font-size: smaller;padding: 4px;margin-top: 5px">powered by <a href="http://janickreynders.be/bubblegum/">bubblegum</a></div>


<div id="newCompetition">
    <form accept-charset="utf-8" method="POST" action="competitions">
        <label>
            Start a new competition:<br/>
            <input type="text" size="30" name="name" placeholder="Competition Name"/>
        </label><input type="button" id="submitbutton" value="Go ->"/>
    </form>
</div>

<script type="text/javascript">
    function goToCompetition(data) {
        window.location.assign(window.location + 'competitions/' + data.id);
    }

    function postCompetition() {
        $.post("competitions", $("#newCompetition form").serialize(), goToCompetition, "json");
        return false;
    }
    $("#submitbutton").click(postCompetition);
    $("form").submit(postCompetition);
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