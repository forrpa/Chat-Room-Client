import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Thread that is constantly listening for incoming text messages from the chat server
 *
 * @author Jennifer McCarthy
 */
public class ReceiverThread extends Thread {

    private final Client client;
    private final BufferedReader bufferedReader;

    /**
     * Constructor that sets the thread's client and reader
     *
     * @param client the client that will receive the message
     * @param bufferedReader is used to read incoming messages
     */
    public ReceiverThread(Client client, BufferedReader bufferedReader){
        this.client = client;
        this.bufferedReader = bufferedReader;
    }

    /**
     * Method that listens for incoming messages from the chat server
     * If normal message is received it will be printed in the chat client
     * If user is connected or disconnected it will be added or removed
     */
    @Override
    public void run(){

        /* Receives all online clients usernames from the server and prints them */
        try{
            String onlineList = bufferedReader.readLine();
            System.out.println(onlineList);
            if (onlineList.length() > 0) {
                onlineList = onlineList.substring(0, onlineList.length() -1);
                List<String> list = new ArrayList<>(Arrays.asList(onlineList.split(",")));
                for (String s : list){
                    client.addToList(s);
                }
                client.updateListModel();
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        /* Listens for incoming messages */
        while(true){
            try{
                String msg = bufferedReader.readLine();
                String username = "";

                if (msg != null) {
                    if (msg.startsWith("CONNECTED")) {
                        username = msg.substring(msg.indexOf("[") + 1, msg.indexOf("]"));
                        if (username.equals(client.getUsername())){
                            client.printMessage("Welcome " + username + "!");
                        } else {
                            client.addToList(username);
                            client.updateListModel();
                            client.printMessage(username + " just joined!");
                        }
                    } else if (msg.startsWith("DISCONNECTED")) {
                        username = msg.substring(msg.indexOf("[") + 1, msg.indexOf("]"));
                        client.removeFromList(username);
                        client.updateListModel();
                        client.printMessage(username + " just left!");
                    } else {
                        client.printMessage(msg);
                    }
                }
            } catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}