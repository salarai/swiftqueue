package com.swiftqueue.service.user;

import com.swiftqueue.dto.location.AddressDTO;
import com.swiftqueue.dto.user.UserInfoDTO;
import com.swiftqueue.exception.business.BusinessException;
import com.swiftqueue.exception.server.ResourceNotFoundException;
import com.swiftqueue.model.user.UserInfo;
import com.swiftqueue.repository.user.UserDetailsRepository;
import com.swiftqueue.service.location.AddressService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class DefaultUserInfoService implements UserInfoService {

    private final ModelMapper modelMapper;
    private final AddressService addressService;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsRepository userDetailsRepository;

    @Autowired
    public DefaultUserInfoService(ModelMapper modelMapper, AddressService addressService, UserDetailsRepository userDetailsRepository) {
        this.modelMapper = modelMapper;
        this.addressService = addressService;
        this.userDetailsRepository = userDetailsRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserInfo> getUserInfoByUserName(String userName) {
        return userDetailsRepository.findByUsername(userName);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDTO getByUsername(String username) {
        return userDetailsRepository.findByUsername(username)
                .map(result -> modelMapper.map(result, UserInfoDTO.class))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoDTO getById(Long id) throws ResourceNotFoundException {
        UserInfo userInfo = userDetailsRepository.findOne(id);
        if (userInfo == null)
            throw new ResourceNotFoundException(TYPE, id);
        return modelMapper.map(userInfo, UserInfoDTO.class);
    }

    @Transactional
    public UserInfoDTO save(UserInfoDTO dto) throws BusinessException {
        if (getByUsername(dto.getUsername()) != null)
            throw new BusinessException(TYPE, "save", "A user already exists with username: " + dto.getUsername());
        if (dto.getAddress() != null) {
            AddressDTO addressDTO = addressService.save(dto.getAddress());
            dto.setAddress(addressDTO);
        }
        UserInfo userInfo = modelMapper.map(dto, UserInfo.class);
        userInfo.setPassword(passwordEncoder.encode(dto.getPassword()));
        userInfo.setRole("ROLE_USER");
        userInfo.setEnabled(false);
        return modelMapper.map(userDetailsRepository.save(userInfo), UserInfoDTO.class);
    }

    @Override
    @Transactional
    public void updateUserInfo(UserInfoDTO userInfoDTO) throws BusinessException {
        UserInfo userInfo = userDetailsRepository.findOne(userInfoDTO.getId());
        if (userInfo == null)
            throw new BusinessException("User", "update", "No user found with ID: " + userInfoDTO.getId());
        userInfo.setFirstName(userInfoDTO.getFirstName());
        userInfo.setLastName(userInfoDTO.getLastName());
        userDetailsRepository.save(userInfo);
    }

    @Override
    @Transactional
    public void enableUser(UserInfoDTO userInfoDTO) throws BusinessException {
        UserInfo userInfo = userDetailsRepository.findOne(userInfoDTO.getId());
        if (userInfo == null)
            throw new BusinessException("User", "enable", "No user found with ID: " + userInfoDTO.getId());
        userInfo.setEnabled(true);
        userDetailsRepository.save(userInfo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean enabledByUsername(String username) throws ResourceNotFoundException {
        return userDetailsRepository.findByUsername(username)
                .map(UserInfo::isEnabled)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}
