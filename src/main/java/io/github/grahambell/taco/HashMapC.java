/*
 * Chainable HashMap class.
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

import java.util.HashMap;

/**
 * Chainable HashMap class.
 *
 * This class extends <code>HashMap</code> to add a chainable version of the
 * <code>put</code> method called <code>putc</code>.
 */
public class HashMapC<K, V> extends HashMap<K, V> {
    /**
     * Put an entry into the <code>Map</code> and return a reference
     * to the map itself.
     *
     * @param key the key for the new entry
     * @param value the value for the new entry
     * @return a reference to this object
     */
    public HashMapC<K, V> putc(K key, V value) {
        put(key, value);
        return this;
    }
}
