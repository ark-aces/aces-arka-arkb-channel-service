package com.arkaces.arka_arkb_channel_service.service_capacity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;

public interface ServiceCapacityRepository extends JpaRepository<ServiceCapacityEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ServiceCapacityEntity s where s.pid = :pid")
    ServiceCapacityEntity findOneForUpdate(@Param("pid") Long pid);
    
}