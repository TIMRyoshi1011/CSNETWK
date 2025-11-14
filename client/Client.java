import java.io.*;
import java.net.*;
import java.util.*; // For Scanner

public class Client {

    public static void main(String[] args) {
        String host = "localhost"; // can be changed
        int port = 5000;
        Scanner sc = new Scanner(System.in);

        String msg;
        try {
            Socket endpoint = new Socket(host, port);

            System.out.println("Client: Has connected to server " + host + ":" + port);
            //TODO: commands menu

            DataInputStream reader = new DataInputStream(endpoint.getInputStream());
            DataOutputStream writer = new DataOutputStream(endpoint.getOutputStream());
            String file = new String();
            new File("clientFiles").mkdir();


            String prompt = "\n-----Command List-----\n\n" +
                                "store <filename>\n" + 
                                "   - stores a text file to the server with the specified filename.\n\n" + 
                                "share <filename>\n" + 
                                "   -this shares the file to other clients\n\n" + 
                                "fetch \n" + 
                                "   - this fetches an existing file from the server\n\n" + 
                                "--Enter 'END' to end the connection--\n" + 
                                "> ";
            System.out.print(prompt);
            while (!(msg = sc.nextLine()).equals("END")) {
                String[] parts = msg.split(" ", 2);
                String command = parts[0].toUpperCase();
                
                writer.writeUTF(command);

                    switch (command) {
                        case "FETCH": 
                            int n = reader.readInt();
                            if (n==0)
                                    System.out.println("No files in server.");
                                
                            else {
                                System.out.println("Server files:" + n);
                                    for(int i=0;i<n;i++) {
                                        System.out.println(". "+reader.readUTF());
                                    }
                                }
                            break;

                        case "SHARE": 
                        String name = (parts.length > 1) ? parts[1] : sc.nextLine();
                        writer.writeUTF(name); // Send filename to server
                        
                        File clientFolder = new File("clientFiles");
                        
                        // Debug: Display client folder contents
                        System.out.println("\nClient folder contents:");
                        File[] files = clientFolder.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                System.out.println("  " + f.getName());
                            }
                        }
                        
                        File shareFile = new File(clientFolder, name);
                        
                        File fileToShare = null;
                        if (shareFile.exists()) {
                            fileToShare = shareFile;
                        }
                        if (fileToShare == null) {
                            writer.writeLong(-1);
                            System.out.println("File not found in: " + new File("clientFiles").getAbsolutePath());
                        } else {
                            writer.writeLong(fileToShare.length());
                            try (FileInputStream fis = new FileInputStream(fileToShare)) {
                                byte[] buffer = new byte[4096];
                                int read;
                                while ((read = fis.read(buffer)) > 0) {
                                    writer.write(buffer, 0, read);
                                }
                            }
                            System.out.println("File shared successfully");
                            System.out.println(reader.readUTF());
                        }
                        break;
                        case "STORE":
                            System.out.print("Enter file name to store: ");
                            String fileName = sc.nextLine();

                            File fileToUpload = new File("clientFiles", fileName);
                            if (!fileToUpload.exists()) {
                                System.out.println("File not found in client folder.");
                                writer.writeUTF("CANCEL");
                                break;
                            }

                            writer.writeUTF(fileName);
                            writer.writeLong(fileToUpload.length());

                            try (FileInputStream fis = new FileInputStream(fileToUpload)) {
                                byte[] buffer = new byte[4096];
                                int read;
                                while ((read = fis.read(buffer)) > 0) {
                                    writer.write(buffer, 0, read);
                                }
                            }

                            System.out.println("File uploaded to server successfully.");
                            break;

                        default: System.out.print("Invalid input, try again.\n");
                    }
                    //System.out.println(reader.readUTF()); // Read completion message
                System.out.print(prompt);
            }
            System.out.println("Client: has terminated connection");

            writer.writeUTF("END");
            endpoint.close();
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

            // // Let's try inputting a string in the console
            // while (!(msg = sc.nextLine()).equals("END")) {
            //     // The message will be send to the server
            //     writer.writeUTF(msg);
                
            //     //print everything from server until "done" mssg
            //     // while (!(response = reader.readUTF()).contains("done")) {
            //     //     System.out.println(response);
            //     // }   

            // }
