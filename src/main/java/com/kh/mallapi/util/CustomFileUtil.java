package com.kh.mallapi.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;

@Component
@Log4j2
@RequiredArgsConstructor
public class CustomFileUtil {

	@Value("${com.kh.upload.path}")
	private String uploadPath;

	@PostConstruct
	// CustomFileUtil 객체로 로드되면서, init() 함수를 자동으로 실행해줘.
	public void init() {
		File tempFolder = new File(uploadPath);
		if (tempFolder.exists() == false) {
			tempFolder.mkdir();
		}
		uploadPath = tempFolder.getAbsolutePath();
		log.info("tempFolder.getAbsolutePath() = " + uploadPath);

	}

	// 사용자가 보내준 리스트파일을 내장폴더에 중복되지 않는 이름으로 변경해서 저장하고, 파일명들을 리스트에 저장해서 리턴한다.
	public List<String> saveFiles(List<MultipartFile> files) throws RuntimeException {
		// 파일이 없거나 비어있으면 빈 리스트를 반환하는 것이 NullPointerException 방지에 좋습니다.
		if (files == null || files.isEmpty() || files.get(0).isEmpty()) {
			return new ArrayList<>();
		}
		// 절대 중복되지않는 파일명을 만들어서 저장리스트
		List<String> uploadNames = new ArrayList<>();

		for (MultipartFile multipartFile : files) {
			// 1.kkk.jpg => fiowfiowfiowifongn_kkk.jpg
			String savedName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
			// 2. savePath =
			// C:/SpringBootProject/workspace/mallapi/upload/fiowfiowfiowifongn_kkk.jpg
			Path savePath = Paths.get(uploadPath, savedName);

			try {
				// 3. 사용자가 보내준 파일을 복사 (inputStream =>
				// C:/SpringBootProject/workspace/mallapi/upload/fiowfiowfiowifongn_kkk.jpg
				Files.copy(multipartFile.getInputStream(), savePath);
				// 4. [fiowfiowfiowifongn_kkk.jpg, nuinfujirnnjksnfeklngn_qqq.jpg]
				// 파일의 타입 kkk.jpg => jpg 타일 파일이다.
				String contentType = multipartFile.getContentType();
				// 썸네일 생성
				// 타입을 체크, 진짜 이미지 파일인지 검토. (파일명) 필터링
				if (contentType != null && contentType.startsWith("image")) {
					// 썸네일파일명 생성:
					// C:/SpringBootProject/workspace/mallapi/upload/s_fndujiskanujiker_kkk.jpg
					Path thumbnailPath = Paths.get(uploadPath, "s_" + savedName);
					// 원본파일을 가로폭, 세로폭을 400씩 변경을 해서 썸네일파일에 저장한다.
					Thumbnails.of(savePath.toFile()).size(400, 400).toFile(thumbnailPath.toFile());
				}
				uploadNames.add(savedName);
			} catch (IOException e) {
				throw new RuntimeException("File save error: " + e.getMessage());
			}
		}
		return uploadNames;
	}

	// 브라우저에게 화면을 보여주는 기능 담당함수
	public ResponseEntity<Resource> getFile(String fileName) {
		// C:/SpringBootProject/workspace/mallapi/upload/s_fndujiskanujiker_kkk.jpg
		Resource resource = new FileSystemResource(uploadPath + File.separator + fileName);
		// 보낼파일이 존재하는지 체크
		if (!resource.exists()) {
			resource = new FileSystemResource(uploadPath + File.separator + "default.jpg");
		}
		// 웹브라우저에 보낼 header
		HttpHeaders headers = new HttpHeaders();
		try {
			// Files.probeContentType()은 파일 경로를 분석하여 MIME 타입을 자동 감지 jpg → image/jpeg, png →
			// image/png pdf → application/pdf 이 정보를 HTTP 응답 헤더에 Content-Type으로 추가한다
			headers.add("Content-Type", Files.probeContentType(resource.getFile().toPath()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError().build();
		}

		return ResponseEntity.ok().headers(headers).body(resource);
	}

	public void deleteFiles(List<String> fileNames) {
		if (fileNames == null || fileNames.isEmpty()) {
			return;
		}
		
		fileNames.forEach(fileName -> {
			// 썸네일이 있는지 확인하고 삭제
			String thumbnailFileName = "s_" + fileName;
			// 썸네일이미지경로확인
			Path thumbnailPath = Paths.get(uploadPath, thumbnailFileName);
			// 원본이미지경로
			Path filePath = Paths.get(uploadPath, fileName);
			try {
				Files.deleteIfExists(filePath);
				Files.deleteIfExists(thumbnailPath);
			} catch (IOException e) {
				throw new RuntimeException(e.getMessage());
			}
		});
	}
}