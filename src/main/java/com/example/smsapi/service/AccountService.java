package com.example.smsapi.service;

import com.example.smsapi.Utils.FileUtils;
import com.example.smsapi.model.Account;
import com.example.smsapi.repository.AccountRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    private AccountRepository repository;
    private final String PATH_SAVE = "avatar/";

    @Autowired
    Environment environment;

    @Autowired
    private FileUtils fileUtils;

    private FirebaseOptions options;

    @EventListener
    public void init(ApplicationReadyEvent event) {
        try {
            ClassPathResource serviceAccount = new ClassPathResource("smsproject-2deea-firebase-adminsdk-wl2nq-e9e4904b01.json");
            options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount.getInputStream()))
                    .setStorageBucket(environment.getProperty("firebase.bucket-name"))
                    .build();
            FirebaseApp.initializeApp(options);

        } catch (Exception ex) {

            ex.printStackTrace();

        }
    }

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = repository.findAccountByUsername(username);
        if (account == null) {
            throw new UsernameNotFoundException(account.getUsername() + "not exsit");
        }
        return account;
    }

    public List<Account> listAccount() {
        return repository.findAll();
    }

    public Account createAccount(Account account) {
        String imageUrl = getURLFile(account.getAvatarPath());
        account.setAvatarURL(imageUrl);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account saveAccount = repository.save(account);
        return saveAccount;
    }

    public Account saveAccount(Account account){
        account.setPassword(passwordEncoder.encode(account.getPassword()));
       return repository.save(account);
    }

    public Account getAccount(Integer id) {
        return repository.findById(id).orElse(null);
    }


    public String saveFile(MultipartFile file) throws IOException {
        String pathFile = PATH_SAVE + fileUtils.generateFileName(file.getOriginalFilename());
        Bucket bucket = StorageClient.getInstance().bucket();
        bucket.create(pathFile, file.getBytes(), file.getContentType());
        return pathFile;
    }

    public String getURLFile(String pathFile) {
        return String.format(environment.getProperty("firebase.image-url"), pathFile);
    }

    public Account changeAvatarAccount(Integer id,MultipartFile file) throws IOException {
        Account account = repository.findById(id).orElse(null);
        if(account!=null){
            Bucket bucket = StorageClient.getInstance().bucket();
            boolean isFind = bucket.get(account.getAvatarPath()).delete();
            if(isFind){
                String pathImage = PATH_SAVE + fileUtils.generateFileName(file.getOriginalFilename());
                bucket.create(pathImage,file.getBytes(),file.getContentType());
                account.setAvatarPath(pathImage);
                account.setAvatarURL(getURLFile(pathImage));
                repository.save(account);
                return account;
            }
            return null;
        }
        return null;
    }

}
