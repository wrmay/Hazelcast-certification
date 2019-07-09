import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class Message  implements DataSerializable {

    private static final ILogger log = Logger.getLogger(Message.class);

    private String message;

    public Message(){
        log.warning("NEW MESSAGE INSTANCE");
    }

    public Message(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void writeData(ObjectDataOutput objectDataOutput) throws IOException {
        objectDataOutput.writeUTF(message);
    }

    @Override
    public void readData(ObjectDataInput objectDataInput) throws IOException {
        message = objectDataInput.readUTF();
    }

    @Override
    public String toString() {
        return "MESSAGE {" + message + "}";
    }

}
