package com.example.bank_backend.exception;

import com.example.bank_backend.model.LegalForm;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Обработчик для ошибок JSON десериализации (enum и др.)
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiError> handleInvalidFormatException(InvalidFormatException ex, WebRequest request) {
        String errorMessage = "Некорректный формат данных";

        if (ex.getTargetType() != null && ex.getTargetType().isEnum()) {
            Object[] enumConstants = ex.getTargetType().getEnumConstants();
            String allowedValues = Arrays.stream(enumConstants)
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            errorMessage = String.format("Некорректная юридическая форма '%s'. Допустимые значения: %s",
                    ex.getValue(), allowedValues);
        }

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Некорректный запрос",
                errorMessage,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(NoClientsFoundException.class)
    public ResponseEntity<ApiError> handleNoClients(NoClientsFoundException ex, WebRequest request) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),  // Важно: 404, а не 500
                "Ресурс не найден",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Обработчик для общего случая нечитаемого сообщения
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, WebRequest request) {
        String errorMessage = "Некорректный формат JSON тела запроса";

        // Если вложенное исключение - InvalidFormatException, делегируем его обработчику
        if (ex.getCause() instanceof InvalidFormatException) {
            return handleInvalidFormatException((InvalidFormatException) ex.getCause(), request);
        }

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Некорректный запрос",
                errorMessage,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации",
                errorMessage,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({NumberFormatException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleConversionExceptions(Exception ex, WebRequest request) {
        String errorMessage = "Некорректный формат параметра";

        if (ex instanceof MethodArgumentTypeMismatchException mismatchEx) {
            if (mismatchEx.getRequiredType() == LegalForm.class) {
                errorMessage = "Некорректная юридическая форма. Допустимые значения: ООО, АО, ПТ, КТ, ПК, ИП, ПАО, ГУП, МУП";
            }
        }

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Некорректный запрос",
                errorMessage,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ApiError> handleClientNotFound(ClientNotFoundException ex, WebRequest request) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Ресурс не найден",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, WebRequest request) {
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Ошибка на стороне сервера",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}