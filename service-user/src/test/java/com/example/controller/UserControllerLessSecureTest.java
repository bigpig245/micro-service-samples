package com.example.controller;

import com.example.TestHelper;
import com.example.dto.CreatedUserDto;
import com.example.service.UserService;
import com.example.utils.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test controller with secure
 */

@RunWith(SpringRunner.class)
@WebMvcTest(value = UserController.class, secure = false)
public class UserControllerLessSecureTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Before
    public void setup() {
    }

    @Test
    public void test_create_user() throws Exception {
        CreatedUserDto createdUserDto = TestHelper.FACTORY.manufacturePojo(CreatedUserDto.class);
        doNothing().when(userService).addUser(any());
        mockMvc.perform(post("/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Constants.MAPPER.writeValueAsBytes(createdUserDto))
        ).andExpect(status().isNoContent());
    }
}
