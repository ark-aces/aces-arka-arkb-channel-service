package com.arkaces.arka_arkb_channel_service.transfer;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;


@Transactional
public interface TransferRepository extends PagingAndSortingRepository<TransferEntity, Long> {

    TransferEntity findOneByArkaTransactionId(String arkaTransactionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransferEntity t where t.pid = :pid")
    TransferEntity findOneForUpdate(@Param("pid") Long pid);
}
