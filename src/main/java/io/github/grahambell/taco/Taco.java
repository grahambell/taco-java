/*
 * Taco Java client class.
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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class Taco implements TacoTransport.Filter {
    protected TacoTransport xp;

    public Taco(String lang) throws TacoException {
        ProcessBuilder pb = new ProcessBuilder("taco-" + lang);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        try {
            Process p = pb.start();

            xp = new TacoTransport(p.getInputStream(), p.getOutputStream(),
                                   this);
        }
        catch (IOException e) {
            throw new TacoException("start error: " + e.getMessage(), e);
        }
    }

    protected java.lang.Object interact(Map<String, java.lang.Object> message)
            throws TacoException {
        xp.write(message);

        Map<String, java.lang.Object> response = xp.read();

        String action = (String) response.get("action");

        if ("result".equals(action)) {
            return response.get("result");
        }
        else if ("exception".equals(action)) {
            throw new TacoException("received exception: "
                    + response.get("message"));
        }
        else {
            throw new TacoException("received unknown action: " + action);
        }
    }

    public java.lang.Object callClassMethod(String className, String name,
            Collection<?> args, Map<String, ?> kwargs,
            Context context)
            throws TacoException {
        return interact(new HashMapC()
                .putc("action", "call_class_method")
                .putc("class", className)
                .putc("name", name)
                .putc("args", args)
                .putc("kwargs", kwargs)
                .putc("context", context == null ? null : context.getName()));
    }

    public java.lang.Object callFunction(String name,
            Collection<?> args, Map<String, ?> kwargs,
            Context context)
            throws TacoException {
        return interact(new HashMapC()
                .putc("action", "call_function")
                .putc("name", name)
                .putc("args", args)
                .putc("kwargs", kwargs)
                .putc("context", context == null ? null : context.getName()));
    }

    private java.lang.Object callMethod(int number, String name,
            Collection<?> args, Map<String, ?> kwargs,
            Context context)
            throws TacoException {
        return interact(new HashMapC()
                .putc("action", "call_method")
                .putc("number", new Integer(number))
                .putc("name", name)
                .putc("args", args)
                .putc("kwargs", kwargs)
                .putc("context", context == null ? null : context.getName()));
    }

    public Object constructObject(String className,
            Collection<?> args, Map<String, ?> kwargs)
            throws TacoException {
        return (Object) interact(new HashMapC()
                .putc("action", "construct_object")
                .putc("class", className)
                .putc("args", args)
                .putc("kwargs", kwargs));
    }

    private void destroyObject(int number)
            throws TacoException {
        interact(new HashMapC()
                .putc("action", "destroy_object")
                .putc("number", new Integer(number)));
    }

    private java.lang.Object getAttribute(int number, String name)
            throws TacoException {
        return interact(new HashMapC()
                .putc("action", "get_attribute")
                .putc("number", new Integer(number))
                .putc("name", name));
    }

    public java.lang.Object getValue(String name)
            throws TacoException {
        return interact(new HashMapC()
                .putc("action", "get_value")
                .putc("name", name));
    }

    public void importModule(String name,
            Collection<?> args, Map<String, ?> kwargs)
            throws TacoException {
        interact(new HashMapC()
                .putc("action", "import_module")
                .putc("name", name)
                .putc("args", args)
                .putc("kwargs", kwargs));
    }

    private void setAttribute(int number, String name, java.lang.Object value)
            throws TacoException {
        interact(new HashMapC()
                .putc("action", "set_attribute")
                .putc("number", new Integer(number))
                .putc("name", name)
                .putc("value", value));
    }

    public void setValue(String name, java.lang.Object value)
            throws TacoException {
        interact(new HashMapC()
                .putc("action", "set_value")
                .putc("name", name)
                .putc("value", value));
    }

    public Map<String, java.lang.Object> objectToMap(java.lang.Object value)
            throws TacoException {
        if (value instanceof Taco.Object) {
            return new HashMapC()
                    .putc("_Taco_Object_", ((Object) value).number);
        }
        else {
            throw new TacoException("unknown object type to turn to JSON");
        }
    }

    public java.lang.Object mapToObject(Map<String, java.lang.Object> map)
            throws TacoException {
        if (map.containsKey("_Taco_Object_")) {
            return new Object((Integer) map.get("_Taco_Object_"));
        }
        else {
            return map;
        }
    }

    /**
     * Class for objects which refer to an object cached by the Taco server.
     */
    public class Object {
        private final int number;

        private Object(int number) {
            this.number = number;
        }

        @Override
        protected void finalize() throws TacoException {
            destroyObject(number);
        }

        @Override
        public String toString() {
            return "<Taco object " + Integer.toString(number) + ">";
        }

        public java.lang.Object callMethod(String name,
                Collection<?> args, Map<String, ?> kwargs,
                Context context)
                throws TacoException {
            return Taco.this.callMethod(number, name, args, kwargs, context);
        }

        public java.lang.Object getAttribute(String name)
                throws TacoException {
            return Taco.this.getAttribute(number, name);
        }

        public void setAttribute(String name, java.lang.Object value)
                throws TacoException {
            Taco.this.setAttribute(number, name, value);
        }
    }

    public static enum Context {
        SCALAR ("scalar"),
        LIST ("list"),
        MAP ("map"),
        VOID ("void");

        private final String name;

        private Context(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
