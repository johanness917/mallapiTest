package com.kh.mallapi.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mallapi.domain.Todo;
import com.kh.mallapi.dto.PageRequestDTO;
import com.kh.mallapi.dto.PageResponseDTO;
import com.kh.mallapi.dto.TodoDTO;
import com.kh.mallapi.repository.TodoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@Log4j2
// 생성자 의존성 주입
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
	// 자동주입 대상은 final
	private final ModelMapper modelMapper;
	private final TodoRepository todoRepository;

	@Override
	public Long register(TodoDTO todoDTO) {
		// TodoDTO = > Todo
		Todo todo = modelMapper.map(todoDTO, Todo.class);
		// TodoDTO todoDTO = modelMapper.map(todo, TodoDTO.class);
		Todo savedTodo = todoRepository.save(todo);
		return savedTodo.getTno();
	}

	@Override
	public TodoDTO get(Long tno) {
		java.util.Optional<Todo> result = todoRepository.findById(tno);
		Todo todo = result.orElseThrow();

		TodoDTO dto = modelMapper.map(todo, TodoDTO.class);
		return dto;
	}

	@Override
	public void modify(TodoDTO todoDTO) {
		java.util.Optional<Todo> result = todoRepository.findById(todoDTO.getTno());
		Todo todo = result.orElseThrow();

		todo.changeTitle(todoDTO.getTitle());
		todo.changeDueDate(todoDTO.getDueDate());
		todo.changeComplete(todoDTO.isComplete());

		todoRepository.save(todo);
	}

	@Override
	public void remove(Long tno) {
		todoRepository.deleteById(tno);
	}

	@Override
	public PageResponseDTO<TodoDTO> list(PageRequestDTO pageRequestDTO) {
		Pageable pageable = PageRequest.of(pageRequestDTO.getPage() - 1, // 1 페이지가 0 이므로 주의
				pageRequestDTO.getSize(), Sort.by("tno").descending());
		// 1페이지 해당되는 레코드 10개를 가져온다. => findAll(pageable);
		Page<Todo> result = todoRepository.findAll(pageable);
		// 1페이지에 해당되는 10개 레코드를 가져온다.
		List<TodoDTO> dtoList = result.getContent().stream().map(todo -> modelMapper.map(todo, TodoDTO.class))
				.collect(Collectors.toList());
		// 전체레코드수를 구함
		long totalCount = result.getTotalElements();
		PageResponseDTO<TodoDTO> responseDTO = PageResponseDTO.<TodoDTO>withAll().dtoList(dtoList)
				.pageRequestDTO(pageRequestDTO).totalCount(totalCount).build();
		return responseDTO;
	}
}
