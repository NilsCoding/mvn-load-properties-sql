package io.github.nilscoding.maven.loadprops.sql.utils;

import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Maven utility functions.
 * @author NilsCoding
 */
public final class MavenUtils {

    private MavenUtils() {
    }

    /**
     * Decrypts the server entry by server id.
     * @param serverId          server id from settings.xml
     * @param settings          settings object
     * @param settingsDecrypter settings decrypter object
     * @return decrytped server entry or null if decrypt could not be done
     */
    public static Server decryptServer(String serverId, Settings settings, SettingsDecrypter settingsDecrypter) {
        if ((StringUtils.isEmpty(serverId)) || (settings == null) || (settingsDecrypter == null)) {
            return null;
        }
        try {
            Server server = settings.getServer(serverId);
            if (server == null) {
                return null;
            }
            SettingsDecryptionRequest decryptRequest = new DefaultSettingsDecryptionRequest();
            decryptRequest.setServers(new ArrayList<>(Arrays.asList(server)));
            SettingsDecryptionResult decryptResult = settingsDecrypter.decrypt(decryptRequest);
            Server decryptedServer = decryptResult.getServer();
            return decryptedServer;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Decrypts the server entry.
     * @param server            server
     * @param settingsDecrypter settings decrypter object
     * @return decrytped server entry or null if decrypt could not be done
     */
    public static Server decryptServer(Server server, SettingsDecrypter settingsDecrypter) {
        if ((server == null) || (settingsDecrypter == null)) {
            return null;
        }
        try {
            SettingsDecryptionRequest decryptRequest = new DefaultSettingsDecryptionRequest();
            decryptRequest.setServers(new ArrayList<>(Arrays.asList(server)));
            SettingsDecryptionResult decryptResult = settingsDecrypter.decrypt(decryptRequest);
            Server decryptedServer = decryptResult.getServer();
            return decryptedServer;
        } catch (Exception ex) {
            return null;
        }
    }

}
