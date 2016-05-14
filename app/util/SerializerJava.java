package util;

import com.google.common.base.Throwables;
import play.Logger;

import java.io.*;
/**
 *
 * Created by howen on 16/2/25.
 */
public class SerializerJava{

    /**
     * {@inheritDoc}
     */
    public static byte[] serializeObject(Object obj) {

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutput out;
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            out.close();

            return bos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(e));
            throw new IllegalArgumentException("Could not serialize object " + obj, e);
        }

    }

    /**
     * {@inheritDoc}
     */
    public static Object deserializeObject(byte[] serializedObj) {

        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(serializedObj));
            return in.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Logger.error(Throwables.getStackTraceAsString(e));
            throw new IllegalArgumentException("Could not deserialize", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserializeAndCast(byte[] serializedObj) {
        Object obj = deserializeObject(serializedObj);
        return (T)obj;
    }

}
