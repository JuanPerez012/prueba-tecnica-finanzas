package prueba.finanzas.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<String> detalles
) {
    public ErrorResponse(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> detalles) {
        this(LocalDateTime.now(), status, error, message, path, detalles);
    }
}
