package com.example.nadavlotan.autodemo;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class TextFetcher {

    private Socket mSocket;
    private static final String URL = "https://abc-app-server-milab2019.herokuapp.com";

    {
        try {
            mSocket = IO.socket(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public Socket getmSocket() {
        return mSocket;
    }
}
