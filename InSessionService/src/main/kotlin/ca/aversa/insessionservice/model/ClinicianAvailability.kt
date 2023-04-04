package ca.aversa.insessionservice.model

import ca.aversa.insessionservice.model.entity.AvailabilityDocument
import ca.aversa.insessionservice.model.request.ClinicianUpdateAvailabilityRequest
import java.time.LocalTime

data class ClinicianAvailability(

    val availabilities: Map<Day, Availability> = mutableMapOf(
        Day.MONDAY to Availability.Default.weekday(),
        Day.TUESDAY to Availability.Default.weekday(),
        Day.WEDNESDAY to Availability.Default.weekday(),
        Day.THURSDAY to Availability.Default.weekday(),
        Day.FRIDAY to Availability.Default.weekday(),
        Day.SATURDAY to Availability.Default.weekend(),
        Day.SUNDAY to Availability.Default.weekend()
    )
) {

    fun toEntityAvailabilities(): Map<String, AvailabilityDocument>{
        return availabilities.entries.associate { entry ->
            entry.key.name to entry.value.toEntity()
        }
    }

    object Mapper {
        fun from(request: ClinicianUpdateAvailabilityRequest): ClinicianAvailability {
            return ClinicianAvailability(request.availabilities)
        }

        fun from(clinicianAvailabilities: Map<String, AvailabilityDocument>): ClinicianAvailability {
            val availabilities = clinicianAvailabilities.entries.associate { entry ->
                Day.valueOf(entry.key) to Availability.Mapper.from(entry.value)
            }

            return ClinicianAvailability(availabilities)
        }
    }
}

class Availability(

    val startTime: LocalTime?,
    val endTime: LocalTime?
) {

    fun toEntity(): AvailabilityDocument {
        return AvailabilityDocument(startTime, endTime)
    }

    object Default {
        fun weekday(): Availability {
            return Availability(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
            )
        }

        fun weekend(): Availability {
            return Availability(null, null)
        }
    }

    object Mapper {
        fun from(availabilityDocument: AvailabilityDocument): Availability {
            return Availability(availabilityDocument.startTime, availabilityDocument.endTime)
        }
    }
}
