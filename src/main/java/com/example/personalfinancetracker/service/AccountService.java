package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.dto.AccountDTO;
import com.example.personalfinancetracker.model.Account;
import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.repository.AccountRepository;
import com.example.personalfinancetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository; // To fetch the User entity

    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Account createAccount(Long userId, AccountDTO accountDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Account account = new Account();
        account.setUser(user);
        account.setAccountName(accountDTO.getAccountName());
        account.setAccountType(accountDTO.getAccountType());
        account.setBalance(accountDTO.getBalance() != null ? accountDTO.getBalance() : BigDecimal.ZERO);
        account.setCurrency(accountDTO.getCurrency() != null ? accountDTO.getCurrency() : "USD");

        return accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(Long userId, Long accountId, AccountDTO accountDTO) {
        Account existingAccount = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or does not belong to user."));

        existingAccount.setAccountName(accountDTO.getAccountName());
        existingAccount.setAccountType(accountDTO.getAccountType());
        existingAccount.setCurrency(accountDTO.getCurrency());
        // Do NOT update balance directly here as it should be managed by transactions
        return accountRepository.save(existingAccount);
    }

    public List<AccountDTO> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<AccountDTO> getAccountByIdAndUserId(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .map(this::convertToDto);
    }

    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        Account accountToDelete = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or does not belong to user."));
        accountRepository.delete(accountToDelete);
    }

    // Helper method to convert Entity to DTO
    private AccountDTO convertToDto(Account account) {
        return new AccountDTO(
                account.getId(),
                account.getAccountName(),
                account.getAccountType(),
                account.getBalance(),
                account.getCurrency()
        );
    }
}
