package com.NotificationSystem.repositories;

import com.NotificationSystem.entities.Collection;
import com.NotificationSystem.entities.Notification;
import com.NotificationSystem.entities.Schedule;
import com.NotificationSystem.entities.Truck;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends Repository<Schedule, Long> {

    interface ScheduleStatusCount {
        Schedule.Status getStatus();
        Long getTotal();
    }
    interface MonthCount {
        Integer getYr();
        Integer getMo();
        Long getTotal();
        Long getCompleted();
    }
    interface ZoneScheduleCount {
        Long getZoneId();
        Schedule.Status getStatus();
        Long getTotal();
    }
    interface ZoneInfo {
        Long getId();
        String getName();
        String getCollectionDay();
    }
    interface ZoneResidentCount {
        Long getZoneId();
        Long getTotal();
    }
    interface NotificationStatusCount {
        Notification.Status getStatus();
        Long getTotal();
    }
    interface NotificationChannelCount {
        Notification.Channel getChannel();
        Long getTotal();
    }
    interface FailureCount {
        String getDetail();
        Long getTotal();
    }
    interface CollectionStatusCount {
        Collection.Status getStatus();
        Long getTotal();
    }
    interface ZoneCollectionCount {
        Long getZoneId();
        Collection.Status getStatus();
        Long getTotal();
    }
    interface TruckInfo {
        Long getId();
        String getPlateNumber();
        String getDriverName();
        Truck.Status getStatus();
    }
    interface TruckScheduleCount {
        Long getTruckId();
        Schedule.Status getStatus();
        Long getTotal();
    }
    @Query("select s.status as status, count(s) as total from Schedule s " +
            "where s.collectionDate between :from and :to " +
            "group by s.status")
    List<ScheduleStatusCount> schedulesByStatus(@Param("from") LocalDate from,
                                                @Param("to") LocalDate to);


    // Cancelled schedules are left out so the trend shows real workload.
    @Query("select extract(year from s.collectionDate) as yr, " +
            "extract(month from s.collectionDate) as mo, " +
            "count(s) as total, " +
            "sum(case when s.status = :doneStatus then 1 else 0 end) as completed " +
            "from Schedule s " +
            "where s.collectionDate between :from and :to and s.status <> :cancelStatus " +
            "group by extract(year from s.collectionDate), extract(month from s.collectionDate)")
    List<MonthCount> schedulesByMonth(@Param("from") LocalDate from,
                                      @Param("to") LocalDate to,
                                      @Param("doneStatus") Schedule.Status doneStatus,
                                      @Param("cancelStatus") Schedule.Status cancelStatus);

    @Query("select s.zone.id as zoneId, s.status as status, count(s) as total from Schedule s " +
            "where s.collectionDate between :from and :to " +
            "group by s.zone.id, s.status")
    List<ZoneScheduleCount> schedulesByZone(@Param("from") LocalDate from,
                                            @Param("to") LocalDate to);

    // Notifications

    @Query("select n.status as status, count(n) as total from Notification n " +
            "where n.createdAt >= :start and n.createdAt < :end " +
            "group by n.status")
    List<NotificationStatusCount> notificationsByStatus(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end);

    @Query("select n.channel as channel, count(n) as total from Notification n " +
            "where n.createdAt >= :start and n.createdAt < :end " +
            "group by n.channel")
    List<NotificationChannelCount> notificationsByChannel(@Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end);

    @Query("select n.errorDetail as detail, count(n) as total from Notification n " +
            "where n.status = :failedStatus and n.errorDetail is not null " +
            "and n.createdAt >= :start and n.createdAt < :end " +
            "group by n.errorDetail order by count(n) desc")
    List<FailureCount> notificationFailures(@Param("failedStatus") Notification.Status failedStatus,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);


    // Collections (per-resident pickup records)

    @Query("select c.status as status, count(c) as total " +
            "from com.NotificationSystem.entities.Collection c, Schedule s " +
            "where s.id = c.scheduleId and s.collectionDate between :from and :to " +
            "group by c.status")
    List<CollectionStatusCount> collectionsByStatus(@Param("from") LocalDate from,
                                                    @Param("to") LocalDate to);

    @Query("select s.zone.id as zoneId, c.status as status, count(c) as total " +
            "from com.NotificationSystem.entities.Collection c, Schedule s " +
            "where s.id = c.scheduleId and s.collectionDate between :from and :to " +
            "group by s.zone.id, c.status")
    List<ZoneCollectionCount> collectionsByZone(@Param("from") LocalDate from,
                                                @Param("to") LocalDate to);


    // Zones and residents (coverage)

    @Query("select z.id as id, z.name as name, z.collectionDay as collectionDay " +
            "from Zone z order by z.name")
    List<ZoneInfo> zoneList();

    @Query("select r.zone.id as zoneId, count(r) as total from Resident r group by r.zone.id")
    List<ZoneResidentCount> residentsPerZone();

    @Query("select count(r) from Resident r")
    Long countResidents();

    @Query("select count(z) from Zone z")
    Long countZones();

    @Query("select count(z) from Zone z where z.collectionDay is null or z.collectionDay = ''")
    Long countZonesWithoutDay();


    // Trucks

    @Query("select t.id as id, t.plateNumber as plateNumber, t.driverName as driverName, " +
            "t.status as status from Truck t order by t.plateNumber")
    List<TruckInfo> truckList();

    @Query("select s.truck.id as truckId, s.status as status, count(s) as total from Schedule s " +
            "where s.truck is not null and s.collectionDate between :from and :to " +
            "group by s.truck.id, s.status")
    List<TruckScheduleCount> schedulesByTruck(@Param("from") LocalDate from,
                                              @Param("to") LocalDate to);

    @Query("select count(s) from Schedule s " +
            "where s.truck is null and s.status <> :cancelStatus " +
            "and s.collectionDate between :from and :to")
    Long countUnassignedSchedules(@Param("from") LocalDate from,
                                  @Param("to") LocalDate to,
                                  @Param("cancelStatus") Schedule.Status cancelStatus);
}

