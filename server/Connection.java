import java.io.*;
import java.net.*;

public class Connection extends Thread {

    private Socket s;

    public Connection(Socket s) {
        this.s = s;
    }
    
public void store(DataOutputStream writer, DataInputStream reader) throws IOException {
    String fileName = reader.readUTF();
    if (fileName.equals("CANCEL")) {
        return;
    }

    long fileSize = reader.readLong();
    File dir = new File("serverFiles");
    dir.mkdir();
    File destFile = new File(dir, fileName);

    try (FileOutputStream fos = new FileOutputStream(destFile)) {
        byte[] buffer = new byte[4096];
        while (fileSize > 0) {
            int read = reader.read(buffer, 0, (int) Math.min(buffer.length, fileSize));
            fos.write(buffer, 0, read);
            fileSize -= read;
        }
    }

    writer.writeUTF("Stored successfully on server.");
}

public void share(String name, DataOutputStream writer, DataInputStream reader) throws IOException {
    String filename = reader.readUTF();
    long fileSize = reader.readLong();
    
    if (fileSize == -1) {
        writer.writeLong(-1);
        return;
    }

    new File("serverFiles").mkdir();
    File destFile = new File("serverFiles", filename);
    try (FileOutputStream fos = new FileOutputStream(destFile)) {
        byte[] buffer = new byte[4096];
        while (fileSize > 0) {
            int read = reader.read(buffer, 0, (int)Math.min(buffer.length, fileSize));
            fos.write(buffer, 0, read);
            fileSize -= read;
        }
    }
    writer.writeLong(destFile.length());
}

public void fetch(DataOutputStream writer) throws IOException { 
// get lisst of files,
// write
    File folder = new File("serverFiles");
    String[] files = folder.list();
    int len = files.length;

    writer.writeInt(len); //num of files available
    // writer.flush();
    
    if (len!=0){
        for (String file: files) {
                writer.writeUTF(file+"\n");
        }
    }

}

@Override
    public void run() {
        try {
            String msg;
            DataInputStream reader = new DataInputStream(s.getInputStream());
            DataOutputStream writer = new DataOutputStream(s.getOutputStream());
            
            
            // This checks whether the string that was sent from
            // the client side is the terminal "END" else we
            // execute cmd
            while (!(msg = reader.readUTF()).equals("END")) {
                
                switch (msg) {
                    case "STORE": store(writer, reader); break;
                    case "SHARE": share(msg, writer, reader); break;
                    case "FETCH": fetch(writer); break;
                }
            }
            s.close();
        } catch (Exception e) {
            e.printStackTrace(); // Uncomment this if you want to look at the error thrown
        } finally {
            System.out.println("Server: Client " + s.getRemoteSocketAddress() + " has disconnected");
        }
    }

}
