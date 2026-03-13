package com.kh.mallapi.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mallapi.domain.Product;
import com.kh.mallapi.domain.ProductImage;
import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;
import com.kh.mallapi.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	@Override
	public PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO) {

		log.info("getList..........");

		// 1. 페이징 설정 (페이지 번호는 0부터 시작하므로 -1 처리)
		Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, pageRequestDTO.getSize(),
				Sort.by("pno").descending());

		// 2. Repository를 통해 데이터 조회 (Object[] 형태의 Page 객체)
		Page<Object[]> result = productRepository.selectList(pageable);

		// 3. Entity 리스트를 DTO 리스트로 변환
		List<ProductDTO> dtoList = result.get().map(arr -> {
			Product product = (Product) arr[0];
			ProductImage productImage = (ProductImage) arr[1];

			ProductDTO productDTO = ProductDTO.builder().pno(product.getPno()).pname(product.getPname())
					.pdesc(product.getPdesc()).price(product.getPrice()).build();

			// 대표 이미지 설정
			String imageStr = productImage.getFileName();
			productDTO.setUploadFileNames(List.of(imageStr));

			return productDTO;
		}).collect(Collectors.toList());

		// 4. 전체 데이터 개수 파악
		long totalCount = result.getTotalElements();

		// 5. 최종 결과 반환
		return PageResponseDTO.<ProductDTO>withAll().dtoList(dtoList).totalCount(totalCount)
				.pageRequestDTO(pageRequestDTO).build();
	}

	@Override
	public Long register(ProductDTO productDTO) {
		Product product = dtoToEntity(productDTO);
		Product result = productRepository.save(product);
		return result.getPno();
	}

	@Override
	public ProductDTO get(Long pno) {
		java.util.Optional<Product> result = productRepository.selectOne(pno);
		Product product = result.orElseThrow();

		ProductDTO productDTO = entityToDTO(product);
		return productDTO;
	}

	// 영속성도메인 => DTO로 변형
	private ProductDTO entityToDTO(Product product) {
		ProductDTO productDTO = ProductDTO.builder().pno(product.getPno()).pname(product.getPname())
				.pdesc(product.getPdesc()).price(product.getPrice()).build();

		List<ProductImage> imageList = product.getImageList();

		if (imageList == null || imageList.size() == 0) {
			return productDTO;
		}

		List<String> fileNameList = imageList.stream().map(productImage -> productImage.getFileName()).toList();

		productDTO.setUploadFileNames(fileNameList);
		return productDTO;
	}

	// DTO => 영속석 entity
	private Product dtoToEntity(ProductDTO productDTO) {
		Product product = Product.builder().pno(productDTO.getPno()).pname(productDTO.getPname())
				.pdesc(productDTO.getPdesc()).price(productDTO.getPrice()).build();
		// 업로드 처리가 끝난 파일들의 이름 리스트
		List<String> uploadFileNames = productDTO.getUploadFileNames();
		if (uploadFileNames == null) {
			return product;
		}
		uploadFileNames.stream().forEach(uploadName -> {
			product.addImageString(uploadName);
		});

		return product;
	}

	@Override
	public void modify(ProductDTO productDTO) {
		Optional<Product> result = productRepository.findById(productDTO.getPno());
		Product product = result.orElseThrow();
		// change pname, pdesc, price
		product.changeName(productDTO.getPname());
		product.changeDesc(productDTO.getPdesc());
		product.changePrice(productDTO.getPrice());

		// 기존의 이미지 파일명을 모두 삭제한다.
		product.clearList();
		
		// 새로 업로드된 파일을 내부폴더에 중복되지않는 파일명으로 저장하고, 저장된 이름을 리스트로 가져온다.
		List<String> uploadFileNames = productDTO.getUploadFileNames();
		
		if (uploadFileNames != null && !uploadFileNames.isEmpty()) {
			uploadFileNames.stream().forEach(uploadName -> {
				product.addImageString(uploadName);
			});
		}
		productRepository.save(product);

	}

	@Override
	public void remove(Long pno) {
		 productRepository.updateToDelete(pno, true); 
		
	}
}