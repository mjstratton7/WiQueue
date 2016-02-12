package com.mstratton.wiqueue;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

// The Class containing all networking components, made into an
// object to be accesible throughout the app.
//
// Uses code from a simple client-server app found here:
// http://lakjeewa.blogspot.com/2014/05/simple-android-client-server-application.html
public class Network {
    private static final String TAG = "Network";
    private Context mContext;

    private WifiManager wifiManager;

    // Server Related
    private static ServerSocket serverSocket;
    private static Socket clientSocket;
    private static InputStreamReader inputStreamReader;
    private static BufferedReader bufferedReader;

    // Client Related
    private Socket client;
    private PrintWriter printwriter;

    private static final int port = 8080;
    private String serverIP;
    private String serverPassword;
    private String serverName;
    private boolean password;
    private boolean running;

    private ArrayList<Server> servers;

    private static String response;

    public Network(Context context) {
        // Context of Parent Activity for Some Actions
        mContext = context;

        // Create WifiManager Object
        wifiManager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
        serverIP = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());

        // Create ArrauList to Store Detected Servers
        servers = new ArrayList<Server>();

        response = "";
    }

    // SERVER --------------------------------------------------------------------------------------

    public void host(String name, String password) {
        // If serverPassword entered, then it is required.
        if (password.equals("")) {
            this.password = false;
        } else {
            this.serverPassword = password;
            this.password = true;
        }

        HttpServerThread httpServerThread = new HttpServerThread();
        httpServerThread.start();

        Log.v(TAG, "Server Started at: " + serverIP + ":" + port);

//        while (true) {
//            try {
//
//                clientSocket = serverSocket.accept(); // accept the client connection
//
//                inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
//
//                bufferedReader = new BufferedReader(inputStreamReader); // get the client message
//
//                response = bufferedReader.readLine();
//
//
//                inputStreamReader.close();
//                clientSocket.close();
//
//                Log.v(TAG, "Received: " + response);
//
//            } catch (IOException ex) {
//
//                Log.v(TAG, "Problem receiving data.");
//
//            }
//
//        }

    }

    public String getServerName() {
        return serverName;
    }

    public boolean requirePassword () {
        return password;
    }

    public void setIP(String ip) {
        serverIP = ip;
    }

    // Got Some Help from here:
    // http://android-er.blogspot.com/2014/08/implement-simple-http-server-running-on.html
    private class HttpServerThread extends Thread {

        @Override
        public void run() {
            Socket socket = null;

            try {
                serverSocket = new ServerSocket(port);

                while(true){
                    socket = serverSocket.accept();

                    HttpResponseThread httpResponseThread = new HttpResponseThread(
                                    socket,
                                    "test response");
                    httpResponseThread.start();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
    }

    // Got Some Help from here:
    // http://android-er.blogspot.com/2014/08/implement-simple-http-server-running-on.html
    private class HttpResponseThread extends Thread {
        Socket socket;
        String h1;

        HttpResponseThread(Socket socket, String msg){
            this.socket = socket;
            h1 = msg;
        }

        @Override
        public void run() {
            BufferedReader is;
            PrintWriter os;
            String request;

            try {
                is = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                request = is.readLine();

                os = new PrintWriter(socket.getOutputStream(), true);

                String response =
                        "<html><head></head>" +
                                "<body>" +
                                "<h1>" + h1 + "</h1>" +
                                "</body></html>";

                os.print("HTTP/1.0 200" + "\r\n");
                os.print("Content type: text/html" + "\r\n");
                os.print("Content length: " + response.length() + "\r\n");
                os.print("\r\n");
                os.print(response + "\r\n");
                os.flush();
                socket.close();

                Log.v(TAG, "Request of " + request + " from " + socket.getInetAddress().toString());

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }

            return;
        }
    }


    // CLIENT --------------------------------------------------------------------------------------

    public boolean connect(String servername, String name, String password) {
        // Find ip of server with name given.
        // Do.
        new HttpSend().execute("reqPassword?");

        try {
            client = new Socket("0.0.0.0", port);

            printwriter = new PrintWriter(client.getOutputStream(), true);
            printwriter.write("Testificate");    // write the message to output stream

            printwriter.flush();
            printwriter.close();

            client.close(); // closing the connection

        } catch (IOException e) {
            // failed to connect
        }

        return true;
    }

    public String getData () {
        return response;
    }

    // Got Some Assistance from here:
    // http://stackoverflow.com/questions/3505930/make-an-http-request-with-android
    private class HttpSend extends AsyncTask<String, String, String> {
        // Create Needed HTML Objects and Handlers
        private HttpClient client;
        private HttpGet httpget;
        ResponseHandler<String> responseHandler;

        @Override
        protected String doInBackground(String... str) {
            HttpClient Client;
            HttpGet httpget;

            try {
                String request_url = "http://" + serverIP + ":" + port + "/recieve?q=" + str[0].replace(" ", "%20");

                // Setup Objects / Handlers
                client = new DefaultHttpClient();
                responseHandler = new BasicResponseHandler();
                httpget = new HttpGet(request_url);

                // Execute HTTP Request and save result to string
                response = client.execute(httpget, responseHandler);

                return response;

            } catch(Exception e) {

                System.out.println(e);

            }

            // Failed it this is reached, should have returned
            return "Cannot Connect";
        }

        protected void onPostExecute(String result) {
            Toast.makeText(mContext.getApplicationContext(), serverIP + " RET: " + response, Toast.LENGTH_SHORT).show();

        }
    }

    // UTILS  --------------------------------------------------------------------------------------

    public ArrayList<String> discover() {
        ArrayList<String> activeAddresses = getActiveIPs();

        // Add in Default Options, would set this in startview, but overwriting
        // that list with this one.
        servers.add(new Server("Select A Host", "0.0.0.0"));
        servers.add(new Server("Host A Playlist", "0.0.0.0"));

        for (String s : activeAddresses) {

            serverIP = "10.0.1.19";
            new HttpSend().execute("reqPassword?");

            if (response.equals("true")) {
                password = true;

                // get name to add to list

            } else if (response.equals("false")) {
                password = false;

                // get name to add to list
            } else {

                // not a playlist server

            }
        }

        // Tester
        servers.add(new Server("Mike's Phone", "10.0.1.19"));

        // List of Server Names to Return
        ArrayList<String> serverList = new ArrayList<String>();

        if (!servers.isEmpty()) {
            for (Server s : servers) {
                serverList.add(s.name);
            }
        }

        return serverList;
    }

    // Find All Active IP Addresses on WiFi Network, store them to check later.
    // Based on Code Found here:
    // http://www.javaprogrammingforums.com/java-networking/6739-how-get-list-all-computers-my-network-computers-connected-me.html
    private ArrayList<String> getActiveIPs() {
        ArrayList<String> activeAddresses = new ArrayList<String>();

        // Stop after how many inactive IPs.
        int badCount = 0;
        int badThreshold = 7;

        try {
            // this code assumes IPv4 is used
            byte[] ip =  InetAddress.getByName(serverIP).getAddress();

            for (int i = 1; i <= 254; i++) {
                if (badCount > badThreshold) {
                    break;
                }

                ip[3] = (byte)i;
                InetAddress address = InetAddress.getByAddress(ip);

                if (address.isReachable(1000)) {

                    // machine is turned on and can be pinged

                    activeAddresses.add(ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3]);
                    Log.v(TAG, "Detected: " + ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3]);
                    badCount = 0;

                } else if (!address.getHostAddress().equals(address.getHostName())) {

                    // machine is known in a DNS lookup

                    Log.v(TAG, "Detected DNS: " + ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3]);
                    badCount++;

                } else {

                    // the host address and host name are equal, meaning the host name could not be resolved
                    badCount++;

                }

            }

        } catch (UnknownHostException e) {

        } catch (IOException e) {

        }

        return activeAddresses;
    }

    // Server object, to hold the list of servers.
    private class Server {
        String name;
        String ip;

        public Server (String name, String ip) {
            this.name = name;
            this.ip = ip;
        }
    }

}