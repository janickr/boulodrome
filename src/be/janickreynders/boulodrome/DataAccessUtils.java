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

import be.janickreynders.bubblegum.Request;
import com.google.appengine.api.datastore.*;

public class DataAccessUtils {
    public static DatastoreService datastore() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    public static Key getKey(Request request, String param) {
        return KeyFactory.stringToKey(request.param(param));
    }

    public static Long inc(Entity entity, String propertyName) {
        return addToProperty(entity, propertyName, 1);
    }

    public static Long dec(Entity entity, String propertyName) {
        return addToProperty(entity, propertyName, -1);
    }

    private static Long addToProperty(Entity entity, String propertyName, int val) {
        Long value = (Long) entity.getProperty(propertyName);
        Long property = (value != null ? value : 0) + val;
        entity.setProperty(propertyName, property);
        return property;
    }

    public static Iterable<Entity> list(Query query, DatastoreService datastore) {
        return datastore.prepare(query).asIterable();
    }
}
