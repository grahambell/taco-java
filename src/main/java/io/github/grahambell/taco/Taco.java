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
import java.util.Arrays;
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
        this(lang, false);
    }

    /**
     * Construct Taco client given either the language or script path.
     *
     * @param langOrScript language (for automatic script name determination)
     *     or script (with path if not in the executable search path)
     * @param byScriptPath true if a script path is being provided, inhibits
     *     construction of script name "taco-LANGUAGE".
     */
    public Taco(String langOrScript, boolean byScriptPath)
            throws TacoException {
        ProcessBuilder pb = new ProcessBuilder(
                byScriptPath ? langOrScript : ("taco-" + langOrScript));
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
     * Constructor for a Taco client using an existing TacoTransport
     * object.
     *
     * @param xp the transport to be used for communication
     */
    protected Taco(TacoTransport xp) {
        this.xp = xp;
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
     * Invoke a (static) class method call within the associated Taco server
     * script.  No context is specified.
     *
     * @param className the name of the class
     * @param name the name of the method
     * @param args positional arguments
     * @param kwargs keyword arguments
     * @return the result of the method call
     * @throws TacoException on error
     */
    public java.lang.Object callClassMethod(String className, String name,
            Collection<?> args, Map<String, ?> kwargs)
            throws TacoException {
        return callClassMethod(className, name, args, kwargs, null);
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

    /**
     * Invoke a function call within the associated Taco server script.
     * No context is specified.
     *
     * @param name the name of the function
     * @param args positional arguments
     * @param kwargs keyword arguments
     * @return the result of the function call
     * @throws TacoException on error
     */
    public java.lang.Object callFunction(String name,
            Collection<?> args, Map<String, ?> kwargs)
            throws TacoException {
        return callFunction(name, args, kwargs, null);
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

    /**
     * Get the value of a class (static) attribute.
     *
     * @param className the name of the class
     * @param name the name of the attribute
     * @return the value of the attribute
     * @throws TacoException on error
     */
    public java.lang.Object getClassAttribute(String className, String name)
            throws TacoException {
        return interact(new HashMapC()
                .putc("action", "get_class_attribute")
                .putc("class", className)
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

    /**
     * Instruct the server to import the given module.
     *
     * @param name the name of the module
     * @param args positional arguments
     * @throws TacoException on error
     */
    public void importModule(String name, Collection<?> args)
            throws TacoException {
        importModule(name, args, null);
    }

    /**
     * Instruct the server to import the given module.
     *
     * @param name the name of the module
     * @throws TacoException on error
     */
    public void importModule(String name) throws TacoException {
        importModule(name, null, null);
    }

    /**
     * Set the value of a class (static) attribute.
     *
     * @param className the name of the class
     * @param name the name of the attribute
     * @param value the new value for the attribute
     * @throws TacoException on error
     */
    public void setClassAttribute(String className, String name,
            java.lang.Object value)
            throws TacoException {
        interact(new HashMapC()
                .putc("action", "set_class_attribute")
                .putc("class", className)
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
     * Create a new convenience {@link Invocable} object constructor.
     *
     * @param className the name of the object class to construct
     * @return constructor
     */
    public Constructor constructor(String className) {
        return new Constructor(className);
    }

    /**
     * Create a new convenience function {@link Invocable}.
     *
     * @param name the name of the function
     * @param context context in which to invoke the function
     * @return function
     */
    public Function function(String name, Context context) {
        return new Function(name, context);
    }

    /**
     * Create a new convenience function {@link Invocable}.
     * No context is specified.
     *
     * @param name the name of the function
     * @return function
     */
    public Function function(String name) {
        return new Function(name, null);
    }

    /**
     * Interface for invocable convenience routines.
     *
     * This interface is for objects which allow a given Taco action
     * to be invoked in a more convenient manner.
     */
    public interface Invocable {
        /**
         * Invoke the associated action.
         */
        public java.lang.Object invoke(java.lang.Object... args)
                throws TacoException;
    }

    /**
     * Object to conveniently invoke an object constructor.
     */
    public class Constructor implements Invocable {
        /**
         * Name of the class of object to construct.
         */
        private final String className;

        /**
         * Constructor.
         *
         * @param className the name of the class of object to construct
         */
        private Constructor(String className) {
            this.className = className;
        }

        /**
         * Invoke the object constructor.
         *
         * This method does not allow keyword arguments to be specified.
         *
         * @param args positional arguments for the constructor
         * @return a reference to the newly constructed object
         * @throws TacoException on error.
         */
        public Object invoke(java.lang.Object... args) throws TacoException {
            return constructObject(className, Arrays.asList(args), null);
        }
    }

    /**
     * Object to conveniently invoke a function.
     */
    public class Function implements Invocable {
        /**
         * Name of the function to call.
         */
        private final String name;

        /**
         * Context in which to call the function.
         */
        private final Context context;

        /**
         * Constructor.
         *
         * @param name the name of the function to call
         * @param context the context in which to call the function
         */
        private Function(String name, Context context) {
            this.name = name;
            this.context = context;
        }

        /**
         * Invoke the function.
         *
         * This method does not allow keyword arguments to be specified.
         *
         * @param args positional arguments for the function
         * @return the result of the function call
         * @throws TacoException on error
         */
        public java.lang.Object invoke(java.lang.Object... args)
                throws TacoException {
            return callFunction(name, Arrays.asList(args), null, context);
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
            interact(new HashMapC()
                    .putc("action", "destroy_object")
                    .putc("number", new Integer(number)));
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
            return interact(new HashMapC()
                    .putc("action", "call_method")
                    .putc("number", new Integer(number))
                    .putc("name", name)
                    .putc("args", args)
                    .putc("kwargs", kwargs)
                    .putc("context", context == null
                            ? null : context.getName()));
        }

        /**
         * Invoke a method on the corresponding object in the server's cache.
         * No context is specified.
         *
         * @param name method name
         * @param args positional arguments
         * @param kwargs keyword arguments
         * @return the value returned by the method
         * @throws TacoException on error
         */
        public java.lang.Object callMethod(String name,
                Collection<?> args, Map<String, ?> kwargs)
                throws TacoException {
            return callMethod(name, args, kwargs, null);
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
            return interact(new HashMapC()
                    .putc("action", "get_attribute")
                    .putc("number", new Integer(number))
                    .putc("name", name));
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
            interact(new HashMapC()
                    .putc("action", "set_attribute")
                    .putc("number", new Integer(number))
                    .putc("name", name)
                    .putc("value", value));
        }

        /**
         * Create a new convenience method {@link Invocable}.
         *
         * @param name the name of the method
         * @param context context in which to invoke the method
         * @return method
         */
        public Method method(String name, Context context) {
            return new Method(name, context);
        }

        /**
         * Create a new convenience method {@link Invocable}.
         * No context is specified.
         *
         * @param name the name of the method
         * @return method
         */
        public Method method(String name) {
            return new Method(name, null);
        }

        /**
         * Object to conveniently invoke an object method.
         */
        public class Method implements Invocable {
            /**
             * Name of the method to call.
             */
            private final String name;

            /**
             * Context in which to call the method.
             */
            private final Context context;

            /**
             * Constructor.
             *
             * @param name the name of the method
             * @param context the context in which to call the method
             */
            private Method(String name, Context context) {
                this.name = name;
                this.context = context;
            }

            /**
             * Invoke the method.
             *
             * This method does not allow keyword arguments to be specified.
             *
             * @param args positional arguments for the method
             * @return the result of the method call
             * @throws TacoException on error
             */
            public java.lang.Object invoke(java.lang.Object... args)
                    throws TacoException {
                return callMethod(name, Arrays.asList(args), null, context);
            }
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
