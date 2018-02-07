package com.example.service;

import com.example.TestHelper;
import com.example.domain.User;
import com.example.dto.CreatedUserDto;
import com.example.mapper.UserMapper;
import com.example.mapper.UserMapperImpl;
import com.example.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.CharBuffer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceMockTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserMapper userMapper = new UserMapperImpl();

    @Before
    public void setup() {
        ReflectionTestUtils.setField(userService, "userMapper", userMapper);
    }

    @Test
    public void should_add_user() {
        CreatedUserDto createdUserDto = TestHelper.FACTORY.manufacturePojo(CreatedUserDto.class);
        given(userRepository.save(any(User.class))).willReturn(any());
        given(passwordEncoder.encode(CharBuffer.wrap(createdUserDto.getPassword()))).willReturn("passwordna");
        userService.addUser(createdUserDto);
        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(argumentCaptor.capture());
        User user = argumentCaptor.getValue();
        assertThat(user.getActivationToken()).isNotNull();
        assertThat(user.getActivationExpiredDate()).isNotNull();
        assertThat(user.getPassword()).isEqualTo("passwordna");
    }
}
