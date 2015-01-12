/*
 * Taco Java dummy transport class.
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

import org.json.JSONArray;
import org.json.JSONObject;

public class DummyTransport extends TacoTransport {
    private JSONObject response;
    private JSONObject message;
    private boolean singleResponse = false;

    public DummyTransport(TacoTransport.Filter filter) {
        super(filter);
    }

    public void setResponse(JSONObject response) {
        setResponse(response, false);
    }

    public void setResponse(JSONObject response, boolean single) {
        this.response = response;
        this.singleResponse = single;
    }

    public JSONObject getMessage() {
        return message;
    }

    @Override
    public Map<String, java.lang.Object> read() throws TacoException {
        JSONObject response = this.response;

        if (singleResponse) {
            this.response = null;
        }

        if (response == null) {
            return null;
        }

        return jsonToMap(response);
    }

    @Override
    public void write(Map<String, java.lang.Object> message)
            throws TacoException{
        this.message = mapToJson(message);
    }
}
