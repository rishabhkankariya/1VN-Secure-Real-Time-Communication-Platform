package com.onevn.client;

import com.onevn.server.Main;
import com.onevn.server.ServerRuntime;
import com.onevn.server.repository.MessageRepository;
import com.onevn.server.repository.UserRepository;
import com.onevn.server.security.MessageEncryption;

public class DesktopLauncher {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   1VN - Secure Real-Time Communication Platform  ");
        System.out.println("               Desktop Edition                    ");
        System.out.println("==================================================");

        // Check if server is already running on localhost:5000
        if (ServerRuntime.isPortAvailable("127.0.0.1", 5000)) {
            System.out.println("[Desktop] No active server detected on 127.0.0.1:5000.");
            System.out.println("[Desktop] Starting self-contained embedded background server...");

            try {
                System.setProperty("1vn.storage", "sqlite");
                MessageEncryption encryption = new MessageEncryption();
                UserRepository userRepo = com.onevn.server.repository.RepositoryFactory.createUserRepository();
                MessageRepository msgRepo = com.onevn.server.repository.RepositoryFactory.createMessageRepository(encryption);

                ServerRuntime runtime = new ServerRuntime(userRepo, msgRepo, 5000);
                runtime.startAsync();

                // Wait briefly for the server socket to accept connections
                long start = System.currentTimeMillis();
                while (ServerRuntime.isPortAvailable("127.0.0.1", 5000) && (System.currentTimeMillis() - start < 3000)) {
                    Thread.sleep(100);
                }
                System.out.println("[Desktop] Embedded server initialized and ready.");
            } catch (Exception e) {
                System.err.println("[Desktop] Warning during embedded server boot: " + e.getMessage());
            }
        } else {
            System.out.println("[Desktop] Detected active server running on 127.0.0.1:5000. Reusing instance.");
        }

        // Launch JavaFX client application
        Main.main(args);
    }
}
