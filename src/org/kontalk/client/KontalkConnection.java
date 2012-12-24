package org.kontalk.client;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

public class KontalkConnection extends XMPPConnection {

    protected EndpointServer mServer;

    public KontalkConnection(EndpointServer server) {
        super(new ConnectionConfiguration(server.getHost(), server.getPort()));

        mServer = server;
        // network name
        config.setServiceName(server.getNetwork());
        // disable reconnection
        config.setReconnectionAllowed(false);
        // enable SASL
        config.setSASLAuthenticationEnabled(true);
        // we don't need the roster
        config.setRosterLoadedAtLogin(false);
        // we will send a custom presence
        config.setSendPresence(false);
    }

    @Override
    public void disconnect() {
        Log.v("KontalkConnection", "disconnecting (no presence)");
        super.disconnect();
    }

    @Override
    public synchronized void disconnect(Presence presence) {
        Log.v("KontalkConnection", "disconnecting ("+presence+")");
        super.disconnect(presence);
    }

}
