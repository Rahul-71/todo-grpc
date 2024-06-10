package com.grpcdemo.grpc_todo_app.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grpcdemo.grpc_todo_app.model.TodoEntity;
import com.grpcdemo.grpc_todo_app.repository.TodoRepository;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import todo.TodoOuterClass.CreateTodoRequest;
import todo.TodoOuterClass.DeleteTodoRequest;
import todo.TodoOuterClass.DeleteTodoResponse;
import todo.TodoOuterClass.GetTodoByIdRequest;
import todo.TodoOuterClass.GetTodosRequest;
import todo.TodoOuterClass.GetTodosResponse;
import todo.TodoOuterClass.Todo;
import todo.TodoOuterClass.UpdateTodoRequest;
import todo.TodoServiceGrpc.TodoServiceImplBase;

@GrpcService
public class TodoService extends TodoServiceImplBase {

    @Autowired
    private TodoRepository repository;

    @Override
    public void createTodo(CreateTodoRequest request, StreamObserver<Todo> responseObserver) {

        TodoEntity todo = new TodoEntity();
        todo.setDescription(request.getDescription());
        todo.setCompletionStatus(request.getCompletionStatus());

        TodoEntity response = this.repository.save(todo);
System.out.println("Todo Created ...");
System.out.println(response.toString());
        responseObserver.onNext(convertToTodo(response));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTodo(DeleteTodoRequest request, StreamObserver<DeleteTodoResponse> responseObserver) {
        int todoId = request.getId();
        boolean isTodoExist = this.repository.existsById(todoId);
System.out.println("Todo Deletion ...");

        DeleteTodoResponse deleteResponse = null;
        if (isTodoExist) {
            this.repository.deleteById(todoId);
            deleteResponse = DeleteTodoResponse.newBuilder()
                    .setSuccess("true")
                    .build();
System.out.println("Success....");
        } else {
            deleteResponse = DeleteTodoResponse.newBuilder()
                    .setSuccess("false")
                    .build();
System.out.println("Failed....");
        }

        responseObserver.onNext(deleteResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void getTodoById(GetTodoByIdRequest request, StreamObserver<Todo> responseObserver) {
        int todoId = request.getId();
        Optional<TodoEntity> todoOptional = this.repository.findById(todoId);

        if (todoOptional.isPresent()) {
            TodoEntity existingTodo = todoOptional.get();
System.out.println("TodoById fetched ...");
System.out.println(existingTodo.toString());
            // Todo todoRes = Todo.newBuilder().build();
            // BeanUtils.copyProperties(existingTodo, todoRes);
            responseObserver.onNext(convertToTodo(existingTodo));
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Todo with id " + todoId + " not found")
                            .asRuntimeException());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getTodos(GetTodosRequest request, StreamObserver<GetTodosResponse> responseObserver) {

        List<TodoEntity> todosEntities = this.repository.findAll();

        List<Todo> todos = todosEntities.stream().map(this::convertToTodo).collect(Collectors.toList());
System.out.println("Todos Fetched ...");
System.out.println(todos);
        GetTodosResponse allTodosResponse = GetTodosResponse.newBuilder()
                .addAllTodos(todos)
                .build();
        responseObserver.onNext(allTodosResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void updateTodo(UpdateTodoRequest request, StreamObserver<Todo> responseObserver) {
        int todoId = request.getId();
        Optional<TodoEntity> todoOptional = this.repository.findById(todoId);

        if (todoOptional.isPresent()) {
            TodoEntity existingTodo = todoOptional.get();

            existingTodo.setDescription(request.getDescription());
            existingTodo.setCompletionStatus(request.getCompletionStatus());

            TodoEntity updatedTodoResponse = this.repository.save(existingTodo);
System.out.println("Todo Updated ...");
System.out.println(updatedTodoResponse.toString());
            responseObserver.onNext(convertToTodo(updatedTodoResponse));
        } else {
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("Todo with id " + todoId + " not found")
                            .asRuntimeException());
        }
        responseObserver.onCompleted();

    }

    private Todo convertToTodo(TodoEntity todoentity) {
        return Todo.newBuilder()
                .setId(todoentity.getId())
                .setDescription(todoentity.getDescription())
                .setCompletionStatus(todoentity.getCompletionStatus())
                .build();
    }

}
