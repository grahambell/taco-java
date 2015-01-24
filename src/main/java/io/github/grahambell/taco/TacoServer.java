/*
 * Taco Java server class.
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

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.IllegalArgumentException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Taco server implementation.
 */
public class TacoServer implements TacoTransport.Filter {
    /**
     * TacoTransport object used for communication.
     */
    protected TacoTransport xp;

    /**
     * Cache of Taco server-side objects.
     */
    protected Map<Integer, Object> objects = new HashMap();

    /**
     * Server-side object counter.
     *
     * This is incremented each time an object is stored in the cache.
     */
    protected int objectNum = 0;

    /**
     * Constructor.
     *
     * Creates a TacoTransport using the given streams.
     *
     * @param in input stream
     * @param out output stream
     */
    public TacoServer(InputStream in, OutputStream out) {
        xp = new TacoTransport(System.in, System.out, this);
    }

    /**
     * Constructor for a TacoServer using an existing TacoTransport
     * object.
     *
     * @param xp the transport to be used for communication
     */
    protected TacoServer(TacoTransport xp) {
        this.xp = xp;
    }

    /**
     * Main program method.
     *
     * Constructs a TacoServer object using standard input and standard output,
     * and invokes its run method.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        TacoServer server = new TacoServer(System.in, System.out);

        // Redirect standard output to standard error to prevent called methods
        // from writing into the Taco communication channel.
        System.setOut(System.err);

        try {
            server.run();
        }
        catch (TacoException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Main message processing method.
     *
     * Repeatedly reads messages from the TacoTransport until the end is
     * reached (null is returned).  For each message, the action parameter
     * is used to look for a matching method in this class.  On success,
     * a "result" action is written.  If an exception is caught while handling
     * a message, an "exception" action is written.
     *
     * @throws TacoException on error reading or writing a message
     */
    public void run() throws TacoException {
        while (true) {
            Map<String, Object> message = xp.read();

            if (message == null) {
                break;
            }

            Map<String, Object> response;

            try {
                String action = (String) message.get("action");
                Method m;

                // Check that the method is not one of the non-action methods
                // in this class.  All Taco actions are underscore-separated
                // words whereas the other methods in this class are not.
                if (! action.contains("_")) {
                    throw new TacoException("not an action: " + action);
                }

                // Try to find a handler method for this action.
                try {
                    m = this.getClass().getMethod(action, Map.class);
                }
                catch (NoSuchMethodException e) {
                    throw new TacoException("unknown action: " + action);
                }

                // Finally invoke the action handler method.
                response = new HashMapC()
                        .putc("action", "result")
                        .putc("result", m.invoke(this, message));
            }
            catch (Throwable e) {
                while (e instanceof InvocationTargetException) {
                    e = e.getCause();
                }

                response = new HashMapC()
                    .putc("action", "exception")
                    .putc("message", "exception caught: "
                            + ((e instanceof TacoException)
                                    ? e.getMessage()
                                    : e.toString()));
            }

            xp.write(response);
        }
    }

    /**
     * Handler for the "call_class_method" action.
     */
    public Object call_class_method(Map<String, Object> message)
            throws Exception {
        Class cls = Class.forName((String) message.get("class"));
        List<Object> args = (List<Object>) message.get("args");
        String name = (String) message.get("name");

        if (args == null) {
            return cls.getMethod(name).invoke(null);
        }
        else {
            return invokeMethod(cls, name, null, (Object[]) args.toArray());
        }
    }

    /**
     * Handler for the "call_function" action.
     */
    public Object call_function(Map<String, Object> message)
            throws Exception {
        throw new TacoException("not implemented");
    }

    /**
     * Handler for the "call_method" action.
     */
    public Object call_method(Map<String, Object> message)
            throws Exception {
        Object object = objects.get((Integer) message.get("number"));
        List<Object> args = (List<Object>) message.get("args");
        String name = (String) message.get("name");

        if (args == null) {
            return object.getClass().getMethod(name).invoke(object);
        }
        else {
            return invokeMethod(object.getClass(), name, object,
                    (Object[]) args.toArray());
        }
    }

    /**
     * Handler for the "construct_object" action.
     */
    public Object construct_object(Map<String, Object> message)
            throws Exception {
        Class cls = Class.forName((String) message.get("class"));
        List<Object> args = (List<Object>) message.get("args");

        if (args == null) {
            return cls.getConstructor().newInstance();
        }
        else {
            // Try all the constructors to see if one accepts our signature.
            for (Constructor c: cls.getConstructors()) {
                try {
                    return c.newInstance((Object[]) args.toArray());
                }
                catch (IllegalArgumentException e) {
                    // Signature didn't match: ignore and try the next one.
                }
            }
        }

        throw new TacoException("no matching constructor signature found");
    }

    /**
     * Handler for the "destroy_object" action.
     */
    public Object destroy_object(Map<String, Object> message)
            throws Exception {
        objects.remove((Integer) message.get("number"));
        return null;
    }

    /**
     * Handler for the "get_attribute" action.
     */
    public Object get_attribute(Map<String, Object> message)
            throws Exception {
        Object object = objects.get((Integer) message.get("number"));
        String name = (String) message.get("name");
        return object.getClass().getField(name).get(object);
    }

    /**
     * Handler for the "get_value" action.
     */
    public Object get_value(Map<String, Object> message)
            throws Exception {
        throw new TacoException("not implemented");
    }

    /**
     * Handler for the "import_module" action.
     */
    public Object import_module(Map<String, Object> message)
            throws Exception {
        String name = (String) message.get("name");
        TacoServer.class.getClassLoader().loadClass(name);
        return null;
    }

    /**
     * Handler for the "set_attribute" action.
     */
    public Object set_attribute(Map<String, Object> message)
            throws Exception {
        Object object = objects.get((Integer) message.get("number"));
        String name = (String) message.get("name");
        object.getClass().getField(name).set(object, message.get("value"));
        return null;
    }

    /**
     * Handler for the "set_value" action.
     */
    public Object set_value(Map<String, Object> message)
            throws Exception {
        throw new TacoException("not implemented");
    }

    /**
     * Convert an object to a map for encoding as JSON.
     *
     * Implementation of the <code>TacoTransport.Filter</code> interface.
     *
     * The object is stored in the object cache ({@link #objects})
     * after incrementing the object counter ({@link #objectNum})
     * and a <code>Map</code> corresponding to a Taco object reference
     * special object is returned.
     */
    public Map<String, Object> objectToMap(Object value) throws TacoException {
        int number = ++ objectNum;
        objects.put(number, value);
        return new HashMapC().putc("_Taco_Object_", number);
    }

    /**
     * Convert decoded JSON objects to Java objects.
     *
     * Implementation of the <code>TacoTransport.Filter</code> interface.
     *
     * If the <code>Map</code> is a Taco object reference special object then
     * the corresponding object from the object cache ({@link #objects})
     * is returned. Otherwise the original map is returned.
     */
    public Object mapToObject(Map<String, Object> map) throws TacoException {
        if (map.containsKey("_Taco_Object_")) {
            return objects.get((Integer) map.get("_Taco_Object_"));
        }
        else {
            return map;
        }
    }

    /**
     * Invoke a static ("class") or instance method by name.
     *
     * @param cls class of the object, passed separately in case the
     *     method is static.
     * @param name the method name
     * @param object the object for instance methods, or null for static
     *     methods
     * @param args array of method arguments
     * @throws TacoException if no matching method is found
     * @throws Exeception on failure
     */
    private Object invokeMethod(Class cls, String name, Object object,
            Object[] args) throws Exception {
        // Try all the methods to see if one accepts our signature.
        for (Method m: cls.getMethods()) {
            if (m.getName().equals(name)) {
                try {
                    return m.invoke(object, args);
                }
                catch (IllegalArgumentException e) {
                    // Signature didn't match: ignore and try the next one.
                }
            }
        }

        // If we didn't return a result already, then we didn't find a
        // match.
        throw new TacoException("no matching method name/signature found");
    }
}
