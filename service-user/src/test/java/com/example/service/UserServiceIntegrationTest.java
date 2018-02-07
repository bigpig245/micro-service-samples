package com.example.service;


import com.example.dto.UserDto;
import com.example.dto.UserLoginDto;
import com.example.dto.enumeration.SUMessage;
import com.example.mapper.UserMapper;
import com.github.springtestdbunit.TransactionDbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import javax.transaction.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestExecutionListeners(value = {TransactionDbUnitTestExecutionListener.class,
        DependencyInjectionTestExecutionListener.class,
        MockitoTestExecutionListener.class,
        ResetMocksTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class})
@DatabaseSetup(value = "/dataset/user.xml")
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void should_get_user() {
        UserDto userDto = userService.getUserInfo(UserLoginDto.builder().login("bigpig").build());
        assertThat(userDto.getEmail()).isEqualTo("ntrang2459@yahoo.com");
    }

    @Test
    public void should_not_get_inactive_user() {
        assertThatThrownBy(() -> userService.getUserInfo(UserLoginDto.builder().login("trang").build()))
                .hasMessage(SUMessage.INACTIVE_USER.getMessage());
    }
}
