package com.kh.mallapi.service;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.TodoDTO;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class TodoServiceTests {
	@Autowired
	private TodoService todoService;

	// @Test
	public void testRegister() {
		TodoDTO todoDTO = TodoDTO.builder().title("서비스 테스트").writer("tester").dueDate(LocalDate.of(2023, 11, 20))
				.build();
		Long tno = todoService.register(todoDTO);
		log.info("TNO: " + tno);
	}

	// @Test
	public void testGet() {
		Long tno = 101L;
		TodoDTO todoDTO = todoService.get(tno);
		log.info(todoDTO);
	}

	// @Test
	public void testModify() {
		TodoDTO todoDTO = TodoDTO.builder().tno(101L).title("사용자 수정").writer("tester").complete(true)
				.dueDate(LocalDate.of(2026, 3, 9)).build();
		todoService.modify(todoDTO);

	}

	// @Test
	public void testRemove() {
		Long tno = 101L;
		todoService.remove(tno);
	}

	@Test
	public void testList() {
		PageRequestDTO pageRequestDTO = PageRequestDTO.builder().page(2).size(10).build();
		PageResponseDTO<TodoDTO> response = todoService.list(pageRequestDTO);
		log.info(response);
	}
}
