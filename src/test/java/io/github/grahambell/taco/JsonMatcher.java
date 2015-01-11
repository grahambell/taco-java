/*
 * JSON matcher class.
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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONObject;

/**
 * Matcher class for comparing JSON objects.
 */
public class JsonMatcher extends TypeSafeMatcher<JSONObject> {
    /**
     * The expected JSON object.
     */
    private final JSONObject expected;

    /**
     * Constructor.
     */
    public JsonMatcher(JSONObject expected) {
        this.expected = expected;
    }

    /**
     * Construct a JsonMatcher.
     */
    public static Matcher<JSONObject> matchesJson(JSONObject expected) {
        return new JsonMatcher(expected);
    }

    @Override
    public boolean matchesSafely(JSONObject item) {
        return expected.similar(item);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(expected.toString());
    }
}
