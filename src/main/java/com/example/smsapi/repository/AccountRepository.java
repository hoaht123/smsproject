package com.example.smsapi.repository;

import com.example.smsapi.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    public Account findAccountByUsername(String username);

//    public Account findAccountByUsernameAndPassword(String username,String password);
}