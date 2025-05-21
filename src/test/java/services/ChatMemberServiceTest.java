package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.pocgg.SNSApp.model.*;
import ru.pocgg.SNSApp.model.exceptions.EntityNotFoundException;
import ru.pocgg.SNSApp.services.ChatMemberService;
import ru.pocgg.SNSApp.services.ChatService;
import ru.pocgg.SNSApp.services.UserService;
import ru.pocgg.SNSApp.services.DAO.interfaces.ChatMemberServiceDAO;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMemberServiceTest {

    @Mock
    private ChatMemberServiceDAO dao;
    @Mock
    private ChatService chatService;
    @Mock
    private UserService userService;
    @InjectMocks
    private ChatMemberService service;

    private int chatId;
    private int memberId;
    private Instant entryDate;
    private Chat chat;
    private User member;
    private ChatMemberId id;
    private ChatMember chatMember;

    @BeforeEach
    void setUp() {
        chatId = 1;
        memberId = 2;
        entryDate = Instant.now();

        member = User.builder()
                .userName("a").creationDate(Instant.now())
                .birthDate(LocalDate.now().minusYears(30))
                .password("p").email("a@a")
                .firstName("A").secondName("A")
                .gender(Gender.MALE).systemRole(SystemRole.USER)
                .deleted(false).acceptingPrivateMsgs(true)
                .postsPublic(true).banned(false)
                .build();
        member.setId(memberId);

        chat = Chat.builder()
                .owner(member)
                .name("chatName")
                .description("desc")
                .creationDate(entryDate)
                .isPrivate(false)
                .build();
        chat.setId(chatId);

        id = ChatMemberId.builder()
                .chatId(chatId)
                .memberId(memberId)
                .build();

        chatMember = ChatMember.builder()
                .chat(chat)
                .member(member)
                .entryDate(entryDate)
                .build();
    }

    @Test
    void createChatMember_positive() {
        when(chatService.getChatById(chatId)).thenReturn(chat);
        when(userService.getUserById(memberId)).thenReturn(member);

        ChatMember result = service.createChatMember(chatId, memberId, entryDate);

        assertNotNull(result);
        assertEquals(chat, result.getChat());
        assertEquals(member, result.getMember());
        assertEquals(entryDate, result.getEntryDate());
        verify(dao).addChatMember(result);
        verify(dao).forceFlush();
    }

    @Test
    void createChatMember_negative() {
        when(chatService.getChatById(chatId)).thenThrow(new EntityNotFoundException("not found"));

        assertThrows(EntityNotFoundException.class,
                () -> service.createChatMember(chatId, memberId, entryDate));
        verifyNoInteractions(dao);
    }

    @Test
    void getChatMemberById_positive() {
        when(dao.getChatMemberById(id)).thenReturn(chatMember);
        ChatMember result = service.getChatMemberById(id);

        assertSame(chatMember, result);
    }

    @Test
    void getChatMemberById_negative() {
        when(dao.getChatMemberById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.getChatMemberById(id));
    }

    @Test
    void isChatMemberExist_positive() {
        when(dao.getChatMemberById(id)).thenReturn(chatMember);

        assertTrue(service.isChatMemberExist(id));
    }

    @Test
    void isChatMemberExist_negative() {
        when(dao.getChatMemberById(id)).thenReturn(null);

        assertFalse(service.isChatMemberExist(id));
    }

    @Test
    void getAllChatMembers_positive() {
        when(dao.getAllChatMembers()).thenReturn(List.of(chatMember));
        List<ChatMember> result = service.getAllChatMembers();

        assertSame(chatMember, result.get(0));
    }

    @Test
    void getAllChatMembers_negative() {
        when(dao.getAllChatMembers()).thenReturn(Collections.emptyList());
        List<ChatMember> result = service.getAllChatMembers();

        assertTrue(result.isEmpty());
    }

    @Test
    void getChatMembersByChatId_positive() {
        when(dao.getMembersByChatId(chatId)).thenReturn(List.of(chatMember));
        List<ChatMember> result = service.getChatMembersByChatId(chatId);

        assertSame(chatMember, result.get(0));
    }

    @Test
    void getChatMembersByChatId_negative() {
        when(dao.getMembersByChatId(chatId)).thenReturn(Collections.emptyList());
        List<ChatMember> result = service.getChatMembersByChatId(chatId);

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteChatMember_positive() {
        when(dao.getChatMemberById(id)).thenReturn(chatMember);
        service.deleteChatMember(id);

        verify(dao).removeChatMember(chatMember);
    }

    @Test
    void deleteChatMember_negative() {
        when(dao.getChatMemberById(id)).thenReturn(null);

        assertThrows(EntityNotFoundException.class,
                () -> service.deleteChatMember(id));
    }
}
