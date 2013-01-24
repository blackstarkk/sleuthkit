/*
 * Sleuth Kit Data Model
 *
 * Copyright 2011 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * Verifies that getParent works as intended.
 */
@RunWith(Parameterized.class)
public class BottomUpTest {
		

	private List<String> imagePaths;

	
	public BottomUpTest(List<String> imagePaths) {
		this.imagePaths = imagePaths;
	}
	/**
	 * Get the sets of filenames for each test image, they should be located in 
	 * the folder specified by the build.xml
	 * @return A Collection of one-element Object arrays, where that one element
	 * is a List<String> containing the image file paths (the type is weird
	 * because that's what JUnit wants for parameters).
	 */
	@Parameterized.Parameters
	public static Collection<Object[]> testImageData() {
		Collection<Object[]> data = new ArrayList<Object[]>();
		
		for (Object imagePaths : DiffUtil.getImagePaths()) {
			data.add(new Object[]{imagePaths});
		}
		return data;
	}
	
	@Test
	public void testBottomUpDiff() {
		try{
			String title = DiffUtil.getImgName(imagePaths.get(0));
			java.io.File dbFile=new java.io.File(DiffUtil.getRsltPath());
			String tempDirPath= dbFile.getAbsolutePath();
			String dbPath = DiffUtil.buildPath(tempDirPath, title, "_BU", ".db");
			dbFile.delete();
			SleuthkitCase sk = SleuthkitCase.newCase(dbPath);
			String timezone = "";
			title = title + DiffUtil.LVS+ ".txt";
			SleuthkitJNI.CaseDbHandle.AddImageProcess process = sk.makeAddImageProcess(timezone, true, false);
			try{
				process.run(imagePaths.toArray(new String[imagePaths.size()]));
			}catch (TskDataException ex){
			}
			process.commit();
			java.io.File lvs = new java.io.File(dbFile.getAbsolutePath()+java.io.File.separator+title);
			Scanner climber = new Scanner(lvs);
			while(climber.hasNextLine())
			{
				String cliNL = climber.nextLine();
				cliNL = cliNL.substring(1);
				String[] ids = cliNL.split("[\\],]\\s?+");
				Content c = sk.getContentById(Integer.parseInt(ids[0]));
				for(int x = 0; x<ids.length; x++)
				{
					assertEquals("Got ID " + c.getId() + " should have gotten ID " + ids[x], ids[x].equals(((Long)c.getId()).toString()), true);
					c = c.getParent();
				}
			}
		} catch (Exception ex)
		{
			System.out.println(ex.toString());
			fail("Failed to run BottomUp test");
		}
	}
}
