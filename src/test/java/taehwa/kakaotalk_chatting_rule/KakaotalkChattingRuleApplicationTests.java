package taehwa.kakaotalk_chatting_rule;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import taehwa.kakaotalk_chatting_rule.dto.bridge.AutoReplyRequest;
import taehwa.kakaotalk_chatting_rule.service.ForbiddenWordProvider;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class KakaotalkChattingRuleApplicationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ForbiddenWordProvider forbiddenWordProvider;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void swaggerApiDocsIsAccessible() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    void swaggerUiIsAccessible() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void bridgeHealthIsAccessible() throws Exception {
        mockMvc.perform(get("/api/bridge/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("bridge ok"));
    }

    @Test
    void autoReplyReturnsWarningForViolation() throws Exception {
        AutoReplyRequest request = AutoReplyRequest.builder()
                .room("friends-room")
                .sender("alice")
                .message("내 번호는 010-1234-5678 이야")
                .build();

        mockMvc.perform(post("/api/bridge/auto-reply")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shouldReply").value(true))
                .andExpect(jsonPath("$.data.violated").value(true))
                .andExpect(jsonPath("$.data.violationType").value("PERSONAL_INFO"))
                .andExpect(jsonPath("$.data.replyMessage").value(containsString("[경고]")));
    }

    @Test
    void autoReplySkipsNormalMessage() throws Exception {
        AutoReplyRequest request = AutoReplyRequest.builder()
                .room("friends-room")
                .sender("bob")
                .message("오늘 저녁 뭐 먹지?")
                .build();

        mockMvc.perform(post("/api/bridge/auto-reply")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shouldReply").value(false))
                .andExpect(jsonPath("$.data.violated").value(false));
    }

    @Test
    void autoReplySkipsBotMessages() throws Exception {
        AutoReplyRequest request = AutoReplyRequest.builder()
                .room("friends-room")
                .sender("warning-bot")
                .message("경고 메시지")
                .fromBot(true)
                .build();

        mockMvc.perform(post("/api/bridge/auto-reply")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shouldReply").value(false))
                .andExpect(jsonPath("$.data.violated").value(false));
    }

    @Test
    void autoReplyDetectsForbiddenWordFromResource() throws Exception {
        AutoReplyRequest request = AutoReplyRequest.builder()
                .room("friends-room")
                .sender("charlie")
                .message(forbiddenWordProvider.firstWord())
                .build();

        mockMvc.perform(post("/api/bridge/auto-reply")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.shouldReply").value(true))
                .andExpect(jsonPath("$.data.violated").value(true))
                .andExpect(jsonPath("$.data.violationType").value("FORBIDDEN_WORD"));
    }
}
