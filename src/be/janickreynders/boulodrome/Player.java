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

import java.util.Arrays;
import java.util.List;

public abstract class Player implements Comparable<Player> {
    public abstract int getRating();
    public abstract void adjust(double delta);

    public int compareTo(Player o) {
        return getRating()-o.getRating();
    }

    public abstract int size();

    public abstract void addWin();

    public abstract void addLoss();

    public static class Team extends Player {

        private List<? extends Player> members;

        public Team(Player... members) {
            this(Arrays.asList(members));
        }

        public Team(List<? extends Player> members) {
            this.members = members;
        }

        @Override
        public int getRating() {
            int total = 0;
            for (Player member : members) {
                total += member.getRating();
            }
            return total / members.size();
        }

        @Override
        public void adjust(double delta) {
            delta = Math.round((delta / (double) members.size())); // verdeel over spelers zodat punten niet lekken

            for (Player member : members) {
                member.adjust(delta);
            }
        }

        @Override
        public int size() {
            return members.size();
        }

        @Override
        public void addWin() {
            for (Player member : members) {
                member.addWin();
            }
        }

        @Override
        public void addLoss() {
            for (Player member : members) {
                member.addLoss();
            }

        }

        @Override
        public String toString() {
            return  members + ": " + getRating();
        }
    }
    
    public static class Individual extends Player {
        private String name;
        private int rating = 1000;
        private int won = 0;
        private int played = 0;

        public Individual(String name) {
            this(name, 1000);
        }

        public Individual(String name, int rating) {
            this.name = name;
            this.rating = rating;
        }

        @Override
        public int getRating() {
            return rating;
        }

        @Override
        public void adjust(double delta) {
            rating += Math.round(delta);
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public void addWin() {
            won++;
            played++;
        }

        @Override
        public void addLoss() {
            played++;
        }

        @Override
        public String toString() {
            return name + ": " + rating;
        }

        public String getName() {
            return name;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public int getWon() {
            return won;
        }

        public int getPlayed() {
            return played;
        }
    }
}
