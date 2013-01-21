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

package pl.nask.hsn2.unicorn;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.nask.hsn2.protobuff.Object.ObjectData;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectRequest.RequestType;
import pl.nask.hsn2.protobuff.ObjectStore.ObjectResponse;

import com.googlecode.protobuf.format.JsonFormat;

public class ObjectsDumper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ObjectsDumper.class);
	private static final String DUMP_DIR = "dump";
	static{
		new File(DUMP_DIR).mkdir();
	}

	public static String dumpToJsonFile(ObjectResponse receiveObjects){
		String fileName = Long.toString(System.currentTimeMillis()) + ".json";
		File dumpFile = new File(DUMP_DIR, fileName);

		OutputStreamWriter output = null;
		try {
			dumpFile.createNewFile();
			output = new OutputStreamWriter(new FileOutputStream(dumpFile));
			for(ObjectData ob : receiveObjects.getDataList())
				JsonFormat.print(ob, output);
			return "Dump file " + fileName;
		} catch (FileNotFoundException e) {
			dumpFile.delete();
			throw new RuntimeException(e);
		} catch (IOException e) {
			dumpFile.delete();
			throw new RuntimeException(e);
		}
		finally{
			checkNotNullThenClose(output);
		}
	}

	public static byte[] getObjectFromJsonFile(String pathName, long jobId) {

		FileReader fileReader = null;
		try {
			fileReader = new FileReader(pathName);
			List<ObjectData> builders = new ArrayList<ObjectData>();
			while(fileReader.ready()){
				ObjectData.Builder builder  = ObjectData.newBuilder();
				JsonFormat.merge(fileReader, builder);
				builders.add(builder.build());

			}
			return ObjectRequest.newBuilder()
				        .setType(RequestType.PUT_RAW)
				        .setJob(jobId)
				        .addAllData(builders)
				        .build().toByteArray();

		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally{
			checkNotNullThenClose(fileReader);
		}
	}

	private static void checkNotNullThenClose(Closeable c){
		if (c != null){
			try {
				c.close();
			} catch (IOException e) {
				LOGGER.error("Can't close stream.");
			}
		}
	}
}
