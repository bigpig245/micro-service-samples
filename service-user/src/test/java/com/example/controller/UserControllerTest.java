package com.example.controller;

import com.example.TestHelper;
import com.example.config.CountryFilter;
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
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Test controller with secure
 */

@RunWith(SpringRunner.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    private MockMvc mockMvc;
    @Autowired
    private UserController userController;


    @MockBean
    private UserService userService;

    @Before
    public void setup() {
        mockMvc = standaloneSetup(userController)
                .addFilter(new CountryFilter())
                .build();
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
