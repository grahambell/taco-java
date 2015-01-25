/*
 * Taco Java server test class.
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

import java.text.DateFormat;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import static io.github.grahambell.taco.JsonMatcher.matchesJson;

public class ServerTest extends TacoServer {
    public ServerTest() {
        super((TacoTransport) null);
        xp = new DummyTransport(this);
    }

    @Test
    public void testServerViaXP() throws TacoException {
        DummyTransport xp = (DummyTransport) this.xp;

        // We should be starting with an empty object cache.
        assertEquals(objects.size(), 0);
        assertEquals(objectNum, 0);

        // Try to construct a Date object.
        xp.setResponse(new JSONObject()
                .put("action", "construct_object")
                .put("class", "java.util.Date")
                .put("args", new JSONArray(new int[] {115, 3, 1}))
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", new JSONObject().put("_Taco_Object_", 1))
        ));

        assertEquals(objects.size(), 1);
        assertEquals(objectNum, 1);
        assertTrue(objects.get(1) instanceof java.util.Date);

        // Try the Data object's methods.
        xp.setResponse(new JSONObject()
                .put("action", "call_method")
                .put("name", "getYear")
                .put("number", 1)
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", 115)
        ));

        xp.setResponse(new JSONObject()
                .put("action", "call_method")
                .put("name", "getMonth")
                .put("number", 1)
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", 3)
        ));

        xp.setResponse(new JSONObject()
                .put("action", "call_method")
                .put("name", "getDate")
                .put("number", 1)
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", 1)
        ));

        // Construct a Locale object.
        xp.setResponse(new JSONObject()
                .put("action", "construct_object")
                .put("class", "java.util.Locale")
                .put("args", new JSONArray(new String[] {"en", "US"}))
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", new JSONObject().put("_Taco_Object_", 2))
        ));

        assertEquals(objects.size(), 2);
        assertEquals(objectNum, 2);
        assertTrue(objects.get(2) instanceof java.util.Locale);

        // Get a DateFormat object.
        xp.setResponse(new JSONObject()
                .put("action", "call_class_method")
                .put("class", "java.text.DateFormat")
                .put("name", "getDateInstance")
                .put("args", new JSONArray(new Object[] {
                        new Integer(DateFormat.SHORT),
                        new JSONObject().put("_Taco_Object_", 2)
                }))
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", new JSONObject().put("_Taco_Object_", 3))
        ));

        assertEquals(objects.size(), 3);
        assertEquals(objectNum, 3);
        assertTrue(objects.get(3) instanceof java.text.DateFormat);

        // Finally, format the date.
        xp.setResponse(new JSONObject()
                .put("action", "call_method")
                .put("number", 3)
                .put("name", "format")
                .put("args", new JSONArray(new Object[] {
                        new JSONObject().put("_Taco_Object_", 1)
                }))
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", "4/1/15")
        ));

        // Destroy the objects.
        xp.setResponse(new JSONObject()
                .put("action", "destroy_object")
                .put("number", 3),
                true);

        run();

        assertEquals(objects.size(), 2);

        xp.setResponse(new JSONObject()
                .put("action", "destroy_object")
                .put("number", 2),
                true);

        run();

        assertEquals(objects.size(), 1);
        assertTrue(objects.get(1) instanceof java.util.Date);

        xp.setResponse(new JSONObject()
                .put("action", "destroy_object")
                .put("number", 1),
                true);

        run();

        assertEquals(objects.size(), 0);

        // Counter should not have been reset.
        assertEquals(objectNum, 3);

        xp.setResponse(new JSONObject()
                .put("action", "construct_object")
                .put("class", "java.util.Date")
                .put("args", JSONObject.NULL)
                .put("kwargs", JSONObject.NULL),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", new JSONObject().put("_Taco_Object_", 4))
        ));

        assertEquals(objectNum, 4);
        assertEquals(objects.size(), 1);

        // Test class attribute actions.
        xp.setResponse(new JSONObject()
                .put("action", "get_class_attribute")
                .put("class", "io.github.grahambell.taco.ExampleClass")
                .put("name", "attr_one"),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", 5678)
        ));

        xp.setResponse(new JSONObject()
                .put("action", "set_class_attribute")
                .put("class", "io.github.grahambell.taco.ExampleClass")
                .put("name", "attr_one")
                .put("value", 8765),
                true);

        run();

        assertThat(xp.getMessage(), matchesJson(new JSONObject()
                .put("action", "result")
                .put("result", JSONObject.NULL)
        ));

        assertEquals(8765, ExampleClass.attr_one);
    }
}
