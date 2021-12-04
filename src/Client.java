import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Chat client that sends and receives text messages from other clients via a chat server using stream sockets
 * If not connected to a chat server the client will shut down
 *
 * @author Jennifer McCarthy
 */
public class Client extends JFrame {

    private final ArrayList<String> connectedUsernames = new ArrayList<>();
    private final int port;
    private final String ip;
    private PrintWriter printWriter;
    private JTextArea jTextArea;
    private JTextField jTextField;
    private DefaultListModel<String> listModel;
    private JList<String> jList;
    private final String username;

    /**
     * Constructor that sets the clients port number, ip address and username
     * Calls a method to create a GUI
     */
    public Client(int port, String ip, String username) {
        this.port = port;
        this.ip = ip;
        this.username = username;
        createGUI();
    }

    /**
     * Creates a GUI for the chat client
     */
    public void createGUI(){

        Color c = new Color(240,248,255);
        Color button = new Color(184,207,229);
        setTitle("Chat room");

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(c);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel jLabel = new JLabel("Chat room on port " + port);
        Font font = new Font(Font.DIALOG_INPUT, Font.BOLD, 16);
        jLabel.setFont(font);
        jLabel.setPreferredSize(new Dimension(0, 50));

        jTextArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        jScrollPane.setBorder(BorderFactory.createLineBorder(button));
        jScrollPane.setBackground(c);
        jTextArea.setEditable(false);

        jTextField = new JTextField();
        jTextField.setBorder(BorderFactory.createLineBorder(button));
        jTextField.addKeyListener(new SendMessageListener());

        JButton jButton = new JButton("Send");
        jButton.setBackground(button);
        jButton.setBorder(BorderFactory.createLineBorder(button));
        jButton.setPreferredSize(new Dimension(60,40));
        jButton.addActionListener(new SendMessageListener());

        JPanel sendPanel = new JPanel();
        sendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        sendPanel.setBackground(c);
        sendPanel.setLayout(new BorderLayout());
        sendPanel.add(jTextField, BorderLayout.CENTER);
        sendPanel.add(jButton, BorderLayout.EAST);

        listModel = new DefaultListModel<>();
        jList = new JList<>(listModel);
        updateListModel();

        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setSelectedIndex(0);
        jList.setVisibleRowCount(5);
        JScrollPane listScrollPane = new JScrollPane(jList);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Online"));
        listScrollPane.setBackground(c);
        listScrollPane.setPreferredSize(new Dimension(150, 80));

        panel.add(jLabel, BorderLayout.NORTH);
        panel.add(sendPanel, BorderLayout.SOUTH);
        panel.add(listScrollPane, BorderLayout.EAST);
        panel.add(jScrollPane, BorderLayout.CENTER);

        setSize(700, 500);
        setLocationRelativeTo(null);
        add(panel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Tries to connect to a chat server with the ip address and port number
     * If succeeded a new receiver thread is created to start listening to incoming messages from the server
     * If not succeeded the program will shut down
     */
    public void run() {
        try {
            Socket socket = new Socket(ip, port);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            setTitle("CONNECTED TO: " + socket.getInetAddress().getHostName() + " - ON PORT: " + socket.getPort());
            ReceiverThread receiverThread = new ReceiverThread(this, bufferedReader);
            receiverThread.start();
            this.printWriter.println(username);
        } catch (Exception e) {
            System.exit(0);
        }
    }

    /**
     * Prints an incoming text message from a chat client via the chat server
     *
     * @param msg message
     */
    public void printMessage(String msg) {
        Color color = new Color(93, 109, 126);
        jTextArea.append(msg + "\n");
        jTextArea.setForeground(color);
    }

    /**
     * Sends a message to the client that is marked in the list
     * If 'All' is marked the message will be broadcasted
     *
     * @param msg the message to be sent
     */
    public void sendMessage(String msg){
        String receiver = jList.getSelectedValue();
        if (receiver.equals("All")){
            printWriter.println("[" + username + ">All]: " + msg);
            return;
        }
        printWriter.println("[" + username + ">" + receiver + "]: " + msg);
    }

    /**
     * Adds a username to the list of online usernames
     *
     * @param s username to be added
     */
    public void addToList(String s){
        connectedUsernames.add(s);
    }

    /**
     * Removes a username from the list of online usernames
     *
     * @param s username to be deleted
     */
    public void removeFromList(String s){
        connectedUsernames.remove(s);
    }

    /**
     * Updates the ListModel of online users
     */
    public void updateListModel(){
        listModel.removeAllElements();
        listModel.addElement("All");
        for (String s : connectedUsernames){
            listModel.addElement(s);
        }
        jList.setSelectedIndex(0);
    }

    /**
     * Gets client username
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Inner class that is used as a key listener to the text field so that a text message will be sent when pressing enter or pressing 'send' button
     */
    class SendMessageListener implements KeyListener, ActionListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        /**
         * Sends a message if user presses Enter key
         *
         * @param e key event
         */
        @Override
        public void keyReleased(KeyEvent e) {
            String msg = jTextField.getText();
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                sendMessage(msg);
                jTextField.setText("");
            }
        }

        /**
         * Sends a message if user presses Send button
         *
         * @param e key event
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = jTextField.getText();
            sendMessage(msg);
            jTextField.setText("");
        }
    }
}