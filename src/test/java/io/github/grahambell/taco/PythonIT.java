/*
 * Taco Java integration test with Python.
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
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PythonIT {
    Taco taco;

    public PythonIT() throws TacoException {
        taco = new Taco("python");
    }

    @Test
    public void testMath() throws TacoException {
        taco.importModule("math", null, null);

        assertEquals(0.0,
                ((Double) taco.callFunction(
                        "math.sin",
                        Arrays.asList(Math.PI),
                        null
                )).doubleValue(),
                1.0E-6);
    }

    @Test
    public void testSys() throws TacoException {
        taco.importModule("sys", null, null);

        boolean exceptionRaised = false;
        try {
            taco.getValue("sys.ps1");
        }
        catch (TacoException e) {
            exceptionRaised = true;
        }
        assertTrue(exceptionRaised);

        taco.setValue("sys.ps1", "!!! ");

        assertEquals("!!! ", (String) taco.getValue("sys.ps1"));

        Object version_info = taco.getValue("sys.version_info");

        assertTrue(version_info instanceof List);

        assertThat(((List<Integer>) version_info).get(0),
                anyOf(equalTo(2), equalTo(3)));
    }

    @Test
    public void testDatetime() throws TacoException {
        taco.importModule("datetime", null, null);

        Taco.Object dt = taco.constructObject("datetime.datetime",
                Arrays.asList(2000, 12, 25), null);

        assertEquals("datetime",
                (String) ((Taco.Object) dt.getAttribute("__class__"))
                        .getAttribute("__name__"));

        assertEquals(new Integer(2000), (Integer) dt.getAttribute("year"));
        assertEquals(new Integer(12), (Integer) dt.getAttribute("month"));
        assertEquals(new Integer(25), (Integer) dt.getAttribute("day"));

        assertEquals("2000-12-25", (String) dt.callMethod("strftime",
                Arrays.asList("%Y-%m-%d"), null));

        Taco.Object dt_d = (Taco.Object) dt.callMethod("date", null, null);
        Taco.Object dt_t = taco.constructObject("datetime.time",
                Arrays.asList(15, 0), null);

        Taco.Object dt_c = (Taco.Object) taco.callClassMethod(
                "datetime.datetime", "combine",
                Arrays.asList(dt_d, dt_t), null);

        Taco.Object dt_cp = (Taco.Object) dt_c.callMethod("replace", null,
                new HashMapC().putc("year", 2010));

        assertEquals("25/12/2010 03:00 PM",
                (String) dt_cp.callMethod("strftime",
                Arrays.asList("%d/%m/%Y %I:%M %p"), null));

        String repr = (String) taco.callFunction("repr",
                Arrays.asList(dt_cp), null);

        assertThat(repr, startsWith("datetime.datetime(2010"));
    }
}
