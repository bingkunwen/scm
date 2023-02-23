import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SshConnectionPool {
    private final String host;
    private final String user;
    private final String password;
    private final int maxConnections;
    private final BlockingQueue<Channel> connectionPool;

    public SshConnectionPool(String host, String user, String password, int maxConnections) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.maxConnections = maxConnections;
        this.connectionPool = new ArrayBlockingQueue<>(maxConnections);
        initialize();
    }

    private void initialize() {
        try {
            for (int i = 0; i < maxConnections; i++) {
                JSch jsch = new JSch();
                Session session = jsch.getSession(user, host, 22);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                Channel channel = session.openChannel("shell");
                channel.connect();
                connectionPool.add(channel);
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public synchronized Channel getConnection() {
        Channel channel = null;
        try {
            channel = connectionPool.take();
            while (!channel.isConnected()) {
                channel = connectionPool.take();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return channel;
    }

    public synchronized void releaseConnection(Channel channel) {
        connectionPool.add(channel);
    }

    public void destroy() {
        for (Channel channel : connectionPool) {
            channel.disconnect();
        }
        connectionPool.clear();
    }
}
