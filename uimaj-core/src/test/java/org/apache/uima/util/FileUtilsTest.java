/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.uima.util;

import static org.apache.uima.pear.util.FileUtil.extractFilesFromJar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;


public class FileUtilsTest extends TestCase {
  public void testFindRelativePath() throws Exception {
    File target = new File("/this/is/a/file.txt");
    File base = new File("/this/is/a/test");
    assertEquals("../file.txt", FileUtils.findRelativePath(target, base));

    base = new File("c:/foo/bar/baz/dir/");
    target = new File("c:/foo/d1/d2/d3/blah.xml");
    assertEquals("../../../d1/d2/d3/blah.xml", FileUtils.findRelativePath(target, base));

    if (File.separatorChar == '\\') {
      base = new File("c:\\foo\\bar\\baz\\dir\\");
      target = new File("c:\\foo\\d1\\d2\\d3\\blah.xml");
      assertEquals("../../../d1/d2/d3/blah.xml", FileUtils.findRelativePath(target, base));
    }
  }

  public void testReadWriteTempFile() throws IOException {
    final String tmpDirPath = System.getProperty("java.io.tmpdir");
    assertNotNull("java.io.tmpdir system property not available", tmpDirPath);
    File tmpDir = FileUtils.createTempDir(new File(tmpDirPath), "fileUtilsTest");
    File tmpFile1 = FileUtils.createTempFile("test", null, tmpDir);
    File tmpFile2 = FileUtils.createTempFile("test", null, tmpDir);
    final String text = "This is some text to test file writing.  Add an Umlaut for encoding tests:"
        + "\n  Greetings from T\u00FCbingen!\n";
    final String utf8 = "UTF-8";

   //  UIMA-2050 Does not work on all platform encodings
   //  Solution: Do not do it!
   //  FileUtils.saveString2File(text, tmpFile1);
   //  assertEquals(text, FileUtils.file2String(tmpFile1));
    
    FileUtils.saveString2File(text, tmpFile2, utf8);
    assertEquals(text, FileUtils.file2String(tmpFile2, utf8));
    
    FileUtils.deleteRecursive(tmpDir);
  }

  public void testThatAllFilesGoToTargetFolder() throws Exception {
    File tmpDir = FileUtils.createTempDir(new File(System.getProperty("java.io.tmpdir")),
            "fileUtilsTest");
    try {
      File zipFile = new File(tmpDir, "test.zip");

      try (ZipOutputStream oStream = new ZipOutputStream(new FileOutputStream(zipFile))) {
        ZipEntry zipEntry = new ZipEntry("../whoops.txt");
        oStream.putNextEntry(zipEntry);
      }

      File target = new File(tmpDir, "target");
      try (JarFile jarFile = new JarFile(zipFile)) {
        extractFilesFromJar(jarFile, target);
        fail("Expected IOException");
      } catch (IOException e) {
        assertTrue(e.getMessage(), e.getMessage().contains("Can only write within target folder"));
      }
    } finally {
      FileUtils.deleteRecursive(tmpDir);
    }
  }
}
