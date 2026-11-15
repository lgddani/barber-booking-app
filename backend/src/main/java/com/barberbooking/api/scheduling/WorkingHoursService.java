package com.barberbooking.api.scheduling;

import com.barberbooking.api.common.BusinessRuleException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkingHoursService {

    private final WorkingHoursRepository workingHoursRepository;

    // 1.MÉTODO DE LECTURA
    public List<WorkingHoursItem> get(UUID barberId) {
        return workingHoursRepository.findByBarberIdOrderByDayOfWeekAscStartTimeAsc(barberId).stream()
                .map(WorkingHoursItem::from)
                .toList();
    }

    // 2.MÉTODO DE ACTUALIZACIÓN MASIVA
    @Transactional
    public List<WorkingHoursItem> replace(UUID barberId, List<WorkingHoursItem> items) {

        //Paso A:Validación de reglas de negocio
        for (WorkingHoursItem item : items) {
            if (!item.startTime().isBefore(item.endTime())) {
                throw new BusinessRuleException(
                        "El horario de inicio debe ser anterior al de fin (" + item.dayOfWeek() + ")"
                );
            }
        }

        //Paso B:Limpieza de la agenda anterior
        workingHoursRepository.deleteByBarberId(barberId);

        //paso C:Transformación de DTOs a Entidades
        List<WorkingHours> entities = items.stream().map(item -> {
            WorkingHours wh = new WorkingHours();
            wh.setBarberId(barberId);
            wh.setDayOfWeek(item.dayOfWeek());
            wh.setStartTime(item.startTime());
            wh.setEndTime(item.endTime());
            return wh;
        }).toList();

        //Paso D: Guardado en lote
        workingHoursRepository.saveAll(entities);

        //Paso E:Retorno de la foto actualizada
        return get(barberId);
    }
}