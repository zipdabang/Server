package zipdabang.server.firebase.fcm.service;

import java.io.IOException;

public interface FirebaseService {

    void sendMessageTo(String targetToken, String title, String body, String targetView, String targetPK, String targetNotification) throws IOException;
}
