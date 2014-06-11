/*
 * Copyright (c) NASK, NCSC
 * 
 * This file is part of HoneySpider Network 2.0.
 * 
 * This is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.nask.hsn2.unicorn.connector;

import java.util.List;

import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse.ResponseType;
import pl.nask.hsn2.unicorn.FailedCommandException;

import com.google.protobuf.InvalidProtocolBufferException;

public class OSResponse extends Response {
	private ObjectResponse objectResponse;

	private OSResponse(String type, String contentType, byte[] body) {
		super(type, contentType, body);
	}

	public OSResponse(Response response) throws InvalidProtocolBufferException {
		super(response.type, response.contentType, response.body);
		objectResponse = ObjectResponse.parseFrom(getBody());
	}

	public List<Long> getObjects() throws FailedCommandException {
		checkNotFailure();
		return objectResponse.getObjectsList();
	}

	public ObjectResponse getObjectResponse() {

		return objectResponse;
	}

	private void checkNotFailure() throws FailedCommandException {
		if (ResponseType.FAILURE.equals(objectResponse.getType())) {
			throw new FailedCommandException("Responses type is FAILURE. " + objectResponse.getError());
		}
	}
}
