package prueba.finanzas.service.producto;

import prueba.finanzas.repository.ProductoRepository;
import prueba.finanzas.enums.TipoCuenta;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class GeneradorNumeroCuenta {

    private static final int LONGITUD_TOTAL = 10;
    private static final int INTENTOS_MAXIMOS = 20;

    private final ProductoRepository productoRepository;
    private final SecureRandom random = new SecureRandom();

    public String generar(TipoCuenta tipoCuenta) {
        String prefijo = prefijoPara(tipoCuenta);
        int digitosRestantes = LONGITUD_TOTAL - prefijo.length();

        for (int intento = 0; intento < INTENTOS_MAXIMOS; intento++) {
            String candidato = prefijo + generarDigitos(digitosRestantes);
            if (!productoRepository.existsByNumeroCuenta(candidato)) {
                return candidato;
            }
        }

        throw new IllegalStateException(
            "No fue posible generar un número de cuenta único tras " + INTENTOS_MAXIMOS + " intentos"
        );
    }

    private String prefijoPara(TipoCuenta tipoCuenta) {
        return switch (tipoCuenta) {
            case AHORROS -> "53";
            case CORRIENTE -> "33";
        };
    }

    private String generarDigitos(int cantidad) {
        StringBuilder sb = new StringBuilder(cantidad);
        for (int i = 0; i < cantidad; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
