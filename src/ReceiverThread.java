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
     * Constructor that sets the thread's client and buffered reader
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

        addOnlineUsernames();
        while(true){
            try{
                String msg = bufferedReader.readLine();
                String username = "";

                if (msg != null) {
                    if (msg.startsWith("CONNECTED")) {
                        username = getUsername(msg);
                        if (username.equals(client.getUsername())){
                            client.printMessage("Welcome " + username + "!");
                        } else {
                            client.addToList(username);
                            client.updateListModel();
                            client.printMessage(username + " just joined!");
                        }
                    } else if (msg.startsWith("DISCONNECTED")) {
                        username = getUsername(msg);
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

    /**
     * Gets the client username
     *
     * @param msg the message where the username is
     * @return username of message
     */
    public String getUsername(String msg){
        return msg.substring(msg.indexOf("[") + 1, msg.indexOf("]"));
    }

    /**
     * Receives all online clients usernames from the server and adds them to the client online list
     */
    public void addOnlineUsernames(){
        try{
            String onlineList = bufferedReader.readLine();
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
    }
}