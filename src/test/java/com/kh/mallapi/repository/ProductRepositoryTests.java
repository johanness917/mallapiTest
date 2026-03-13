package com.kh.mallapi.repository;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.kh.mallapi.domain.Product;
import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;
import com.kh.mallapi.service.ProductService;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class ProductRepositoryTests {

	@Autowired
	ProductRepository productRepository;

	@Autowired
	ProductService productService;

	// @Test
	public void testInsert() {
		for (int i = 0; i < 10; i++) {
			// ProductDTO => Product
			Product product = Product.builder().pname("상품" + i).price(100 * i).pdesc("상품설명 " + i).build();
			// 2 개의 이미지 파일 추가
			product.addImageString(UUID.randomUUID().toString() + "-" + "IMAGE1.jpg");
			product.addImageString(UUID.randomUUID().toString() + "-" + "IMAGE2.jpg");
			productRepository.save(product);
			log.info(" ");
		}
	}

	// 상품정보 select(Lazy 방식)
	// @Transactional
	// @Test
	public void testRead() {
		// ProductDTO.pno
		Long pno = 1L;

		Optional<Product> result = productRepository.findById(pno);
		Product product = result.orElseThrow();
		log.info(product);
		log.info(product.getImageList());
	}

	// 상품정보 select(Eager 방식)
	// @Test
	public void testRead2() {
		Long pno = 1L;
		Optional<Product> result = productRepository.selectOne(pno);
		Product product = result.orElseThrow();
		log.info(product);
		log.info(product.getImageList());
	}

	// @Commit
	// @Transactional
	// @Test
	public void testDelete() {
		Long pno = 1L;
		productRepository.updateToDelete(pno, true);
	}

	// @Test
	public void testUpdate() {
		Long pno = 10L;
		Product product = productRepository.selectOne(pno).get();
		product.changeName("10번 상품수정");
		product.changeDesc("10번 상품 설명수정");
		product.changePrice(5000);
		// 저장된파일명을 모두삭제
		product.clearList();

		product.addImageString(UUID.randomUUID().toString() + "-" + "NEWIMAGE1.jpg");
		product.addImageString(UUID.randomUUID().toString() + "-" + "NEWIMAGE2.jpg");
		product.addImageString(UUID.randomUUID().toString() + "-" + "NEWIMAGE3.jpg");

		productRepository.save(product);
	}

	// @Test
	public void testList() {
		// org.springframework.data.domain 패키지
		int page = 1;
		Pageable pageable = PageRequest.of(page - 1, 5, Sort.by("pno").descending());

		Page<Object[]> result = productRepository.selectList(pageable);
		// java.util
		result.getContent().forEach(arr -> log.info(Arrays.toString(arr)));
	}

	// @Test
	public void testList2() {
		// 1 page, 10 size
		PageRequestDTO pageRequestDTO = PageRequestDTO.builder().build();
		PageResponseDTO<ProductDTO> result = productService.getList(pageRequestDTO);
		result.getDtoList().forEach(dto -> log.info(dto));
	}

	// @Test
	public void testRegister() {
		ProductDTO productDTO = ProductDTO.builder().pname("새로운 상품").pdesc("신규 추가 상품입니다.").price(1000).build();
		// uuid가 있어야함
		// List<String> listString = List.of("aaaa","bbbb");
		productDTO.setUploadFileNames(
				java.util.List.of(UUID.randomUUID() + "_" + "Test1.jpg", UUID.randomUUID() + "_" + "Test2.jpg"));
		productService.register(productDTO);
	}

	@Test
	public void testRead3() {
		// 실제 존재하는 번호로 테스트(DB에서 확인)
		Long pno = 9L;
		ProductDTO productDTO = productService.get(pno);
		log.info(productDTO);
		log.info(productDTO.getUploadFileNames());
	}
}
