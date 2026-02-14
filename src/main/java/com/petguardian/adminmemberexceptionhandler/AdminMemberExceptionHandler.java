package com.petguardian.adminmemberexceptionhandler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice(basePackages = {"com.petguardian.admin", "com.petguardian.member"})
public class AdminMemberExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handle(Exception exception){
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				             .body("Exception: " + exception.getMessage()); 
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String,String>> handle(MethodArgumentNotValidException exception){
		
		Map<String,String> errors = new HashMap<>();
		
		exception.getBindingResult().getFieldErrors().forEach(error -> {
			String fieldName = error.getField();
			String errorMessage = error.getDefaultMessage();
			
		errors.putIfAbsent(fieldName, errorMessage);
		
		});
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Map<String, String>> handle(ConstraintViolationException exception){
		
		Map<String,String> errors = new HashMap<>();
		
		exception.getConstraintViolations().forEach(violation -> {
			
			String fieldName = violation.getPropertyPath().toString();
			
			fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
			
			String errorMessage = violation.getMessage();
			
			errors.putIfAbsent(fieldName, errorMessage);
			
		});
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);//400
		
	}
	
	
}