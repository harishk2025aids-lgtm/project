package com.ruralhealth.platform.service;

import com.ruralhealth.platform.entity.ChartOfAccount;
import com.ruralhealth.platform.entity.JournalEntry;
import com.ruralhealth.platform.entity.JournalLine;
import com.ruralhealth.platform.repository.ChartOfAccountRepository;
import com.ruralhealth.platform.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** Helper for posting simple double-entry journal entries (debit/credit pairs). */
@Service
public class JournalService {

    private final JournalEntryRepository journalEntryRepository;
    private final ChartOfAccountRepository chartOfAccountRepository;

    public JournalService(JournalEntryRepository journalEntryRepository,
                           ChartOfAccountRepository chartOfAccountRepository) {
        this.journalEntryRepository = journalEntryRepository;
        this.chartOfAccountRepository = chartOfAccountRepository;
    }

    /** Posts a balanced two-line journal entry: debit one account, credit another. */
    public JournalEntry postEntry(String description, String referenceType, Long referenceId,
                                   String debitAccountCode, String creditAccountCode, BigDecimal amount) {
        ChartOfAccount debitAccount = chartOfAccountRepository.findByAccountCode(debitAccountCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown account code: " + debitAccountCode));
        ChartOfAccount creditAccount = chartOfAccountRepository.findByAccountCode(creditAccountCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown account code: " + creditAccountCode));

        JournalEntry entry = new JournalEntry();
        entry.setDescription(description);
        entry.setReferenceType(referenceType);
        entry.setReferenceId(referenceId);

        JournalLine debitLine = new JournalLine();
        debitLine.setJournalEntry(entry);
        debitLine.setAccount(debitAccount);
        debitLine.setDebitAmount(amount);
        debitLine.setCreditAmount(BigDecimal.ZERO);

        JournalLine creditLine = new JournalLine();
        creditLine.setJournalEntry(entry);
        creditLine.setAccount(creditAccount);
        creditLine.setDebitAmount(BigDecimal.ZERO);
        creditLine.setCreditAmount(amount);

        entry.getLines().add(debitLine);
        entry.getLines().add(creditLine);

        return journalEntryRepository.save(entry);
    }
}
