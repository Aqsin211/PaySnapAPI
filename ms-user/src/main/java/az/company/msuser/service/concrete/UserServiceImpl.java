package az.company.msuser.service.concrete;

import az.company.msuser.dao.entity.UserEntity;
import az.company.msuser.dao.repository.UserRepository;
import az.company.msuser.exception.NotFoundException;
import az.company.msuser.exception.UserExistsException;
import az.company.msuser.model.request.AuthRequest;
import az.company.msuser.model.request.UserRequest;
import az.company.msuser.model.response.UserResponse;
import az.company.msuser.service.abstraction.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static az.company.msuser.model.enums.ErrorMessages.*;
import static az.company.msuser.model.enums.UserRoles.USER;
import static az.company.msuser.model.mapper.UserMapper.USER_MAPPER;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createUser(UserRequest request) {
        checkUserConflicts(request);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole(USER.name());
        userRepository.save(USER_MAPPER.mapRequestToEntity(request));
    }

    @Override
    public UserResponse getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(USER_MAPPER::mapEntityToResponse)
                .orElseThrow(() -> new NotFoundException(USER_DOES_NOT_EXIST.getMessage()));
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(USER_DOES_NOT_EXIST.getMessage());
        }
        userRepository.deleteById(userId);
    }

    @Override
    public void updateUser(Long userId, UserRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_DOES_NOT_EXIST.getMessage()));

        updateFields(user, request);
        userRepository.save(user);
    }

    @Override
    public Boolean userIsValid(AuthRequest request) {
        UserEntity user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException(USER_DOES_NOT_EXIST.getMessage()));
        return passwordEncoder.matches(request.getPassword(), user.getPassword());
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(USER_DOES_NOT_EXIST.getMessage()));
        return USER_MAPPER.mapEntityToResponse(user);
    }

    private void checkUserConflicts(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new UserExistsException(USER_EXISTS.getMessage());
        if (userRepository.existsByGmail(request.getGmail())) throw new UserExistsException(GMAIL_AT_USE.getMessage());
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) throw new UserExistsException(PHONE_AT_USE.getMessage());
    }

    private void updateFields(UserEntity user, UserRequest request) {
        if (!Objects.equals(user.getUsername(), request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new UserExistsException(USER_EXISTS.getMessage());
        }
        user.setUsername(request.getUsername());

        if (!Objects.equals(user.getGmail(), request.getGmail()) && userRepository.existsByGmail(request.getGmail())) {
            throw new UserExistsException(GMAIL_AT_USE.getMessage());
        }
        user.setGmail(request.getGmail());

        if (!Objects.equals(user.getPhoneNumber(), request.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new UserExistsException(PHONE_AT_USE.getMessage());
        }
        user.setPhoneNumber(request.getPhoneNumber());

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    }
}
