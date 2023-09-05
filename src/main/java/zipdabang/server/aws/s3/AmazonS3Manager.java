package zipdabang.server.aws.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import zipdabang.server.config.AmazonConfig;
import zipdabang.server.domain.etc.Uuid;
import zipdabang.server.repository.UuidRepository;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

    private final AmazonS3 amazonS3;
    private final AmazonConfig amazonConfig;
    private final UuidRepository uuidRepository;

    public String uploadFile(String KeyName, MultipartFile file) throws IOException {
        System.out.println(KeyName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(new PutObjectRequest(amazonConfig.getBucket(), KeyName,file.getInputStream(), metadata));

        return amazonS3.getUrl(amazonConfig.getBucket(), KeyName).toString();
    }

    public void deleteFile(String keyname) {
        log.info("KEY NAME : " + keyname);
        amazonS3.deleteObject(amazonConfig.getBucket(),keyname);
    }

    public String generateMemberKeyName(Uuid uuid, String originalFilename) {
        return amazonConfig.getUserProfile() + '/' + uuid.getUuid() + originalFilename;
    }

    public String generateRecipeKeyName(Uuid uuid, String originalFilename) {
        return amazonConfig.getRecipeThumbnail() + '/' + uuid.getUuid() + originalFilename;
    }

    public String generateStepKeyName(Uuid uuid, String originalFilename) {
        return amazonConfig.getRecipeStep() + '/' + uuid.getUuid() + originalFilename;
    }

    // 중복된 UUID가 있다면 중복이 없을때까지 재귀적으로 동작
    public Uuid createUUID() {
        Uuid savedUuid = null;
        String candidate = UUID.randomUUID().toString();
        if (uuidRepository.existsByUuid(candidate)) {
            savedUuid = createUUID();
        }
        savedUuid = uuidRepository.save(Uuid.builder().uuid(candidate).build());
        return savedUuid;
    }
}
