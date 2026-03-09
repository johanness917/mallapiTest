package com.kh.mallapi.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mallapi.domain.Todo;
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
	//자동주입 대상은 final
	private final ModelMapper modelMapper; 
	private final TodoRepository todoRepository;
	
	@Override
	public Long register(TodoDTO todoDTO) {
		// TodoDTO = > Todo
		Todo todo = modelMapper.map(todoDTO, Todo.class);
		//TodoDTO todoDTO = modelMapper.map(todo, TodoDTO.class);
		Todo savedTodo = todoRepository.save(todo);
		return savedTodo.getTno();
	}
}
