package com.hazelcast.certification.server;

import com.hazelcast.map.merge.PassThroughMergePolicy;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/*
 * This is just used to test the txn generator
 */
public class StupidSimpleServer {
    public static void main(String []args){
        Charset ascii  = Charset.forName("ASCII");
        try {
            Socket sock = new Socket("127.0.0.1", 8511);
            sock.getOutputStream().write(0);

            InputStream in = sock.getInputStream();
            byte []buffer = new byte[100];
            int read = in.read(buffer);
            while (read >= 0) {
                if (read == 0)
                    System.out.println(">>> 0 byte read");
                else{
                    System.out.println(">>> " + ascii.decode(ByteBuffer.wrap(buffer, 0, read)));
                }
                read = in.read(buffer);
            }

        } catch (IOException x){
            System.out.println("THAT DIDN'T WORK");
        }
    }
}
