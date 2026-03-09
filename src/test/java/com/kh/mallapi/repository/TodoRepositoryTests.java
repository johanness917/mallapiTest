package com.kh.mallapi.repository;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.kh.mallapi.domain.Todo;

import lombok.extern.log4j.Log4j2;

@SpringBootTest
@Log4j2
public class TodoRepositoryTests {
	@Autowired
	private TodoRepository todoRepository;

	// @Test
	public void test1() {
		log.info("===============");
		log.info(todoRepository);
		log.info("===============");
	}

	// @Test
	// insert == repository.save(Entity)
	public void testInsert() {
		for (int i = 1; i <= 100; i++) {
			Todo todo = Todo.builder().title("Title..." + i).dueDate(LocalDate.of(2026, 3, 9)).writer("user00").build();
			// Todo todo1 = new Todo();
			// Todo todo2 = new Todo(null, null, null, false, null);
			// jpa: save(Entity)
			// insert into tbl_todo value(todo_seq.nextval(), 'tetle...1', false, 'user00',
			// '2006 .3. 9')
			todoRepository.save(todo);
		}
	}

	// @Test
	// select * from tbl_todo where tno 33 == repository.findById(33L);
	public void testRead() {
		// 존재하는 번호로 확인
		Long tno = 33L;
		java.util.Optional<Todo> result = todoRepository.findById(tno);
		// 에러가 발생하면 NoSuchElementException
		Todo todo = result.orElseThrow();
		log.info(todo);
	}

	// @Test
	// update => 1. repository.findById(33L) 2.해당컬컴을 setter
	// 3.repository.save(Entity)
	public void testModify() {
		Long tno = 33L;
		java.util.Optional<Todo> result = todoRepository.findById(tno);
		Todo todo = result.orElseThrow();
		todo.changeTitle("Modified 33...");
		todo.changeComplete(true);
		todo.changeDueDate(LocalDate.of(2023, 10, 10));
		todoRepository.save(todo);
	}

	//@Test
	// delete from tble_todo where tno = 1 === repository.deleteById(1L)
	public void testDelete() {
		Long tno = 1L;
		todoRepository.deleteById(tno);
	}

	@Test
	//Pageable pageable = PageRequest.of(시작페이지-1, 페이지당보여주는라인수, 정렬기준);
	// 페이징기법: todoRepository.findAll(pageable);
	public void testPaging() {
		// 0번째 페이지요청(페이지 인덱스는 0부터 시작), 한 페이지에 10개의 데이터,
		// 정렬기준은 tno 필드를 기준으로 내림차순
		Pageable pageable = PageRequest.of(2-1, 5, Sort.by("tno").descending());
		// Page<Todo>타입 반환되며, 전체 정보(총 개수, 현재 페이지 등)가 포함
		//List<Todo> : 1페이지에 해당되는 10 레코드수가 들어있다.
		Page<Todo> result = todoRepository.findAll(pageable);
		// 전체 데이터 개수(전체 Todo 엔티티 수)를 로그로 출력
		log.info(result.getTotalElements());
		// 현재 페이지(0페이지)에 포함된 Todo 목록을 가져온다.
		result.getContent().stream().forEach(todo -> log.info(todo));
	}
}
