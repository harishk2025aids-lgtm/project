package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalLineRepository extends JpaRepository<JournalLine, Long> {
}
