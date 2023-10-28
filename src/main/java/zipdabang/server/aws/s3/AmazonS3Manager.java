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
import java.util.List;
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

    public String getPattern(){
        return "https://"+amazonConfig.getBucket()+"\\.s3\\."+amazonConfig.getRegion()+"\\.amazonaws\\.com(.*)";
    }

    public void deleteFile(String keyname) {

        log.info("keyname: " + keyname);

        if(amazonS3.doesObjectExist(amazonConfig.getBucket(), keyname)) {

            log.info("KEY NAME : " + keyname);

            amazonS3.deleteObject(amazonConfig.getBucket(), keyname);

            String[] keynameSplit = keyname.split("/");
            String getUuid = keynameSplit[keynameSplit.length-1];
            log.info(getUuid);

            uuidRepository.deleteByUuid(getUuid);
            log.info("해당 uuid 삭제: "+ !uuidRepository.existsByUuid(getUuid));
        } else{
            log.info("KEY NAME : " + keyname + "에 해당하는 파일이 s3에 없음.");
        }

    }

    public String generateMemberKeyName(Uuid uuid) {
        return amazonConfig.getUserProfile() + '/' + uuid.getUuid();
    }

    public String generateRecipeKeyName(Uuid uuid) {
        return amazonConfig.getRecipeThumbnail() + '/' + uuid.getUuid();
    }

    public String generateStepKeyName(Uuid uuid) {
        return amazonConfig.getRecipeStep() + '/' + uuid.getUuid();
    }

    public String generateInqueryKeyName(Uuid uuid) {return amazonConfig.getInquery() + '/' + uuid.getUuid();}

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
