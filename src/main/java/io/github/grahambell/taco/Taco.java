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

/**
 * Taco client class.
 */
public class Taco implements TacoTransport.Filter {
    /**
     * TacoTransport object used for communication.
     */
    protected TacoTransport xp;

    /**
     * Construct Taco client by launching the Taco server script for
     * the given language in a subprocess.
     *
     * The script is expected to be called "taco-LANGUAGE" where "LANGUAGE"
     * is the requested language.  This script must be in the executable
     * search path.
     *
     * @param lang name of language for which to launch a Taco server script
     */
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

    /**
     * Perform an interaction with the Taco server.
     *
     * @param message the message to send to the Taco server
     * @return the result included in any "result" action received
     * @throws TacoException on error reading or writing, if an unknown action
     *     is received, or if an "exception" action is received
     */
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

    /**
     * Invoke a (static) class method call within the associated Taco server
     * script.
     *
     * @param className the name of the class
     * @param name the name of the method
     * @param args positional arguments
     * @param kwargs keyword arguments
     * @param context context in which to invoke the function
     * @return the result of the method call
     * @throws TacoException on error
     */
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

    /**
     * Invoke a function call within the associated Taco server script.
     *
     * @param name the name of the function
     * @param args positional arguments
     * @param kwargs keyword arguments
     * @param context context in which to invoke the function
     * @return the result of the function call
     * @throws TacoException on error
     */
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

    /**
     * Invoke an object constructor.
     *
     * @param className the name of the object class
     * @param args positional arguments
     * @param kwargs keyword arguments
     * @return a reference to the newly constructed object
     * @throws TacoException on error.
     */
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

    /**
     * Get the value of the given variable.
     *
     * @param name the name of the variable
     * @return the value of the variable
     * @throws TacoException on error
     */
    public java.lang.Object getValue(String name)
            throws TacoException {
        return interact(new HashMapC()
                .putc("action", "get_value")
                .putc("name", name));
    }

    /**
     * Instruct the server to import the given module.
     *
     * @param name the name of the module
     * @param args positional arguments
     * @param kwargs keyword arguments
     * @throws TacoException on error
     */
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

    /**
     * Set the value of the given variable.
     *
     * @param name the name of the variable
     * @param value the new value for the variable
     * @throws TacoException on error
     */
    public void setValue(String name, java.lang.Object value)
            throws TacoException {
        interact(new HashMapC()
                .putc("action", "set_value")
                .putc("name", name)
                .putc("value", value));
    }

    /**
     * Convert objects to a map suitable for conversion to JSON.
     *
     * Implementation of the <code>TacoTransport.Filter</code> interface.
     *
     * If the object is a <code>Taco.Object</code> instance, then an
     * object reference special object is returned.
     *
     * @param value the object to attempt to convert
     * @return a map for conversion to JSON
     * @throws TacoException if any other kind of object is encountered
     */
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

    /**
     * Convert a decoded JSON object to a Java object.
     *
     * Implementation of the <code>TacoTransport.Filter</code> interface.
     *
     * If the map is an object reference special object then a corresponding
     * <code>Taco.Object</code> instance is returned.  Otherwise the
     * original map is returned.
     *
     * @param map the decoded JSON object
     * @return a Java object
     */
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
        /**
         * The number identifying the object in the server's cache.
         */
        private final int number;

        /**
         * Constructor.
         */
        private Object(int number) {
            this.number = number;
        }

        /**
         * Destructor.
         *
         * Instructs the server to remove this object from its cache.
         */
        @Override
        protected void finalize() throws TacoException {
            destroyObject(number);
        }

        /**
         * Get a string representation of this object.
         *
         * This includes the object number.
         */
        @Override
        public String toString() {
            return "<Taco object " + Integer.toString(number) + ">";
        }

        /**
         * Invoke a method on the corresponding object in the server's cache.
         *
         * @param name method name
         * @param args positional arguments
         * @param kwargs keyword arguments
         * @param context context in which to invoke the method
         * @return the value returned by the method
         * @throws TacoException on error
         */
        public java.lang.Object callMethod(String name,
                Collection<?> args, Map<String, ?> kwargs,
                Context context)
                throws TacoException {
            return Taco.this.callMethod(number, name, args, kwargs, context);
        }

        /**
         * Get the value of an attribute of the corresponding object in the
         * server's cache.
         *
         * @param name the name of the attribute
         * @return the value of the attribute
         * @throws TacoException on error
         */
        public java.lang.Object getAttribute(String name)
                throws TacoException {
            return Taco.this.getAttribute(number, name);
        }

        /**
         * Set the value of an attribute of the corresponding object in the
         * server's cache.
         *
         * @param name the name of the attribute
         * @param value the new value for the attribute
         * @throws TacoException on error
         */
        public void setAttribute(String name, java.lang.Object value)
                throws TacoException {
            Taco.this.setAttribute(number, name, value);
        }
    }

    /**
     * Enumeration of allowed Taco context parameters.
     */
    public static enum Context {
        SCALAR ("scalar"),
        LIST ("list"),
        MAP ("map"),
        VOID ("void");

        private final String name;

        private Context(String name) {
            this.name = name;
        }

        /**
         * Get the name for this context.
         *
         * @return the name (as used in the Taco protocol) for this context
         */
        public String getName() {
            return name;
        }
    }
}
