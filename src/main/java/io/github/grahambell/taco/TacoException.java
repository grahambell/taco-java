/*
 * Taco Java exception class.
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

/**
 * Class of exceptions thrown by the Taco system.
 */
public class TacoException extends Exception {
    /**
     * Constructor to be used when a source throwable has caused this
     * exception in the Taco system.
     *
     * @param message the error message
     * @param cause the original exception
     */
    public TacoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor to be used without a source throwable.
     *
     * @param message the error message
     */
    public TacoException(String message) {
        super(message);
    }
}
