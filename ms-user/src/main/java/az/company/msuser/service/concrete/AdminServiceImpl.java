package az.company.msuser.service.concrete;

import az.company.msuser.dao.entity.UserEntity;
import az.company.msuser.dao.repository.UserRepository;
import az.company.msuser.exception.ActionDeniedException;
import az.company.msuser.exception.NotFoundException;
import az.company.msuser.exception.UserExistsException;
import az.company.msuser.model.request.UserRequest;
import az.company.msuser.model.response.UserResponse;
import az.company.msuser.service.abstraction.AdminService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static az.company.msuser.model.enums.ErrorMessages.*;
import static az.company.msuser.model.enums.UserRoles.ADMIN;
import static az.company.msuser.model.enums.UserRoles.USER;
import static az.company.msuser.model.mapper.UserMapper.USER_MAPPER;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createAdmin(UserRequest request) {
        checkUserConflicts(request);
        request.setPassword(passwordEncoder.encode(request.getPassword()));
        request.setRole(ADMIN.name());
        userRepository.save(USER_MAPPER.mapRequestToEntity(request));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(USER_MAPPER::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_DOES_NOT_EXIST.getMessage()));

        if (user.getRole() != USER){
            throw new ActionDeniedException(ACTION_REFUSED.getMessage());
        }
        userRepository.delete(user);
    }

    @Override
    public UserResponse getUser(Long userId) {
        return userRepository.findById(userId)
                .map(USER_MAPPER::mapEntityToResponse)
                .orElseThrow(() -> new NotFoundException(USER_DOES_NOT_EXIST.getMessage()));
    }

    @Override
    public void updateUser(Long userId, UserRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_DOES_NOT_EXIST.getMessage()));

        if (user.getRole() != USER){
            throw new ActionDeniedException(ACTION_REFUSED.getMessage());
        }
        updateFields(user, request);
        userRepository.save(user);
    }


    private void checkUserConflicts(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new UserExistsException(USER_EXISTS.getMessage());
        if (userRepository.existsByGmail(request.getGmail())) throw new UserExistsException(GMAIL_AT_USE.getMessage());
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber()))
            throw new UserExistsException(PHONE_AT_USE.getMessage());
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
