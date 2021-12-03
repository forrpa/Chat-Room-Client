import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.lang.Exception;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Login page where users can log in and register
 *
 * @author Jennifer McCarthy, jemc7787, 930124-0983
 */
class Login extends JFrame implements ActionListener {

    private JLabel label;
    private JTextField usernameText, passwordText;
    private final int port;
    private final String ip;

    Login(int port, String ip) {
        this.port = port;
        this.ip = ip;
        createGUI();
    }

    /**
     * Creates a GUI for the login page
     */
    public void createGUI(){

        setTitle("Chat login: port " + port);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(600, 300);

        Color c = new Color(240,248,255);
        Color button = new Color(184,207,229);

        JLabel welcomeLabel = new JLabel("Welcome to chat room " + port + "!");
        Font font = new Font(Font.DIALOG_INPUT, Font.BOLD, 16);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        welcomeLabel.setFont(font);
        JLabel emptyLabel = new JLabel("");
        label = new JLabel("");
        JLabel usernameLabel = new JLabel();
        usernameLabel.setText("Username");
        usernameText = new JTextField(14);
        JLabel passwordLabel = new JLabel();
        passwordLabel.setText("Password");
        passwordText = new JPasswordField(14);
        passwordText.addKeyListener(new LoginListener());
        JButton loginBtn = new JButton("LOG IN");
        loginBtn.setBackground(button);
        JButton registerBtn = new JButton("REGISTER");
        registerBtn.setBackground(button);
        loginBtn.addActionListener(new LoginListener());
        registerBtn.addActionListener(this);

        JPanel jPanel = new JPanel(new GridLayout(5, 1));
        jPanel.setBackground(c);
        jPanel.setBorder(BorderFactory.createEmptyBorder(50, 30, 30, 30));
        jPanel.add(welcomeLabel);
        jPanel.add(emptyLabel);
        jPanel.add(usernameLabel);
        jPanel.add(usernameText);
        jPanel.add(passwordLabel);
        jPanel.add(passwordText);
        jPanel.add(loginBtn);
        jPanel.add(registerBtn);
        jPanel.add(label);

        add(jPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * Inner class that is used as a key and action listener when user tries to log in
     */
    class LoginListener implements KeyListener, ActionListener {
        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {

        }

        /**
         * Method that is called when user presses enter key
         * Check if username and password is null and if not call login method
         *
         * @param e
         */
        @Override
        public void keyReleased(KeyEvent e) {
            String username = usernameText.getText();
            String password = passwordText.getText();

            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (username.isEmpty() || password.isEmpty()){
                    label.setText("");
                    label.setText("Username and password can't be empty!");
                } else {
                    login(username, password);
                }
            }
        }

        /**
         * Method that is called when user presses 'Log in' button
         * Check if username and password is null and if not call login method
         *
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameText.getText();
            String password = passwordText.getText();

            if (username.isEmpty() || password.isEmpty()){
                label.setText("");
                label.setText("Username and password cant be empty!");
            } else {
                login(username, password);
            }
        }

        /**
         * Checks if username and password exists in database and if they ar correct
         * If correct a new client will be created with port, ip address and username and login form will not be visible
         * If not correct a label will print that it is wrong username or password
         *
         * @param username user to be logged in
         * @param password user password
         */
        public void login(String username, String password){
            try {
                Connection connection = DriverManager.getConnection("jdbc:mysql://atlas.dsv.su.se:3306/db_20790470", "usr_20790470", "790470");
                PreparedStatement preparedStatement = connection.prepareStatement("select username, password from user where username = ? and password = ?");
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    dispose();
                    Client client = new Client(port, ip, username);
                    client.run();
                    setVisible(false);
                } else {
                    label.setText("");
                    label.setText("Wrong username or password");
                }

            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
            }
        }
    }

    /**
     * Method that is called when user presses 'register' button
     * If username not exists a new user is created in sql database with username and password
     * If username exists a message appears saying that username exists
     *
     * @param actionEvent
     */
    public void actionPerformed(ActionEvent actionEvent)
    {
        String username = usernameText.getText();
        String password = passwordText.getText();

        if (username.isEmpty() || password.isEmpty()){
            label.setText("");
            label.setText("Username and password cant be empty!");
        } else {
            String sanitizedUsername = sanitizeString(username);
            String sanitizedPassword = sanitizeString(password);

            if (sanitizedUsername.equals("censur") || sanitizedPassword.equals("censur")){
                label.setText("");
                label.setText("No HTML!");
            } else {
                String query = "insert into user " + "values ('" + sanitizedUsername + "','" + sanitizedPassword + "', null)";
                try {
                    label.setText("");
                    Connection connection = DriverManager.getConnection("jdbc:mysql://atlas.dsv.su.se:3306/db_20790470", "usr_20790470", "790470");
                    Statement statement = connection.createStatement();
                    statement.executeUpdate(query);
                    usernameText.setText("");
                    passwordText.setText("");
                    label.setText("You are now registered as " + username + "!");
                } catch (SQLException sq) {
                    label.setText("Username already exists: " + sq.getMessage());
                }
            }
        }
    }

    /**
     * Sanitizes string if it contains html code
     *
     * @param s string to be sanitized
     * @return string
     */
    public String sanitizeString(String s) {
        Pattern p = Pattern.compile("<.*>");
        Matcher matcher = p.matcher(s);
        if (matcher.find()) {
            return "censur";
        }
        return s;
    }

    /**
     * Main method that connects to jdbc database and starts a new Login page with standard or user inputted port and ip address
     *
     * @param args potential port and ip address
     */
    public static void main(String args[]) {
        int port = 2000;
        String ip = "127.0.0.1";

        if (args.length == 1) {
            ip = args[0];
        } else if (args.length == 2) {
            ip = args[0];
            port = Integer.parseInt(args[1]);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            DriverManager.getConnection("jdbc:mysql://atlas.dsv.su.se:3306/db_20790470", "usr_20790470", "790470");
            new Login(port, ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
