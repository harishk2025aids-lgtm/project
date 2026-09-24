package com.ruralhealth.platform.repository;

import com.ruralhealth.platform.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
}
