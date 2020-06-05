package org.sun.raft.io;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

public class JavaSerializationWriterTest {

    private JavaSerializationWriter writer;

    @BeforeClass
    public void setUp() {

        File file = new File(System.getProperty("java.io.tmp") + File.pathSeparator + "/a");

        if(!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {

            }
        }



        //writer = new JavaSerializationWriter()
    }

    @AfterClass
    public void cleanUp() {

    }

    @Test
    public void testWrite() {

    }

    @Test
    public void testReset() {
    }
}

class Person {

    String name;

    public Person(String name) {
        this.name = name;
    }

}