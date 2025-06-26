package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.dto.TransactionDTO;
import com.example.personalfinancetracker.model.Account;
import com.example.personalfinancetracker.model.Category;
import com.example.personalfinancetracker.model.Transaction;
import com.example.personalfinancetracker.model.User;
import com.example.personalfinancetracker.repository.AccountRepository;
import com.example.personalfinancetracker.repository.CategoryRepository;
import com.example.personalfinancetracker.repository.TransactionRepository;
import com.example.personalfinancetracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository,
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Transaction createTransaction(Long userId, TransactionDTO transactionDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Account account = accountRepository.findByIdAndUserId(transactionDTO.getAccountId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found or does not belong to user."));

        Category category = categoryRepository.findByIdAndUserId(transactionDTO.getCategoryId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found or does not belong to user."));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setTransactionDate(transactionDTO.getTransactionDate());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTransactionType(transactionDTO.getTransactionType());
        transaction.setDescription(transactionDTO.getDescription());

        // Update account balance
        updateAccountBalance(account, transaction.getAmount(), transaction.getTransactionType(), "add");

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction updateTransaction(Long userId, Long transactionId, TransactionDTO transactionDTO) {
        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (!existingTransaction.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Transaction does not belong to the authenticated user.");
        }

        // Revert old transaction's impact on account balance
        updateAccountBalance(existingTransaction.getAccount(), existingTransaction.getAmount(), existingTransaction.getTransactionType(), "subtract");

        // Update transaction details
        Account newAccount = accountRepository.findByIdAndUserId(transactionDTO.getAccountId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("New account not found or does not belong to user."));
        Category newCategory = categoryRepository.findByIdAndUserId(transactionDTO.getCategoryId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("New category not found or does not belong to user."));

        existingTransaction.setAccount(newAccount);
        existingTransaction.setCategory(newCategory);
        existingTransaction.setTransactionDate(transactionDTO.getTransactionDate());
        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setTransactionType(transactionDTO.getTransactionType());
        existingTransaction.setDescription(transactionDTO.getDescription());

        // Apply new transaction's impact on account balance
        updateAccountBalance(newAccount, existingTransaction.getAmount(), existingTransaction.getTransactionType(), "add");

        return transactionRepository.save(existingTransaction);
    }

    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transactionToDelete = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));

        if (!transactionToDelete.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Transaction does not belong to the authenticated user.");
        }

        // Revert transaction's impact on account balance
        updateAccountBalance(transactionToDelete.getAccount(), transactionToDelete.getAmount(), transactionToDelete.getTransactionType(), "subtract");

        transactionRepository.delete(transactionToDelete);
    }

    public List<TransactionDTO> getTransactionsByUserId(Long userId) {
        return transactionRepository.findByUserIdOrderByTransactionDateDesc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<TransactionDTO> getTransactionByIdAndUserId(Long transactionId, Long userId) {
        return transactionRepository.findById(transactionId)
                .filter(t -> t.getUser().getId().equals(userId))
                .map(this::convertToDto);
    }

    public List<TransactionDTO> getTransactionsByAccountId(Long userId, Long accountId) {
        return transactionRepository.findByAccountIdAndUserIdOrderByTransactionDateDesc(accountId, userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<TransactionDTO> getTransactionsByCategoryId(Long userId, Long categoryId) {
        return transactionRepository.findByCategoryIdAndUserIdOrderByTransactionDateDesc(categoryId, userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void updateAccountBalance(Account account, BigDecimal amount, String type, String operation) {
        if ("Income".equalsIgnoreCase(type)) {
            if ("add".equalsIgnoreCase(operation)) {
                account.setBalance(account.getBalance().add(amount));
            } else { // subtract
                account.setBalance(account.getBalance().subtract(amount));
            }
        } else if ("Expense".equalsIgnoreCase(type)) {
            if ("add".equalsIgnoreCase(operation)) {
                account.setBalance(account.getBalance().subtract(amount));
            } else { // subtract
                account.setBalance(account.getBalance().add(amount));
            }
        }
        accountRepository.save(account); // Save the updated account balance
    }

    private TransactionDTO convertToDto(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setAccountId(transaction.getAccount().getId());
        dto.setCategoryId(transaction.getCategory().getId());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setDescription(transaction.getDescription());
        return dto;
    }
}