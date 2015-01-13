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

import java.util.Arrays;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import static io.github.grahambell.taco.JsonMatcher.matchesJson;

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

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_class_method")
                .put("class", "SomeClass")
                .put("name", "someMethod")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", JSONObject.NULL)
        ));

        callClassMethod("SomeOtherClass", "someOtherMethod", null, null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_class_method")
                .put("class", "SomeOtherClass")
                .put("name", "someOtherMethod")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", JSONObject.NULL)
        ));

        callFunction("someFunction", null, null, null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_function")
                .put("name", "someFunction")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", JSONObject.NULL)
        ));

        callFunction("someFunction", null, null, Context.SCALAR);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_function")
                .put("name", "someFunction")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", "scalar")
        ));

        callFunction("someFunction", null, null, Context.LIST);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_function")
                .put("name", "someFunction")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", "list")
        ));

        callFunction("someFunction", null, null, Context.MAP);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_function")
                .put("name", "someFunction")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", "map")
        ));

        callFunction("someFunction", null, null, Context.VOID);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_function")
                .put("name", "someFunction")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", "void")
        ));

        callFunction("someOtherFunction", null, null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_function")
                .put("name", "someOtherFunction")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", JSONObject.NULL)
        ));

        getValue("SomeVariable");

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "get_value")
                .put("name", "SomeVariable")
        ));

        importModule("SomeModule", null, null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "import_module")
                .put("name", "SomeModule")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
        ));

        setValue("SomeValue", null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "set_value")
                .put("name", "SomeValue")
                .put("value", JSONObject.NULL)
        ));
    }

    @Test
    public void testOoClient() throws TacoException {
        DummyTransport xp = (DummyTransport) this.xp;

        xp.setResponse(new JSONObject()
                .put("action", "result")
                .put("result", new JSONObject().put("_Taco_Object_", 58)));

        Object obj = constructObject("SomeClass", null, null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "construct_object")
                .put("class", "SomeClass")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
        ));

        assertEquals("<Taco object 58>", obj.toString());

        xp.setResponse(new JSONObject()
                .put("action", "result")
                .put("result", JSONObject.NULL));

        obj.callMethod("someMethod", null, null, null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_method")
                .put("name", "someMethod")
                .put("number", 58)
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", JSONObject.NULL)
        ));

        obj.callMethod("someOtherMethod", null, null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_method")
                .put("name", "someOtherMethod")
                .put("number", 58)
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL)
                .put("context", JSONObject.NULL)
        ));

        obj.getAttribute("someAttribute");

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "get_attribute")
                .put("name", "someAttribute")
                .put("number", 58)
        ));

        obj.setAttribute("someAttribute", "some value");

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "set_attribute")
                .put("name", "someAttribute")
                .put("number", 58)
                .put("value", "some value")
        ));

        callFunction("someFunction",
                Arrays.asList(obj),
                null);

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "call_function")
                .put("name", "someFunction")
                .put("args", new JSONArray(new java.lang.Object[] {
                        new JSONObject().put("_Taco_Object_", 58)
                        }))
                .put("kwargs", JSONObject.NULL)
                .put("context", JSONObject.NULL)
        ));

        obj.finalize();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "destroy_object")
                .put("number", 58)
        ));
    }
}
