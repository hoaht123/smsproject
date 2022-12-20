package com.example.smsapi.api;

import com.example.smsapi.jwt.JwtTokenProvider;
import com.example.smsapi.model.Account;
import com.example.smsapi.model.LoginResponse;
import com.example.smsapi.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountRestController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/saveAccount")
    public ResponseEntity<?> saveAccount(@RequestParam("account") String account, @RequestParam("file") MultipartFile file) {
        try {
            Account accountConvert = new ObjectMapper().readValue(account, Account.class);
            String avatarImage = accountService.saveFile(file);
            accountConvert.setAvatarPath(avatarImage);
            Account savedAccount = accountService.createAccount(accountConvert);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedAccount);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/getAll")
    public ResponseEntity<?> getAll() {
        List<Account> getAll = accountService.listAccount();
        return ResponseEntity.status(HttpStatus.OK).body(getAll);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginAccount(@RequestParam("username") String username, @RequestParam("password") String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            Account account = (Account) authentication.getPrincipal();
            String jwtToken = jwtTokenProvider.generateTokenFromAccount(account);
            return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(account, jwtToken, "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new LoginResponse(null, null, "failed"));
        }

    }

    @PutMapping("/changePassword/{id}")
    public ResponseEntity<?> changePasswordAccount(@PathVariable("id") Integer id,
                                                   @RequestParam("password") String password,
                                                   @RequestParam("newPassword") String newPassword) {
        try {
            Account account = accountService.getAccount(id);
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(account, password)
            );
            Account getAccount = (Account) authentication.getPrincipal();
            getAccount.setPassword(newPassword);
            Account savedAccount = accountService.saveAccount(getAccount);
            return ResponseEntity.status(HttpStatus.OK).body(savedAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new LoginResponse(null, null, "failed"));
        }
    }

    @PutMapping("/changeAvatar/{id}")
    public ResponseEntity<?> changeAvatar(@PathVariable("id") Integer id,@RequestParam("file")MultipartFile file){
        try {
            Account account = accountService.changeAvatarAccount(id,file);
            return account!=null ? ResponseEntity.status(HttpStatus.OK).body(account) :
                    ResponseEntity.status(HttpStatus.NOT_FOUND).body("NOT found account with id := "+id);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

}
