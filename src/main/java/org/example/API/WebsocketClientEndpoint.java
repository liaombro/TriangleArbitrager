package org.example.API;

import org.example.Main;

import javax.websocket.*;
import java.net.URI;
import java.nio.ByteBuffer;

@ClientEndpoint
public class WebsocketClientEndpoint {

    Session userSession = null;
    private MsgHandler messageHandler;
    private WebSocketContainer container;
    private URI endpointURI;

    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            this.endpointURI = endpointURI;
            container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        System.out.println("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        System.out.printf("closing websocket, reason: %s\n", reason);
        this.userSession = null;
        Main.RESTART_NEEDED = true;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            this.messageHandler.handleMessage(message);
        }
    }

    @OnMessage
    public void onMessage(ByteBuffer bytes) {
        System.out.println("Handle byte buffer");
    }


    @OnError
    public void onError(Session session, Throwable t){
        System.out.println(t.toString());
        t.printStackTrace();
    }
    /**
     * register message handler
     *
     * @param msgHandler message handler
     */
    public void addMessageHandler(MsgHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

}

