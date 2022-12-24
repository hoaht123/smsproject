package com.example.smsapi.client;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.smsapi.model.Account;
import com.example.smsapi.model.LoginResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.util.Date;
import java.util.List;


@Controller
@Slf4j
public class AccountController {

    private final String URL_ACCOUNT = "http://localhost:8080/api/v1/accounts/";

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        Model model, HttpServletResponse responseHttp,
                        RedirectAttributes rattrs) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.add("username", username);
        params.add("password", password);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(params, headers);
        try {
            ResponseEntity<String> response =
                    restTemplate.exchange(URL_ACCOUNT + "login", HttpMethod.POST, request, String.class);
            LoginResponse lg = new ObjectMapper().readValue(response.getBody(), LoginResponse.class);
            Cookie jwtTokenCookie = new Cookie("_token", lg.getToken());
            DecodedJWT jwtDecode = JWT.decode(lg.getToken());
            Date timeJWT = jwtDecode.getExpiresAt();
            Date now = new Date();
            long time = (timeJWT.getTime() - now.getTime()) / 1000;
            jwtTokenCookie.setMaxAge(Integer.parseInt(Long.toString(time)));
            jwtTokenCookie.setSecure(true);
            jwtTokenCookie.setHttpOnly(true);
            jwtTokenCookie.setPath("/");
            responseHttp.addCookie(jwtTokenCookie);
            if (lg.getAccount().getRoleByRole().getRoleName().equals("ROLE_ADMIN")) {
                return "redirect:/list";
            } else {
                rattrs.addAttribute("account", lg.getAccount()).addFlashAttribute("account", lg.getAccount());
                return "redirect:/student_profile";
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                model.addAttribute("msg", e.getMessage());
                return "index";
            }
            return null;
        }
    }

    @PostMapping("/logoutAccount")
    public String logout(HttpServletResponse response){
       Cookie cookie = new Cookie("_token",null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Don't set to -1 or it will become a session cookie!
        response.addCookie(cookie);
        return "redirect:/";
    }

    @GetMapping("/list")
    public String listAccount(@CookieValue(name = "_token", defaultValue = "") String _token
            , Model model) throws JsonProcessingException {
        if (_token.equals("")) {
            return "redirect:/";
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + _token);
        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<String> listJson = restTemplate.exchange(URL_ACCOUNT + "getAll", HttpMethod.GET, entity, String.class);
        List<Account> list = new ObjectMapper().readValue(listJson.getBody(), new TypeReference<>() {
        });
        model.addAttribute("list", list.stream().filter(account -> !account.getRoleByRole().getRoleName().equals("ROLE_ADMIN")).toList());
        return "list";
    }

    @GetMapping("/student_profile")
    public String student_profile(@CookieValue(name = "_token", defaultValue = "") String _token,
                                  Model model, @ModelAttribute("account") Account account) {
        if (_token.equals("")) {
            return "redirect:/";
        }
        model.addAttribute("account", account);
        return "profile_student";
    }

    @PostMapping("/changeAvatar")
    public String changeAvatar(@CookieValue(name = "_token", defaultValue = "") String _token,
                               @RequestParam("id") Integer id,
                               @RequestParam("fileAvatar") MultipartFile file,
                               RedirectAttributes attributes) throws IOException {
        if (_token.equals("")) {
            return "redirect:/";
        }
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "multipart/form-data");
        headers.set("Authorization", "Bearer " + _token);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("file", file.getResource());
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(URL_ACCOUNT + "changeAvatar/" + id, HttpMethod.PUT, requestEntity, String.class);
        Account account = new ObjectMapper().readValue(response.getBody(), Account.class);
        attributes.addAttribute("account", account).addFlashAttribute("account", account);
        return "redirect:/student_profile";
    }

    @GetMapping("/changePassword/{id}")
    public String changePassword(@CookieValue(name = "_token", defaultValue = "") String _token,
                                 @PathVariable("id") Integer id, Model model) {
        if (_token.equals("")) {
            return "redirect:/";
        }
        model.addAttribute("id", id);
        return "change_password";
    }

    @PostMapping("/changePassword")
    public String changePassword(@CookieValue(name = "_token", defaultValue = "") String _token,
                                 @RequestParam("id") Integer id,
                                 @RequestParam("password") String password,
                                 @RequestParam("new_password") String new_password,
                                 RedirectAttributes redirectAttributes) {
        if (_token.equals("")) {
            return "redirect:/";
        }

        if(!new_password.equals(password)){
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization","Bearer "+_token);
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("id", id.toString());
            params.add("password",password);
            params.add("newPassword", new_password);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            try {
                ResponseEntity<String> response = restTemplate.exchange(URL_ACCOUNT+"changePassword/"+id,HttpMethod.PUT,request,String.class);
                Account account = new ObjectMapper().readValue(response.getBody(),Account.class);
                if (response.getStatusCode()== HttpStatus.OK){
                    redirectAttributes.addAttribute("account",account).addFlashAttribute("account",account);
                    return "redirect:student_profile";
                }
                redirectAttributes.addFlashAttribute("msg","Something is wrong");
                return "redirect:/changePassword/"+id;
            }catch (Exception e){
                log.error(e.getMessage());
                redirectAttributes.addFlashAttribute("msg","Password is wrong !");
                return "redirect:/changePassword/"+id;
            }
        }else{
            redirectAttributes.addFlashAttribute("msg","New password cannot match with password");
            return "redirect:/changePassword/"+id;
        }

    }

}