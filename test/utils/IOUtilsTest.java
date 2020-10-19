/** Copyright (C) 2014 - Anas H. Sulaiman (ahs.pw)
* 			All Rights Reserved.
*/

package utils;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Author: Anas H. Sulaiman 
 */
public class IOUtilsTest {

    @Test
    public void testGetFileName() {
        IOUtils io = IOUtils.getInstance();
        assertEquals("test", io.getFileName("test/test/test.lol"));
        assertEquals("test", io.getFileName("test/test/test"));
        assertEquals("test.lol1", io.getFileName("test/test/test.lol1.lol2"));
        assertEquals("test.lol1.lol2", io.getFileName("test/test/test.lol1.lol2.lol3"));
        assertEquals("test", io.getFileName("test"));
        assertEquals("test", io.getFileName("test.lol"));
        assertEquals("test.lol1", io.getFileName("test.lol1.lol2"));
        assertEquals("test.lol1.lol2", io.getFileName("test.lol1.lol2.lol3"));
        assertEquals("", io.getFileName(""));
        assertEquals("", io.getFileName("/"));
        assertEquals("", io.getFileName("./"));
    }

    @Test
    public void testGetFileExt() {
        IOUtils io = IOUtils.getInstance();
        assertEquals("lol", io.getFileExt("test/test/test.lol"));
        assertEquals("", io.getFileExt("test/test/test"));
        assertEquals("lol2", io.getFileExt("test/test/test.lol1.lol2"));
        assertEquals("lol3", io.getFileExt("test/test/test.lol1.lol2.lol3"));
        assertEquals("", io.getFileExt("test"));
        assertEquals("lol", io.getFileExt("test.lol"));
        assertEquals("lol2", io.getFileExt("test.lol1.lol2"));
        assertEquals("lol3", io.getFileExt("test.lol1.lol2.lol3"));
        assertEquals("", io.getFileName(""));
        assertEquals("", io.getFileName("/"));
        assertEquals("", io.getFileName("./"));
    }

    @Test
    public void testGetFileParentNames() {
        IOUtils io = IOUtils.getInstance();
        List<String> names;

        names = io.getFileParentNames("test");
        assertTrue(names.isEmpty());

        names = io.getFileParentNames("/test");
        assertTrue(names.isEmpty());

        names = io.getFileParentNames("/test/");
        assertTrue(names.isEmpty());

        names = io.getFileParentNames("");
        assertTrue(names.isEmpty());

        names = io.getFileParentNames(".");
        assertTrue(names.isEmpty());

        names = io.getFileParentNames("./");
        assertTrue(names.isEmpty());

        names = io.getFileParentNames("test/test");
        assertEquals(1, names.size());

        names = io.getFileParentNames("test1/test2/test3");
        assertEquals(2, names.size());

        names = io.getFileParentNames("test1/test2/test3/test4.lol");
        assertEquals(3, names.size());
    }

	// ignored
    public void testCreateHiddenDir() throws Exception {
        String userHome = System.getProperty("user.home");
	    Path p = Paths.get(userHome + File.separator + "test");
	    String createdFile = IOUtils.getInstance().createHiddenDir(p.toString());
	    Path p2 = Paths.get(createdFile);
	    assertTrue(Files.exists(p2, LinkOption.NOFOLLOW_LINKS));
	    assertTrue(p2.getFileName().toString().startsWith("."));
	    Files.isHidden(p2);

	    Files.deleteIfExists(p2);
    }
}
