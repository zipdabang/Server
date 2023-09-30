package zipdabang.server.service;

import zipdabang.server.domain.Category;
import zipdabang.server.domain.Report;
import zipdabang.server.domain.inform.Notification;

import java.io.IOException;
import java.util.List;

public interface RootService {

    List<Category> getAllCategories();

    Boolean autoLoginService(String authorizationHeader);

    List<Notification> notificationList();

    Notification findNotification(Long notificationId);

    List<Report> getAllReports();

    void testFCMService(String fcmToken) throws IOException;
}
