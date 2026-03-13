package com.kh.mallapi.service;

import org.springframework.transaction.annotation.Transactional;

import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.ProductDTO;

@Transactional
public interface ProductService {
	public PageResponseDTO<ProductDTO> getList(PageRequestDTO pageRequestDTO);
	public Long register(ProductDTO productDTO); 
	public ProductDTO get(Long pno); 
	public void modify(ProductDTO productDTO);
	public void remove(Long pno); 
}
