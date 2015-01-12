/*
 * Taco Java integration test with Perl.
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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PerlIT {
    Taco taco;

    public PerlIT() throws TacoException {
        taco = new Taco("perl");
    }

    @Test
    public void testDatetime() throws TacoException {
        taco.importModule("DateTime", null, null);

        Taco.Object dt = taco.constructObject("DateTime", null,
                new HashMapC().putc("year", 2000)
                        .putc("month", 4).putc("day", 1));

        assertEquals("2000/04/01", (String) dt.callMethod("ymd",
                Arrays.asList(new String[] {"/"}), null));

        taco.importModule("Data::Dumper", null, null);

        assertThat((String) taco.callFunction("Dumper",
                Arrays.asList(new Object[] {dt}), null),
                not(containsString("_taco_test_attr")));

        dt.setAttribute("_taco_test_attr", 12345);

        assertThat((String) taco.callFunction("Dumper",
                Arrays.asList(new Object[] {dt}), null),
                containsString("_taco_test_attr"));

        assertEquals(new Integer(12345),
                (Integer) dt.getAttribute("_taco_test_attr"));

        taco.importModule("DateTime::Duration", null, null);

        Taco.Object dur = taco.constructObject("DateTime::Duration", null,
                new HashMapC().putc("days", 3));

        dt.callMethod("add_duration", Arrays.asList(new Object[] {dur}), null);

        assertEquals("04-04-2000", (String) dt.callMethod("strftime",
                Arrays.asList(new String[] {"%d-%m-%Y"}), null));

        dt = (Taco.Object) taco.callClassMethod("DateTime", "from_epoch",
                null, new HashMapC().putc("epoch", 15));

        assertEquals("1970-01-01T00:00:15", (String) dt.callMethod("datetime",
                null, null));
    }
}
