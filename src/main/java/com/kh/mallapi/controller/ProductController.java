package com.kh.mallapi.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;
import com.kh.mallapi.service.ProductService;
import com.kh.mallapi.util.CustomFileUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/products")
public class ProductController {
	private final CustomFileUtil fileUtil;
	private final ProductService productService;

	@PostMapping("/")
	public Map<String, Long> register(ProductDTO productDTO) {
		log.info("rgister: " + productDTO);
		// 첨부된파일
		List<MultipartFile> files = productDTO.getFiles();
		// 중복되지않게 파일명을 작성하고, 내부폴더에 복사하고, 중복되지 않는 이름을 List<String> 리턴
		List<String> uploadFileNames = fileUtil.saveFiles(files);
		// 업로드된 파일을 중복되지 않는 파일명리스트를 productDTO 저장한다.
		productDTO.setUploadFileNames(uploadFileNames);
		log.info(uploadFileNames);
		// 서비스 호출
		Long pno = productService.register(productDTO);
		return Map.of("result", pno);
	}

	@GetMapping("/view/{fileName}")
	public ResponseEntity<Resource> viewFileGET(@PathVariable String fileName) {
		return fileUtil.getFile(fileName);
	}

	@GetMapping("/list")
	public PageResponseDTO<ProductDTO> list(PageRequestDTO pageRequestDTO) {
		log.info("list............." + pageRequestDTO);
		return productService.getList(pageRequestDTO);
	}

	@GetMapping("/{pno}")
	public ProductDTO read(@PathVariable(name = "pno") Long pno) {
		return productService.get(pno);
	}

	@PutMapping("/{pno}")
	public Map<String, String> modify(@PathVariable(name = "pno") Long pno, ProductDTO productDTO) {
		productDTO.setPno(pno);

		// 현재 있는 파일의 정보 pno = 120L pname = "aaa" pprice = 10000 pdesc = "aaaa" pfile =
		// [] imgString = "aaaa.jpg"
		ProductDTO oldProductDTO = productService.get(pno);
		// 기존파일들 (데이터베이스에 존재하는 파일들-수정 과정에서 삭제되었을 수 있음)
		// aaaa.jpg
		List<String> oldFileNames = oldProductDTO.getUploadFileNames();

		// 새로 업로드 해야 하는 파일들(0, 0), (x, 0),(x, x), (0, x)
		// bbb.jpg
		// bbb.jpg
		List<MultipartFile> files = productDTO.getFiles();

		// 새로 업로드되어서 만들어진 파일 이름들
		// bbb.jpg 내부폴더에 저장하고, nrioqnroq_bbb.jpg 리턴
		// bbb.jpg 내부폴더에 저장하고, nrioqnroq2_bbb.jpg 리턴
		List<String> currentUploadFileNames = null;
		if (files != null && !files.isEmpty()) {
			currentUploadFileNames = fileUtil.saveFiles(files);
		}

		// 화면에서 변화 없이 계속 유지된 파일들
		// aaaa.jpg
		// aaaa.jpg
		List<String> uploadedFileNames = productDTO.getUploadFileNames();

		// 유지되는 파일들 + 새로 업로드된 파일 이름들이 저장해야 하는 파일 목록이 됨
		if (currentUploadFileNames != null && currentUploadFileNames.isEmpty()) {
			// {기존: aaaa.jpg, nrioqnroq_bbb.jpg 추가}
			// {기존: x, nrioqnroq2_bbb.jpg 추가}
			uploadedFileNames.addAll(currentUploadFileNames);
		}
		// 수정 작업
		productService.modify(productDTO);

		// aaaa.jpg
		if (oldFileNames != null && !oldFileNames.isEmpty()) {
			// 지워야 하는 파일 목록 찾기
			// 예전 파일들 중에서 지워져야 하는 파일이름들
			List<String> removeFiles = oldFileNames.stream()
					.filter(fileName -> uploadedFileNames.indexOf(fileName) == -1).collect(Collectors.toList());
			// 실제 파일 삭제
			fileUtil.deleteFiles(removeFiles);
		}
		return Map.of("RESULT", "SUCCESS");
	}

	@DeleteMapping("/{pno}")
	public Map<String, String> remove(@PathVariable("pno") Long pno) {
		// 삭제해야 할 파일들 알아내기 
		List<String> oldFileNames = productService.get(pno).getUploadFileNames();
		// 테이블 flag = true update
		productService.remove(pno);

		// 기존이미지는 삭제함.
		fileUtil.deleteFiles(oldFileNames);
		return Map.of("RESULT", "SUCCESS");
	}
}
