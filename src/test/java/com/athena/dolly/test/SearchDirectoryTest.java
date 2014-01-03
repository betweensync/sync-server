package com.athena.dolly.test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;

public class SearchDirectoryTest {

	@Test
	public void scan() throws IOException {
		File directory = new File("C:/Private");
		String [] extensions = {};
		boolean recursive = true;
		//Collection<File> files = FileUtils.listFiles(directory, null, recursive);
		//Collection<File> files = FileUtils.listFilesAndDirs(directory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
		Collection<File> files = FileUtils.listFiles(directory, TrueFileFilter.TRUE, TrueFileFilter.TRUE);
		
		for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();
            if( file.isDirectory() ) {
            	System.out.println("=== Directory [" + file.getPath() + "] ===");
            }
            System.out.println("Name = " + file.getCanonicalPath());
        }
	}
}
