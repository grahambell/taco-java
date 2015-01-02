/*
 * Taco Java transport test class.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for TacoTransport.
 */
public class TransportTest {
    @Test
    public void testReadWrite()
            throws UnsupportedEncodingException, TacoException {
        String input = "{\"action\":\"test\"}\n// END\n";

        ByteArrayInputStream inStream =
                new ByteArrayInputStream( input.getBytes("UTF-8"));
        ByteArrayOutputStream outStream =
                new ByteArrayOutputStream();

        TacoTransport xp = new TacoTransport(inStream, outStream, null);

        Map<String, Object> map = xp.read();

        assertEquals("test", (String) map.get("action"));

        xp.write(new HashMapC<String, Object>().putc("action", "response"));

        String output = outStream.toString("UTF-8");

        assertEquals("{\"action\":\"response\"}\n// END\n", output);
    }
}
