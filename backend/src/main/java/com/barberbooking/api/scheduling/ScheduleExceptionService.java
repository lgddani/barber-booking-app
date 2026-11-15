package com.barberbooking.api.scheduling;

import com.barberbooking.api.common.BusinessRuleException;
import com.barberbooking.api.common.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleExceptionService {

    private final ScheduleExceptionRepository scheduleExceptionRepository;

    // 1. LISTADO CON FILTRO OPCIONAL POR FECHAS
    public List<ScheduleExceptionResponse> list(UUID barberId, LocalDate from, LocalDate to) {
        var exceptions = (from != null && to != null)
                ? scheduleExceptionRepository.findByBarberIdAndDateBetweenOrderByDateAsc(barberId, from, to)
                : scheduleExceptionRepository.findByBarberIdOrderByDateAsc(barberId);
        return exceptions.stream().map(ScheduleExceptionResponse::from).toList();
    }

    // 2. CREACIÓN CON VALIDACIÓN DE DOMINIO
    @Transactional
    public ScheduleExceptionResponse create(UUID barberId, ScheduleExceptionRequest request) {
        validate(request);

        ScheduleException exception = new ScheduleException();
        exception.setBarberId(barberId);
        exception.setDate(request.date());
        exception.setType(request.type());
        exception.setStartTime(request.startTime());
        exception.setEndTime(request.endTime());
        scheduleExceptionRepository.save(exception);

        return ScheduleExceptionResponse.from(exception);
    }

    // 3. ELIMINACIÓN CON DOBLE VERIFICACIÓN DE PERTENENCIA (Anti-IDOR)
    @Transactional
    public void delete(UUID barberId, UUID exceptionId) {
        ScheduleException exception = scheduleExceptionRepository.findById(exceptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Excepción no encontrada: " + exceptionId));

        //Verificar que la excepción realmente pertenezca al barbero de la URL
        if (!exception.getBarberId().equals(barberId)) {
            throw new ResourceNotFoundException("Excepción no encontrada: " + exceptionId);
        }

        scheduleExceptionRepository.delete(exception);
    }

    // 4. REGLAS DE NEGOCIO SEGÚN EL TIPO DE EXCEPCIÓN
    private void validate(ScheduleExceptionRequest request) {
        if (request.type() == ExceptionType.CLOSED) {
            if (request.startTime() != null || request.endTime() != null) {
                throw new BusinessRuleException("Un día CERRADO no debe tener horas de inicio/fin");
            }
        } else {
            if (request.startTime() == null || request.endTime() == null) {
                throw new BusinessRuleException("CUSTOM_HOURS requiere horas de inicio y fin");
            }
            if (!request.startTime().isBefore(request.endTime())) {
                throw new BusinessRuleException("La hora de inicio debe ser antes que la de fin");
            }
        }
    }
}