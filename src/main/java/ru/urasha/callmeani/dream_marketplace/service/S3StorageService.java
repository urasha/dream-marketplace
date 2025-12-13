package ru.urasha.callmeani.dream_marketplace.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import ru.urasha.callmeani.dream_marketplace.config.AppS3Properties;

@Service
public class S3StorageService {

    private final S3Client s3;
    private final AppS3Properties props;

    public S3StorageService(S3Client s3, AppS3Properties props) {
        this.s3 = s3;
        this.props = props;
    }

    public String uploadBytes(String key, byte[] content, String mime) {
        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(key)
                .contentType(mime)
                .build();
        s3.putObject(req, RequestBody.fromBytes(content));
        return "s3://" + props.bucket() + "/" + key;
    }
}
