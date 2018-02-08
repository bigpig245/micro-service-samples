package com.example.mapper;

import com.example.TestHelper;
import com.example.domain.User;
import com.example.dto.UserDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MapperTestConfiguration.class)
public class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void should_map_user_to_user_dto() {
        User user = TestHelper.FACTORY.manufacturePojo(User.class);
        UserDto userDto = userMapper.userToUserDto(user);
        assertThat(userDto).isEqualToComparingFieldByField(user);
    }

}
