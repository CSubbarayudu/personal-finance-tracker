package com.example.personalfinancetracker.service;

import com.example.personalfinancetracker.dto.ChartDataDTO;
import com.example.personalfinancetracker.dto.TransactionDTO;
import com.example.personalfinancetracker.model.Account;
import com.example.personalfinancetracker.model.Category;
import com.example.personalfinancetracker.model.Transaction;
import com.example.personalfinancetracker.model.TransactionType; // IMPORTANT: Ensure this import is present
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository; // Correctly injected and used
    private final UserRepository userRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository, // Corrected: Injecting CategoryRepository
                              UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository; // Initializing the field
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
        transaction.setTransactionType(transactionDTO.getTransactionType()); // Uses enum
        transaction.setDescription(transactionDTO.getDescription());

        // Update account balance: uses the enum now
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

        // Revert old transaction's impact on account balance: uses the enum now
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
        existingTransaction.setTransactionType(transactionDTO.getTransactionType()); // Uses enum
        existingTransaction.setDescription(transactionDTO.getDescription());

        // Apply new transaction's impact on account balance: uses the enum now
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

        // Revert transaction's impact on account balance: uses the enum now
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

    /**
     * Calculates the total expense amount for each category for a given user.
     * Only considers transactions with type "EXPENSE".
     *
     * @param userId The ID of the logged-in user.
     * @return A list of ChartDataDTO, where each DTO contains a category name (label)
     * and the total expense amount for that category (value).
     */
    public List<ChartDataDTO> getExpenseDistributionByCategory(Long userId) {
        // Fetch all expense transactions for the user - now uses enum
        List<Transaction> expenseTransactions = transactionRepository.findByUserIdAndTransactionType(userId, TransactionType.EXPENSE);

        // Group by category name and sum the amounts
        Map<String, BigDecimal> expenseTotalsByCategory = expenseTransactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategory().getCategoryName(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        // Convert the map to a list of ChartDataDTOs
        return expenseTotalsByCategory.entrySet().stream()
                .map(entry -> new ChartDataDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Calculates the total amount for Income and Expense transaction types for a given user.
     *
     * @param userId The ID of the logged-in user.
     * @return A list of ChartDataDTO, where each DTO contains the transaction type (label)
     * and the total amount for that type (value).
     */
    public List<ChartDataDTO> getTransactionTypeDistribution(Long userId) {
        // Fetch all transactions for the user
        List<Transaction> allTransactions = transactionRepository.findByUserId(userId);

        // Group by transaction type (INCOME/EXPENSE) and sum the amounts - now uses enum
        Map<TransactionType, BigDecimal> typeTotals = allTransactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getTransactionType, // Group by TransactionType enum
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        // Convert the map to a list of ChartDataDTOs - now converts enum to String label
        return typeTotals.entrySet().stream()
                .map(entry -> new ChartDataDTO(entry.getKey().name(), entry.getValue())) // Use .name() to get string label
                .collect(Collectors.toList());
    }

    /**
     * Updates the account balance based on the transaction type and operation.
     * This is the core logic for adding/subtracting amounts.
     *
     * @param account The account whose balance needs to be updated.
     * @param amount The amount of the transaction.
     * @param type The TransactionType (INCOME or EXPENSE).
     * @param operation "add" for applying the transaction, "subtract" for reverting.
     */
    private void updateAccountBalance(Account account, BigDecimal amount, TransactionType type, String operation) {
        if (TransactionType.INCOME.equals(type)) { // Correctly compares with the enum
            if ("add".equalsIgnoreCase(operation)) {
                account.setBalance(account.getBalance().add(amount));
            } else { // "subtract"
                account.setBalance(account.getBalance().subtract(amount));
            }
        } else if (TransactionType.EXPENSE.equals(type)) { // Correctly compares with the enum
            if ("add".equalsIgnoreCase(operation)) {
                account.setBalance(account.getBalance().subtract(amount)); // Subtract for expenses when adding transaction
            } else { // "subtract"
                account.setBalance(account.getBalance().add(amount)); // Add back for expenses when reverting transaction
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
        dto.setTransactionType(transaction.getTransactionType()); // Uses enum
        dto.setDescription(transaction.getDescription());
        return dto;
    }
}
