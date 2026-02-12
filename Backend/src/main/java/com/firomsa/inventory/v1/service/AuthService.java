package com.firomsa.inventory.v1.service;

import com.firomsa.inventory.exception.AuthenticationException;
import com.firomsa.inventory.exception.InvalidOtpException;
import com.firomsa.inventory.exception.ResourceNotFoundException;
import com.firomsa.inventory.exception.UserAlreadyExistsException;
import com.firomsa.inventory.model.ConfirmationOTP;
import com.firomsa.inventory.model.RefreshToken;
import com.firomsa.inventory.model.Role;
import com.firomsa.inventory.model.Roles;
import com.firomsa.inventory.model.User;
import com.firomsa.inventory.repository.ConfirmationOtpRepository;
import com.firomsa.inventory.repository.RefreshTokenRepository;
import com.firomsa.inventory.repository.RoleRepository;
import com.firomsa.inventory.repository.UserRepository;
import com.firomsa.inventory.v1.dto.ConfirmOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ConfirmOtpResponseDTO;
import com.firomsa.inventory.v1.dto.LoginRequestDTO;
import com.firomsa.inventory.v1.dto.LoginResponseDTO;
import com.firomsa.inventory.v1.dto.LogoutRequestDTO;
import com.firomsa.inventory.v1.dto.LogoutResponseDTO;
import com.firomsa.inventory.v1.dto.RefreshTokenRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterRequestDTO;
import com.firomsa.inventory.v1.dto.RegisterResponseDTO;
import com.firomsa.inventory.v1.dto.ResendOtpRequestDTO;
import com.firomsa.inventory.v1.dto.ResendOtpResponseDTO;
import com.firomsa.inventory.v1.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.Random;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final ConfirmationOtpRepository confirmationOtpRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTAuthService jwtAuthService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final int OTP_DURATION = 6;
    private final int REFRESH_TOKEN_DURATION = 15;

    @Transactional
    public RegisterResponseDTO create(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.findByUsername(registerRequestDTO.username()).isPresent()) {
            throw new UserAlreadyExistsException(registerRequestDTO.username());
        }

        if (userRepository.findByEmail(registerRequestDTO.email()).isPresent()) {
            throw new UserAlreadyExistsException(registerRequestDTO.email());
        }

        Role role = roleRepository.findByName(registerRequestDTO.role()).orElseThrow(
                () -> new ResourceNotFoundException("Role: " + registerRequestDTO.role().name()));

        User user = userMapper.toModel(registerRequestDTO);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(registerRequestDTO.password()));

        var registeredUser = userRepository.save(user);
        var otp = generateOtp();
        confirmationOtpRepository.save(ConfirmationOTP.builder().otp(otp).user(registeredUser)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_DURATION)).build());
        emailService.sendOtp(otp, user.getEmail());
        var response = new RegisterResponseDTO(userMapper.toDTO(registeredUser),
                "You have successfully registered, confirm the OTP sent to your email");
        return response;
    }

    @Transactional
    public RegisterResponseDTO createAdmin(RegisterRequestDTO registerRequestDTO) {
        if (userRepository.count() > 0) {
            throw new AuthenticationException(
                    "Only one admin can be registered, if you want to create more admins please ask the existing admin to create them");
        }

        Role role = roleRepository.findByName(Roles.ADMIN)
                .orElseThrow(() -> new ResourceNotFoundException("Role: ADMIN"));

        User user = userMapper.toModel(registerRequestDTO);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(registerRequestDTO.password()));

        var registeredUser = userRepository.save(user);
        var otp = generateOtp();
        confirmationOtpRepository.save(ConfirmationOTP.builder().otp(otp).user(registeredUser)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_DURATION)).build());
        emailService.sendOtp(otp, user.getEmail());
        var response = new RegisterResponseDTO(userMapper.toDTO(registeredUser),
                "You have successfully registered, confirm the OTP sent to your email");
        return response;
    }

    @Transactional
    public ConfirmOtpResponseDTO confirmOtp(ConfirmOtpRequestDTO confirmOtpRequestDTO) {
        User user = userRepository.findByEmail(confirmOtpRequestDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(confirmOtpRequestDTO.email()));
        var otp = confirmationOtpRepository
                .findByOtpAndExpiresAtAfterAndConfirmedFalse(confirmOtpRequestDTO.otp(),
                        LocalDateTime.now())
                .orElseThrow(() -> new InvalidOtpException(
                        "Wrong otp, please use the correct OTP code or ask for a resend"));

        user.setEnabled(true);
        otp.setConfirmed(true);
        userRepository.save(user);
        confirmationOtpRepository.save(otp);
        confirmationOtpRepository.deleteAllByUser(user);
        return new ConfirmOtpResponseDTO(
                "Successfully confirmed OTP, please login using your email and password");
    }

    public String generateOtp() {
        var random = new Random();
        var numbers = new StringBuffer();

        for (int i = 0; i < 5; i++) {
            numbers.append(random.nextInt(10));
        }

        return numbers.toString();
    }

    public ResendOtpResponseDTO resendOtp(ResendOtpRequestDTO resendOtpRequestDTO) {
        User user = userRepository.findByEmail(resendOtpRequestDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(resendOtpRequestDTO.email()));
        var otp = generateOtp();
        confirmationOtpRepository.save(ConfirmationOTP.builder().otp(otp).user(user)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_DURATION)).build());
        emailService.sendOtp(otp, user.getEmail());
        return new ResendOtpResponseDTO("Successfully resent OTP, check your inbox");
    }

    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByEmail(loginRequestDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(loginRequestDTO.email()));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDTO.email(), loginRequestDTO.password()));

        String accessToken = jwtAuthService.generateToken(loginRequestDTO.email());

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DURATION));
        var savedRefreshToken = refreshTokenRepository.save(refreshToken);

        return new LoginResponseDTO(user.getRole().getName(), accessToken,
                savedRefreshToken.getId().toString(), user.getUsername(), user.getEmail());
    }

    public LoginResponseDTO refreshAccessToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        User user = userRepository.findByEmail(refreshTokenRequestDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(refreshTokenRequestDTO.email()));
        var refreshToken = refreshTokenRepository
                .findByIdAndExpiresAtAfter(refreshTokenRequestDTO.refreshToken(),
                        LocalDateTime.now())
                .orElseThrow(() -> new AuthenticationException(
                        "Refresh token is invalid, please login"));
        String accessToken = jwtAuthService.generateToken(refreshTokenRequestDTO.email());

        return new LoginResponseDTO(user.getRole().getName(), accessToken,
                refreshToken.getId().toString(), user.getUsername(), user.getEmail());
    }

    @Transactional
    public LogoutResponseDTO logoutUser(LogoutRequestDTO logoutRequestDTO) {
        User user = userRepository.findByEmail(logoutRequestDTO.email())
                .orElseThrow(() -> new ResourceNotFoundException(logoutRequestDTO.email()));
        refreshTokenRepository
                .findByIdAndExpiresAtAfter(logoutRequestDTO.refreshToken(), LocalDateTime.now())
                .orElseThrow(() -> new AuthenticationException(
                        "Refresh token is invalid, please login"));

        refreshTokenRepository.deleteAllByUser(user);
        return new LogoutResponseDTO("Successfully logged out");
    }
}
