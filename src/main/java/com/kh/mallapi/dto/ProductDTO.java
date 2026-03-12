package com.kh.mallapi.dto;

import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
	private Long pno;
	private String pname;
	private int price;
	private String pdesc;
	
	@Builder.Default
	private List<MultipartFile> files = new ArrayList<MultipartFile>();
	
	@Builder.Default
	private List<String> uploadFileNames = new ArrayList<String>();
}
