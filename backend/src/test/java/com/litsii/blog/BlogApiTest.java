package com.litsii.blog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BlogApiTest {

    @Autowired
    MockMvc mvc;

    private static final String BODY = """
            {"title":"첫 글","summary":"요약","content":"# 안녕","status":"%s"}
            """;

    @Test
    void 공개목록은_누구나_조회() throws Exception {
        mvc.perform(get("/api/posts")).andExpect(status().isOk());
    }

    @Test
    void 관리자API는_비로그인시_401() throws Exception {
        mvc.perform(get("/api/admin/posts")).andExpect(status().isUnauthorized());
    }

    @Test
    void 정의되지_않은_경로는_차단() throws Exception {
        mvc.perform(get("/h2-console")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void CSRF_토큰없이_쓰기불가() throws Exception {
        mvc.perform(post("/api/admin/posts").contentType(MediaType.APPLICATION_JSON).content(BODY.formatted("DRAFT")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 초안은_공개API에서_보이지_않음() throws Exception {
        String location = mvc.perform(post("/api/admin/posts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY.formatted("DRAFT")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = location.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/posts/" + id)).andExpect(status().isNotFound());
        mvc.perform(get("/api/admin/posts/" + id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void 발행글은_공개API에서_조회() throws Exception {
        String res = mvc.perform(post("/api/admin/posts").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(BODY.formatted("PUBLISHED")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = res.replaceAll(".*\"id\":(\\d+).*", "$1");

        mvc.perform(get("/api/posts/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("첫 글"));
    }

    @Test
    void 잘못된_비밀번호는_401() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 올바른_비밀번호는_로그인() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin1234\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated").value(true));
    }
}
