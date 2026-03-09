package com.kh.mallapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kh.mallapi.domain.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {

}
