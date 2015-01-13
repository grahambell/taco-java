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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * Class for handling the communication between Taco clients and
 * servers.
 *
 * This class provides an interface based on reading and writing messages
 * as <code>Map&lt;String, Object&gt;</code> objects.  It converts these
 * messages to and from JSON using the <code>org.json</code> library and
 * passes them over the specified streams.
 */
public class TacoTransport {
    /**
     * Reader for the input stream.
     */

    protected BufferedReader in;
    /**
     * Writer for the output stream.
     */
    protected Writer out;

    /**
     * Object filter.
     */
    protected Filter filter;

    /**
     * Construct new TacoTransport object.
     *
     * A reader and writer will be constructed for the given streams.
     *
     * @param in input stream
     * @param out output stream
     * @param filter object filter, or null if not required
     */
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

    /**
     * Constructor which does not set up input and output streams.
     *
     * @param filter object filter
     */
    protected TacoTransport(Filter filter) {
        this.in = null;
        this.out = null;
        this.filter = filter;
    }

    /**
     * Read one message from the input stream.
     *
     * @return the message as a <code>Map</code> object
     * @throws TacoException on error reading or parsing the message
     */
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

    /**
     * Write a message to the output stream.
     *
     * @param message the message to be written
     * @throws TacoException on error converting the message to JSON or
     *     writing it to the output stream
     */
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

    /**
     * Convert JSON object to Java Map.
     *
     * Each entry in the object is converted using the {@link #jsonToObject}
     * method.
     *
     * @param json the JSON object
     * @return Java Map representation of the object
     * @throws TacoException on error in conversion
     */
    public Map<String, Object> jsonToMap(JSONObject json)
            throws TacoException {
        Map map = new HashMap<String, Object>();

        for (Iterator<String> i = json.keys(); i.hasNext(); ) {
            String key = i.next();
            map.put(key, jsonToObject(json.get(key)));
        }

        return map;
    }

    /**
     * Convert JSON array to Java List.
     *
     * Each entry in the object is converted using the {@link #jsonToObject}
     * method.
     *
     * @param json the JSON array
     * @return Java List representation of the array
     * @throws TacoException on error in conversion
     */
    public List<Object> jsonToList(JSONArray json)
            throws TacoException {
        List<Object> list = new ArrayList<Object>();

        for (int i = 0; i < json.length(); i ++) {
            list.add(jsonToObject(json.get(i)));
        }

        return list;
    }

    /**
     * Convert an individual JSON entry to a Java Object.
     *
     * JSON objects and arrays are converted using the {@link #jsonToMap}
     * and {@link #jsonToList} methods.  Instances of
     * <code>Boolean</code>, <code>Number</code> and <code>String</code>
     * are returned as they are.  If an object filter has been specified
     * then its <code>mapToObject</code> method is applied to JSON
     * objects which have been conveted to <code>Map</code> instances.
     *
     * @param value an object obtained by parsing part of a JSON message
     * @return a Java representation of the value
     * @throws TacoException on error in conversion
     */
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
            return jsonToList((JSONArray) value);
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

    /**
     * Convert a Java Map to a JSON object.
     *
     * Entries of the <code>Map</code> are converted using the
     * {@link #objectToJson} method.
     *
     * @param map the input Map
     * @return a JSON representation of the Map
     * @throws TacoException on error in conversion
     */
    public JSONObject mapToJson(Map<String, Object> map)
            throws TacoException {
        JSONObject json = new JSONObject();

        for (Map.Entry<String, Object> entry: map.entrySet()) {
            json.put(entry.getKey(), objectToJson(entry.getValue()));
        }

        return json;
    }

    /**
     * Convert a Java Collection to a JSON array.
     *
     * Entries of the <code>Collection</code> are converted using the
     * {@link #objectToJson} method.
     *
     * @param list the input Collection
     * @return a JSON representation of the Collection
     * @throws TacoException on error in conversion
     */
    public JSONArray collectionToJson(Collection<Object> list)
            throws TacoException {
        JSONArray json = new JSONArray();

        for (Object value: list) {
            json.put(objectToJson(value));
        }

        return json;
    }

    /**
     * Convert an individual Java object to an object suitable for
     * representation in JSON.
     *
     * Instances of <code>Map</code> and <code>Collection</code>
     * are converted using the {@link #mapToJson} and {@link #collectionToJson}
     * methods.  Null entries are converted to JSON null values.
     * Instances of <code>Boolean</code>, <code>Number</code> and
     * <code>String</code> are returned as they are.  If an object filter
     * has been specified then its <code>objectToMap</code> is applied
     * to any other type of value.  When no filter has been provided
     * and an unknown object type is encountered, an exception is thrown.
     *
     * @param value the input object
     * @return an object suitable for representation in JSON
     * @throws TacoException on error in conversion
     */
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
                return mapToJson(filter.objectToMap(value));
            }
        }
    }

    /**
     * Interface for object filtering methods.
     *
     * An implementation of this class can be provided to the TacoTransport
     * constructor in order to specify how to handle the conversion of
     * unknown objects to JSON, and also to give a special interpretation
     * for decoded JSON objects.
     */
    public interface Filter {
        /**
         * Attempt to convert an arbitrary object to a Map for encoding
         * as JSON.
         *
         * @param value the object for conversion
         * @return the converted Map
         * @throws TacoException if conversion is not possible
         */
        public Map<String, Object> objectToMap(Object value)
                throws TacoException;

        /**
         * Process decoded JSON objects.
         *
         * This method will be applied to <code>Map</code> instances obtained
         * from decoded JSON objects.
         *
         * @param map the decoded JSON object
         * @return the processed object, or a reference to the original map
         *     if no processing is necessary
         * @throws TacoException if conversion fails
         */
        public Object mapToObject(Map<String, Object> map)
                throws TacoException;
    }
}
