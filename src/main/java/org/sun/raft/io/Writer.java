package org.sun.raft.io;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Writer implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(Writer.class);
    private String directory = "";
    private Map<String, ObjectOutputStream> cache = new HashMap();

    public Writer() {
    }

    /**
     * 注意：如果文件被重新打开，文件内容会被覆写，否则是追加
     * @param fileName
     * @param object
     * @throws IOException
     */
    public void writeObject(String fileName, Object object) throws IOException {
        ObjectOutputStream objectOutputStream = (ObjectOutputStream)this.cache.get(fileName);
        if (objectOutputStream == null) {
            objectOutputStream = new ObjectOutputStream(new FileOutputStream(this.directory + fileName));
            this.cache.put(fileName, objectOutputStream);
        }
        objectOutputStream.writeObject(object);
    }

    public Object readObject(String fileName) throws IOException, ClassNotFoundException {
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(fileName));
        Object object = inputStream.readObject();
        inputStream.close();
        return object;
    }

    public void close() throws IOException {
        Iterator var1 = this.cache.entrySet().iterator();

        while(var1.hasNext()) {
            Entry<String, ObjectOutputStream> entry = (Entry)var1.next();
            ((ObjectOutputStream)entry.getValue()).close();
        }

        this.cache.clear();
    }

    public void close(String fileName) throws IOException {
        ObjectOutputStream outputStream = (ObjectOutputStream)this.cache.get(fileName);
        if (outputStream != null) {
            outputStream.close();
            this.cache.remove(fileName);
        }

    }
}
