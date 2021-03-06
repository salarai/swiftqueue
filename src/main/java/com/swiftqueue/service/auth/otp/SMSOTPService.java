package com.swiftqueue.service.auth.otp;

import com.swiftqueue.dto.auth.VerificationCodeDTO;
import com.swiftqueue.model.auth.VerificationCode;
import com.swiftqueue.repository.auth.VerificationCodeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SMSOTPService {

    private final ModelMapper modelMapper;
    private final VerificationCodeRepository verificationCodeRepository;

    @Autowired
    public SMSOTPService(ModelMapper modelMapper, VerificationCodeRepository verificationCodeRepository) {
        this.modelMapper = modelMapper;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    @Transactional
    public VerificationCodeDTO sendOTP(String number) {
        verificationCodeRepository.findByPhoneNumber(number).ifPresent(verificationCodeRepository::delete);
        VerificationCode verificationCode = verificationCodeRepository.save(new VerificationCode("c0de", number, LocalDateTime.now()));
        return modelMapper.map(verificationCode, VerificationCodeDTO.class);
    }

    @Transactional
    public boolean verifyOTP(String code, String number) {
        return verificationCodeRepository.findByCodeAndPhoneNumber(code, number)
                .map(value -> {
                    verificationCodeRepository.delete(value.getId());
                    return true;
                }).orElse(false);
    }
}
