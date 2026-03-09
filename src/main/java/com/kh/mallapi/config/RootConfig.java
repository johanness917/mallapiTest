package com.kh.mallapi.config;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RootConfig {
	@Bean
	public ModelMapper getMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setFieldMatchingEnabled(true) // 이름이 일치할경우 매핑
				.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE) // 필드접근수준 private
				.setMatchingStrategy(MatchingStrategies.LOOSE); // 필드이름이 비슷하면 매핑 (홍길둥 => 홍길동)
		return modelMapper;
	}
}
