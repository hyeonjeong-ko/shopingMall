//package goorm.server.timedeal.service.aws;
//
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.UUID;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Service
//public class S3Service {
//	private final AmazonS3 amazonS3Client;
//	private final RestTemplate restTemplate; // HTTP 요청을 보낼 RestTemplate
//
//	// Load bucket name, CloudFront CNAME, and image folder from application.yml
//	@Value("${cloud.aws.s3.bucket}")
//	private String bucketName;
//
//	@Value("${cloud.aws.cloudfront.cname}")
//	private String cloudFrontCname;
//
//	@Value("${cloud.aws.s3.image-folder}")
//	private String imageFolder;
//
//	public S3Service(AmazonS3 amazonS3Client, RestTemplate restTemplate) {
//		this.amazonS3Client = amazonS3Client;
//		this.restTemplate = restTemplate;
//	}
//
//	/**
//	 * URL로 이미지를 받아 S3에 업로드하고, 해당 URL을 반환하는 메서드
//	 */
//	public String uploadImageFromUrl(String imageUrl) throws IOException {
//		log.info("Uploading image from URL: " + imageUrl);
//
//		// 1. 이미지 URL로 HTTP GET 요청을 보내서 이미지 데이터 가져오기
//		byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
//
//		// 2. 파일명 생성: UUID로 고유한 파일 이름 생성
//		String fileName = UUID.randomUUID().toString() + ".jpg";  // 예시로 .jpg 확장자 사용
//
//		// 3. S3에 업로드하기 위한 InputStream 생성
//		assert imageBytes != null;
//		InputStream inputStream = new ByteArrayInputStream(imageBytes);
//
//		// 4. ObjectMetadata 생성 및 설정
//		ObjectMetadata metadata = new ObjectMetadata();
//		metadata.setContentLength(imageBytes.length); // Content-Length 설정
//		metadata.setContentType("image/jpeg"); // MIME 유형 설정
//
//		// 5. S3에 이미지 업로드
//		amazonS3Client.putObject(new PutObjectRequest(bucketName, imageFolder + "/" + fileName, inputStream, metadata));
//
//		// 6. 업로드한 이미지의 URL 반환
//		return amazonS3Client.getUrl(bucketName, imageFolder + "/" + fileName).toString();
//	}
//
//	public String uploadImage(MultipartFile file) throws IOException {
//		String fileName = imageFolder + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();
//		ObjectMetadata metadata = new ObjectMetadata();
//		metadata.setContentType(file.getContentType());
//		metadata.setContentLength(file.getSize());
//
//		// Upload file to S3 bucket
//		amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, file.getInputStream(), metadata));
//
//		// Return CloudFront URL
//		return "https://" + cloudFrontCname + "/" + fileName;
//	}
//
//	@Transactional
//	public String uploadImageFromUrlWithCloudFront(String imageUrl) throws IOException {
//		log.info("Uploading image from URL: " + imageUrl);
//
//		// Fetch image bytes from URL
//		byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
//
//		// Generate unique file name
//		String fileName = imageFolder + "/" + UUID.randomUUID() + ".jpg";
//
//		// Convert bytes to InputStream
//		assert imageBytes != null;
//		InputStream inputStream = new ByteArrayInputStream(imageBytes);
//
//		// Prepare metadata
//		ObjectMetadata metadata = new ObjectMetadata();
//		metadata.setContentLength(imageBytes.length);
//		metadata.setContentType("image/jpeg");
//
//		// Upload to S3
//		amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
//
//		// Return CloudFront URL
//		return "https://" + cloudFrontCname + "/" + fileName;
//	}
//}
