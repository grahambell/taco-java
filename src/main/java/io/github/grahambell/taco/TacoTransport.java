/*
 * Taco Java transport class.
 * Copyright (C) 2014-2015 Graham Bell
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.grahambell.taco;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class TacoTransport {
    protected BufferedReader in;
    protected Writer out;
    protected Filter filter;

    public TacoTransport(InputStream in, OutputStream out, Filter filter) {
        try {
            this.in = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            this.out = new OutputStreamWriter(out, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not supported");
        }

        this.filter = filter;
    }

    public Map<String, Object> read() throws TacoException {
        StringBuilder text = new StringBuilder();

        while (true) {
            try {
                String line = in.readLine();

                if (line == null || line.startsWith("// END")) {
                    break;
                }

                text.append(line);
                text.append('\n');
            }
            catch (IOException e) {
                throw new TacoException("read error: " + e.getMessage(), e);
            }
        }

        if (text.length() == 0) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(text.toString());
            return jsonToMap(json);
        }
        catch (JSONException e) {
            throw new TacoException("read error: " + e.getMessage(), e);
        }
    }

    public void write(Map<String, Object> message) throws TacoException {
        try {
            JSONObject json = mapToJson(message);
            json.write(out);
            out.write("\n// END\n");
            out.flush();
        }
        catch (JSONException e) {
            throw new TacoException("json write error: " + e.getMessage(), e);
        }
        catch (IOException e) {
            throw new TacoException("i/o write error: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> jsonToMap(JSONObject json)
            throws TacoException {
        Map map = new HashMap<String, Object>();

        for (Iterator<String> i = json.keys(); i.hasNext(); ) {
            String key = i.next();
            map.put(key, jsonToObject(json.get(key)));
        }

        return map;
    }

    public Collection<Object> jsonToCollection(JSONArray json)
            throws TacoException {
        Collection<Object> list = new ArrayList<Object>();

        for (int i = 0; i < json.length(); i ++) {
            list.add(jsonToObject(json.get(i)));
        }

        return list;
    }

    public Object jsonToObject(Object value) throws TacoException {
        if (JSONObject.NULL == value) {
            return null;
        }
        else if (value instanceof JSONObject) {
            Map<String, Object> map = jsonToMap((JSONObject) value);

            if (filter == null) {
                return map;
            }
            else {
                return filter.mapToObject(map);
            }
        }
        else if (value instanceof JSONArray) {
            return jsonToCollection((JSONArray) value);
        }
        else if ((value instanceof Boolean)
                || (value instanceof Number)
                || (value instanceof String)) {
            return value;
        }
        else {
            throw new TacoException("unexpected type in JSONObject");
        }
    }

    public JSONObject mapToJson(Map<String, Object> map)
            throws TacoException {
        JSONObject json = new JSONObject();

        for (Map.Entry<String, Object> entry: map.entrySet()) {
            json.put(entry.getKey(), objectToJson(entry.getValue()));
        }

        return json;
    }

    public JSONArray collectionToJson(Collection<Object> list)
            throws TacoException {
        JSONArray json = new JSONArray();

        for (Object value: list) {
            json.put(objectToJson(value));
        }

        return json;
    }

    public Object objectToJson(Object value) throws TacoException {
        if (value == null) {
            return JSONObject.NULL;
        }
        else if (value instanceof Map) {
            return mapToJson((Map<String, Object>) value);
        }
        else if (value instanceof Collection) {
            return collectionToJson((Collection<Object>) value);
        }
        else if ((value instanceof Boolean)
                || (value instanceof Number)
                || (value instanceof String)) {
            return value;
        }
        else {
            if (filter == null) {
                throw new TacoException("unknown object type to turn to JSON");
            }
            else {
                return filter.objectToMap(value);
            }
        }
    }

    public interface Filter {
        public Map<String, Object> objectToMap(Object value)
                throws TacoException;
        public Object mapToObject(Map<String, Object> map)
                throws TacoException;
    }
}
