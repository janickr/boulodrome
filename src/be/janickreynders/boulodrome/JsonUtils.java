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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    public static final Gson GSON = new Gson();

    public static String json(Entity entity) {
        return json(getProperties(entity));
    }

    public static String json(Map<String, Object> properties) {
        return GSON.toJson(properties);
    }

    private static Map<String, Object> getProperties(Entity entity) {
        Map<String, Object> properties = new HashMap<String, Object>(entity.getProperties());
        properties.put("id", KeyFactory.keyToString(entity.getKey()));
        return properties;
    }

    public static String json(Iterable<Entity> entityIterator) {
        List<Map> result = new ArrayList<Map>();

        for (Entity entity : entityIterator) {
            result.add(getProperties(entity));
        }

        return GSON.toJson(result);
    }
}
