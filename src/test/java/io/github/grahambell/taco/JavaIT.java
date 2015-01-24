/*
 * Taco Java self-integration test.
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

import static org.junit.Assert.assertEquals;

public class JavaIT {
    Taco taco;

    public JavaIT() throws TacoException {
        taco = new Taco("scripts/taco-java", true);
    }

    @Test
    public void testStringBuilder() throws TacoException {
        Taco.Object sb = taco.constructObject("java.lang.StringBuilder",
                null, null);

        sb.callMethod("append", Arrays.asList("a"), null);
        sb.callMethod("append", Arrays.asList("-"), null);
        sb.callMethod("append", Arrays.asList("z"), null);

        assertEquals("a-z", (String) sb.callMethod("toString", null, null));
    }

    @Test
    public void testConvenienceScanner() throws TacoException {
        taco.importModule("java.util.Scanner");
        taco.importModule("java.io.StringReader");

        Taco.Object scanner = taco.constructor("java.util.Scanner").invoke(
            taco.constructor("java.io.StringReader").invoke("1 1 2 3 5 8"));

        Taco.Object.Method nextInt = scanner.method("nextInt");

        assertEquals(1, nextInt.invoke());
        assertEquals(1, nextInt.invoke());
        assertEquals(2, nextInt.invoke());
        assertEquals(3, nextInt.invoke());
        assertEquals(5, nextInt.invoke());
        assertEquals(8, nextInt.invoke());
    }
}
