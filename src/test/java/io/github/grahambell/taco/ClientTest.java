/*
 * Taco Java client test class.
 * Copyright (C) 2015 Graham Bell
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

import java.util.Map;

import org.json.JSONObject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ClientTest extends Taco {
    public ClientTest() {
        super((TacoTransport) null);
        xp = new DummyTransport(this);
    }

    @Test
    public void testBasicClient() throws TacoException {
        DummyTransport xp = (DummyTransport) this.xp;

        xp.setResponse(new JSONObject()
                .put("action", "result")
                .put("result", "some result"));

        callClassMethod("SomeClass", "someMethod", null, null, null);

        assertEquals("call_class_method", xp.getMessage().get("action"));

        callFunction("someFunction", null, null, null);

        assertEquals("call_function", xp.getMessage().get("action"));

        getValue("SomeVariable");

        assertEquals("get_value", xp.getMessage().get("action"));

        importModule("SomeModule", null, null);

        assertEquals("import_module", xp.getMessage().get("action"));

        setValue("SomeValue", null);

        assertEquals("set_value", xp.getMessage().get("action"));
    }

    @Test
    public void testOoClient() throws TacoException {
        DummyTransport xp = (DummyTransport) this.xp;

        xp.setResponse(new JSONObject()
                .put("action", "result")
                .put("result", new JSONObject().put("_Taco_Object_", 58)));

        Object obj = constructObject("SomeClass", null, null);

        assertEquals("construct_object", xp.getMessage().get("action"));

        assertEquals("<Taco object 58>", obj.toString());

        xp.setResponse(new JSONObject()
                .put("action", "result")
                .put("result", JSONObject.NULL));

        obj.callMethod("someMethod", null, null, null);

        assertEquals("call_method", xp.getMessage().get("action"));

        obj.getAttribute("someAttribute");

        assertEquals("get_attribute", xp.getMessage().get("action"));

        obj.setAttribute("someAttribute", "some value");

        assertEquals("set_attribute", xp.getMessage().get("action"));

        obj.finalize();

        assertEquals("destroy_object", xp.getMessage().get("action"));
    }

    private static class DummyTransport extends TacoTransport {
        private JSONObject response;
        private JSONObject message;

        public DummyTransport(TacoTransport.Filter filter) {
            super(filter);
        }

        public void setResponse(JSONObject response) {
            this.response = response;
        }

        public JSONObject getMessage() {
            return message;
        }

        @Override
        public Map<String, java.lang.Object> read() throws TacoException {
            return jsonToMap(response);
        }

        @Override
        public void write(Map<String, java.lang.Object> message)
                throws TacoException{
            this.message = mapToJson(message);
        }
    }
}
