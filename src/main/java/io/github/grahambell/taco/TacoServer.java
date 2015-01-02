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
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

class TacoServer implements TacoTransport.Filter {
    protected TacoTransport xp;
    protected Map<Integer, Object> objects = new HashMap();
    protected int objectNum = 0;

    public TacoServer(InputStream in, OutputStream out) {
        xp = new TacoTransport(System.in, System.out, this);
    }

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

    public Object call_class_method(Map<String, Object> message)
            throws Exception {
        Class cls = Class.forName((String) message.get("class"));
        Collection<Object> args = (Collection<Object>) message.get("args");
        String name = (String) message.get("name");

        if (args == null) {
            return cls.getMethod(name).invoke(null);
        }
        else {
            return cls.getMethod(name, typeArray(args))
                    .invoke(null, (Object[]) args.toArray());
        }
    }

    public Object call_function(Map<String, Object> message)
            throws Exception {
        throw new TacoException("not implemented");
    }

    public Object call_method(Map<String, Object> message)
            throws Exception {
        Object object = objects.get((Integer) message.get("number"));
        Collection<Object> args = (Collection<Object>) message.get("args");
        String name = (String) message.get("name");

        if (args == null) {
            return object.getClass().getMethod(name).invoke(object);
        }
        else {
            return object.getClass()
                    .getMethod(name, typeArray(args))
                    .invoke(object, (Object[]) args.toArray());
        }
    }

    public Object construct_object(Map<String, Object> message)
            throws Exception {
        Class cls = Class.forName((String) message.get("class"));
        Collection<Object> args = (Collection<Object>) message.get("args");

        if (args == null) {
            return cls.getConstructor().newInstance();
        }
        else {
            return cls.getConstructor(typeArray(args))
                    .newInstance((Object[]) args.toArray());
        }
    }

    public Object destroy_object(Map<String, Object> message)
            throws Exception {
        objects.remove((Integer) message.get("number"));
        return null;
    }

    public Object get_attribute(Map<String, Object> message)
            throws Exception {
        Object object = objects.get((Integer) message.get("number"));
        String name = (String) message.get("name");
        return object.getClass().getField(name).get(object);
    }

    public Object get_value(Map<String, Object> message)
            throws Exception {
        throw new TacoException("not implemented");
    }

    public Object import_module(Map<String, Object> message)
            throws Exception {
        String name = (String) message.get("name");
        TacoServer.class.getClassLoader().loadClass(name);
        return null;
    }

    public Object set_attribute(Map<String, Object> message)
            throws Exception {
        Object object = objects.get((Integer) message.get("number"));
        String name = (String) message.get("name");
        object.getClass().getField(name).set(object, message.get("value"));
        return null;
    }

    public Object set_value(Map<String, Object> message)
            throws Exception {
        throw new TacoException("not implemented");
    }

    public Map<String, Object> objectToMap(Object value) throws TacoException {
        int number = ++ objectNum;
        objects.put(number, value);
        return new HashMapC().putc("_Taco_Object_", number);
    }

    public Object mapToObject(Map<String, Object> map) throws TacoException {
        if (map.containsKey("_Taco_Object_")) {
            return objects.get((Integer) map.get("_Taco_Object_"));
        }
        else {
            return map;
        }
    }

    public static Class[] typeArray(Collection<Object> args) {
        Class[] types = new Class[args.size()];
        int i = 0;

        for (Object value: args) {
            Class type;

            // TODO: also handle long?  The JSON library will return int
            // if the value fits, otherwise long...
            if (value instanceof Integer) {
                type = int.class;
            }
            else if (value instanceof Double) {
                type = double.class;
            }
            else if (value instanceof Boolean) {
                type = boolean.class;
            }
            else {
                type = value.getClass();
            }

            types[i++] = type;
        }

        return types;
    }
}
